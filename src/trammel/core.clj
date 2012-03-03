;; trammel.clj -- Contracts programming library for Clojure

;; by Michael Fogus - <http://fogus.me/fun/trammel>
;; May 26, 2010

; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
; and distribution terms for this software are covered by the Eclipse
; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file COPYING the root of this
; distribution.  By using this software in any fashion, you are
; agreeing to be bound by the terms of this license.  You must not
; remove this notice, or any other, from this software.

(ns trammel.core
  "The core contracts programming functions and macros for Trammel."
  (:use [trammel.funcify :only (funcify)])
  (:use trammel.factors))

;; HOF support

(defrecord HOC [args argspec ctx])

(defmacro _ [args & argspec]
  `(HOC. '~args (vec '~argspec) nil))

(comment
  (_ [n] even? number? => number?)
)

;; # base functions and macros

(defn- keys-apply
  "Takes a function, a set of keys, and a map and applies the function to the map on the given keys.  
   A new map of the results of the function applied to the keyed entries is returned.
  "
  [f ks m]
  {:pre  [(or (fn? f) (keyword? f) (symbol? f))
          (except (coll? ks) (map? ks))
          (map? m)]
   :post [(map? %)]}
  (let [only (select-keys m ks)] 
    (zipmap (keys only)
            (map f (vals only)))))

(defn- manip-map
  "Takes a function, a set of keys, and a map and applies the function to the map on the given keys.  
   A modified version of the original map is returned with the results of the function applied to each 
   keyed entry.
  "
  [f ks m]
  {:pre  [(or (fn? f) (keyword? f) (symbol? f))
          (except (coll? ks) (map? ks))
          (map? m)]
   :post [(map? %)
          (= (keys %) (keys m))]}  
  (conj m (keys-apply f ks m)))

(defn- build-pre-post-map
  "Takes a vector of the form `[pre ... => post ...]` and infers the expectations described
   therein.  The map that comes out will look like Clojure's default pre- anlein d post-conditions
   map.  If the argument is already a map then it's assumed that the default pre/post map is used and
   as a result is used directly without manipulation.
  "
  [cnstr]
  (if (vector? cnstr)
    (let [[L M R] (partition-by #{'=>} cnstr)]
      {:pre  (when (not= L '(=>)) L)
       :post (if (= L '(=>)) M R)})
    cnstr))

(defn- build-constraints-map 
  "Takes the corresponding arglist and a vector of the contract expectations, the latter of which looks 
   like any of the following:

       [(= 0 _)] or [number?] ;; only the pre-
       [number? => number?]   ;; a pre- and post-
       [=> number?]           ;; only a post-
       [foo bar => baz]       ;; 2 pre- and 1 post-

   It then takes this form and builds a pre- and post-condition map of the form:

       {:pre  [(foo x) (bar x)]
        :post [(baz %)]}
  "
  [args cnstr]
  [args 
   (->> (build-pre-post-map cnstr)
        (manip-map (partial funcify '[%]) [:post])
        (manip-map (partial funcify args) [:pre]))])


(defn- build-contract 
  "Expects a seq representing an arity-based expectation of the form:

        [[x] {:pre [(foo x)] :post [(bar %)]}]

   It then uses this data to build another list reprsenting a specific arity body
   for a higher-order function with attached pre- and post-conditions that directly 
   calls the function passed in:

        ([f x] {:pre [(foo x)] :post [(bar %)]} (f x))

   However, the picture is slightly more compilcated than that because Clojure does
   not have disparate pre-/post-conditions.  Therefore, it's on me to provide a
   slightly more crystaline picture of the condition failure when it occurs.  As a
   result the body of the contract is interwoven with `try`/`catch` blocks to catch
   and examine the contents of `AssertionErrors` and based on context rethrow them
   with more information.  At the moment this information only takes the form of a
   richer assertion message.
  "
  [n cnstr]
  (let [[args pre-post-map] cnstr]
    `(~(into '[f] args)
      (let [ret# (try
                   ((fn []
                      ~(select-keys pre-post-map [:pre])
                      ~(list* 'f (mapcat (fn [item]
                                           (cond (symbol? item) [item]
                                                 (map? item) [(:as item)]
                                                 :else [item]))
                                         args))))
                   (catch AssertionError pre#
                     (throw (AssertionError. (str "Pre-condition failure in " ~n "! " (.getMessage pre#))))))]
        (try
          ((fn []
             ~(select-keys pre-post-map [:post])
             ret#))
          (catch AssertionError post#
            (throw (AssertionError. (str "Post-condition failure in " ~n "! " (.getMessage post#))))))))))

(comment
  (let [hoc (_ [n] even? number? => number?)]
    (build-contract 'hof (build-constraints-map (:args hoc) (:argspec hoc))))
)

(defmacro contract
  "The base contract form returning a higher-order function that can then be partially
   applied to an existing function to 'apply' a contract.  Take for example a simple
   contract that describes an expectation for a function that simply takes one or two
   numbers and returns the double:
   
       (def doubler-contract
         (contract doubler
           “Ensures that when given a number,
            the result is doubled.”
           [x] [number? => (= (* 2 x) %)]

           [x y] [(every? number? [x y])
                  =>
                  (= (* 2 (+ x y)) %)]))

   You can then partially apply this contract with an existing function:

       (def doubler
            (partial doubler-contract
                     #(* 2 %)))
       
       (def bad-doubler
            (partial doubler-contract
                     #(* 3 %)))

   And then running these functions will be checked against the contract at runtime:

       (doubler 2)
       ;=> 4
       
       (bad-doubler 2)
       ; java.lang.AssertionError:
       ;   Assert failed: (= (* 2 x) %)

   Similar results would occur for the 2-arity versions of `doubler` and `bad-doubler`.

   While it's fine to use `partial` directly, it's better to use the `with-constraints` function
   found in this same library.

   If you're so inclined, you can inspect the terms of the contract via its metadata, keyed on
   the keyword `:constraints`.
  "
  [n docstring & constraints]
  (if (not (string? docstring))
    (throw (IllegalArgumentException. "Sorry, but contracts require docstrings"))
    (let [raw-cnstr   (partition 2 constraints)
          arity-cnstr (for [[a c] raw-cnstr]
                        (build-constraints-map a c))
          fn-arities  (for [b arity-cnstr]
                        (build-contract docstring b))
          body (list* 'fn n fn-arities)]
      `(with-meta 
         ~body
         {:constraints (into {} '~arity-cnstr)
          :docstring ~docstring}))))

(defn with-constraints
  "A contract combinator.
   
   Takes a target function and a number of contracts and returns a function with the contracts
   applied to the original.  This is the preferred way to apply a contract previously created
   using `contract` as the use of `partial` may not work as implementation details change.
  "
  ([f] f)
  ([f c] (partial c f))
  ([f c & more]
     (apply with-constraints (with-constraints f c) more)))

(defmacro defcontract
  "Convenience macro for defining a named contract.  Equivalent to `(def fc (contract ...))`"
  [name docstring & forms]
  `(def ~name
     (contract ~(symbol (str name "-impl")) ~docstring ~@forms)))

(defmacro defconstrainedfn
  "Defines a function using the `contract` vector appearing after the arguments.

       (defconstrainedfn sqr
         [n] [number? (not= 0 n) => pos? number?]
         (* n n))

   Like the `contract` macro, multiple arity functions can be defined where each argument vector 
   is immediately followed by the relevent arity expectations.  This macro will also detect
   if a map is in that constraints position and use that instead under the assumption that
   Clojure's `:pre`/`:post` map is used instead.
  "
  [name & body]
  (let [mdata (if (string? (first body))
                {:doc (first body)}
                {})
        body  (if (:doc mdata)
                (next body)
                body)
        body  (if (vector? (first body))
                (list body)
                body)
        body  (for [[args cnstr & bd] body]
                (list* args
                       (if (vector? cnstr)
                         (second (build-constraints-map args cnstr))
                         cnstr)
                       bd))]
    `(defn ~name
       ~(str (:doc mdata))
       ~@body)))

(defmacro defconstrainedrecord
  [name slots invariants & etc]
  (let [fields       (->> slots (partition 2) (map first) vec)
        defaults     (->> slots (partition 2) (map second))
        ctor-name    (symbol (str name \.))
        factory-name (symbol (str "->" name))]
    `(do
       (let [t# (defrecord ~name ~fields ~@etc)]
         (defn ~(symbol (str name \?)) [r#]
           (= t# (type r#))))

       (let [chk# (contract ~(symbol (str "chk-" name))
                            ~(str "Invariant contract for " name) 
                            [{:keys ~fields :as m#}] ~invariants)]
         (defconstrainedfn ~factory-name
           ([] [] (with-meta 
                    (~ctor-name ~@defaults)
                    {:contract chk#}))
           ([& {:keys ~fields :as kwargs# :or ~(apply hash-map slots)}]
              ~invariants
              (with-meta
                (-> (~ctor-name ~@defaults)
                    (merge kwargs#))
                {:contract chk#}))))
       ~name)))

(defn- apply-contract
  [f]
  (if (:hooked (meta f))
    f
    (with-meta
      (fn [m & args]
        (if-let [contract (-> m meta :contract)]
          ((partial contract identity) (apply f m args))
          (apply f m args)))
      {:hooked true})))

(when *assert*
  (alter-var-root (var assoc) apply-contract)
  (alter-var-root (var dissoc) apply-contract)
  (alter-var-root (var merge) apply-contract)
  (alter-var-root (var merge-with) (fn [f] (let [mw (apply-contract f)] (fn [f & maps] (apply mw f maps)))))
  (alter-var-root (var into) apply-contract)
  (alter-var-root (var conj) apply-contract)
  (alter-var-root (var assoc-in) apply-contract)
  (alter-var-root (var update-in) apply-contract))

(defmacro defconstrainedtype
  [name slots invariants & etc]
  (let [fields       (vec slots)
        ctor-name    (symbol (str name \.))
        factory-name (symbol (str "->" name))]
    `(do
       (let [t# (deftype ~name ~fields ~@etc)]
         (defn ~(symbol (str name \?)) [r#]
           (= t# (type r#))))
       
       (let [chk# (contract ~(symbol (str "chk-" name))
                            ~(str "Invariant contract for " name) 
                            [{:keys ~fields :as m#}] ~invariants)]
         (defconstrainedfn ~factory-name
           (~fields ~invariants
              (~ctor-name ~@fields))))
       ~name)))

(comment
  (use 'trammel.factors)
  
  (defconstrainedfn leap-year?
    [year],[number? pos? =>]
    (and (= (mod year 4) 0)
         (not (some #{(mod year 400)}
                    [100 200 300]))))

  (leap-year? 111)
  (let [year 0]
    (in (mod year 400)
        100 200 300))

  (defconstrainedfn sqr
    [n],[number? => pos? number?]
    (* n n))

  (sqr 0)
)

