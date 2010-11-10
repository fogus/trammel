;;; trammel.clj -- Contracts programming library for Clojure

;; by Michael Fogus - <http://fogus.me/fun/trammel>
;; May 26, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.trammel)

;; constraint functions and multimethods

(def all-numbers?  #(every? number? %&))
(def all-positive? #(and (apply all-numbers? %&) (every? pos? %&)))
(def all-negative? #(and (apply all-numbers? %&) (every? (complement pos?) %&)))
(defn anything [& _] true)

(defn in [e & args] (some #{e} (mapcat #(if (vector? %) 
                                          (apply range %) 
                                          [%]) 
                                       args)))

;; base functions and macros

(defn- keys-apply [f ks m]
  "Takes a function, a set of keys, and a map and applies the function to the map on the given keys.  
   A new map of the results of the function applied to the keyed entries is returned.
  "
  (let [only (select-keys m ks)] 
    (zipmap (keys only) (map f (vals only)))))

(defn- manip-map [f ks m]
  "Takes a function, a set of keys, and a map and applies the function to the map on the given keys.  
   A modified version of the original map is returned with the results of the function applied to each 
   keyed entry.
  "
  (conj m (keys-apply f ks m)))

(defn- build-pre-post-map
  "Takes a vector of the form `[pre ... => post ...]` and infers the expectations described
   therein.  The map that comes out will look like Clojure's default pre- and post-conditions
   map.
  "
  [cnstr]
  (let [[L M R] (partition-by #{'=>} cnstr)]
    {:pre  (when (not= L '(=>)) L)
     :post (if (= L '(=>)) M R)}))

(declare funcify)
(defmulti funcify* (fn [e _] (class e)))

(defmethod funcify* clojure.lang.IFn        [e args] (list* e args))
(defmethod funcify* java.util.regex.Pattern [e args] (list* 'clojure.core/re-matches e args))
(defmethod funcify* java.lang.String        [e args] (list* 'clojure.core/= e args))
(defmethod funcify* java.lang.Number        [e args] (list* 'clojure.core/= e args))
(defmethod funcify* :default                [e args] (case (first e) 
                                                       'or (list* 'or (funcify args (rest e)))
                                                       'in (concat (list* 'in args) (rest e))
                                                       e))

(defn- funcify
  "Performs the *magic* of the Trammel syntax.  That is, it currently identifies isolated functions and
   wraps them in a list with the appropriate args.  It also recognizes keywords and does the same under 
   the assumption that a map access is required.  It then returns the vector of calls expected by the
   Clojure pre- and post-conditions map."
  [args cnstr]
  (vec (map #(funcify* % args) cnstr)))

(defn- build-constraints-map 
  "Takes the corresponding arglist and a vector of the contract expectations, the latter of which looks 
   like any of the following:

    [(= 0 _)] or [number?] ;; lists only the pre-condition
    [number? => number?]   ;; lists a pre- and post-condition
    [=> number?]           ;; lists only a post-condition
    [foo bar => baz]       ;; lists a pre- and post-condition

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
  "
  [cnstr]
  (let [[args pre-post-map] cnstr]
    (list (into '[f] args)
          pre-post-map
          (list* 'f (mapcat (fn [item]
                              (cond (symbol? item) [item]
                                    (map? item) [(:as item)]
                                    :else [item]))
                            args)))))

(defmacro contract
  "The base contract form returning a higher-order function that can then be partially
   applied to an existing function to 'apply' a contract.  Take for example a simple
   contract that describes an expectation for a function that simply takes one or two
   numbers and returns the double:
   
    (def doubler-contract
       (contract doubler
         [x] [number? => (= (* 2 x) %)]

         [x y] [(every? number? [x y]) => (= (* 2 (+ x y)) %)]))

   You can then partially apply this contract with an existing function:

    (def doubler (partial doubler-contract #(* 2 %)))
    (def bad-doubler (partial doubler-contract #(* 3 %)))

   And then running these functions will be checked against the contract at runtime:

    (doubler 2)
    ;=> 4

    (bad-doubler 2)
    ; java.lang.AssertionError: Assert failed: (= (* 2 x) %)

   Similar results would occur for the 2-arity versions of `doubler` and `bad-doubler`.

   While it's fine to use `partial` directly, it's better to use the `with-constraints` function
   found in this same library.

   If you're so inclined, you can inspect the terms of the contract via its metadata, keyed on
   the keyword `:constraints`.
  "
  [name docstring & constraints]
  (let [raw-cnstr   (partition 2 constraints)
        arity-cnstr (for [[a c] raw-cnstr]
                      (build-constraints-map a c))
        fn-arities  (for [b arity-cnstr]
                      (build-contract b))]
    (list `with-meta 
          (list* `fn name fn-arities)
          `{:constraints (into {} '~arity-cnstr)
            :docstring ~docstring})))

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
  "Defines a function using the `contract` scheme with an additional `:body` element.

    (defconstrainedfn sqr
      \"Squares a number\"
      [n] [number? (not= 0 n) => pos? number?]
      (* n n))

   Like the `contract` macro, multiple arity functions can be defined where each argument vector 
   is immediately followed by the relevent arity expectations.
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
                       (second (build-constraints-map args cnstr))
                       bd))]
    `(defn ~name
       ~(if (:doc mdata) (:doc mdata) "")
       ~@body)))

(defmacro provide-contracts [& contracts]
  (let [fn-names  (map first contracts)
        contracts (for [[n ds & more] contracts] 
                    (if (vector? (first more)) 
                      (list* `contract n ds more) 
                      (first more)))]
    `(do
      ~@(for [[n# c#] (zipmap fn-names contracts)]
          (list `alter-var-root (list `var n#) 
                (list `fn '[f c] (list `with-constraints 'f 'c)) c#))
      nil)))

(defmacro defconstrainedrecord
  [name slots invariants & etc]
  (let [fields       (->> slots (partition 2) (map first) vec)
        defaults     (->> slots (partition 2) (map second))
        ctor-name    (symbol (str name \.))
        factory-name (symbol (str "new-" name))]
    `(do
       (defrecord ~name
         ~fields
         ~@etc)
       (let [chk# (contract ~(symbol (str "chk-" name))
                            (str "Invariant contract for " (str ~name)) 
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

(defn- apply-contract [f]
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


(comment 
  (defconstrainedrecord Foo [a 1 b 2]
    [(every? number? [a b])]
    Object
    (toString [this] (str "record Foo has " a " and " b)))
  
  (meta (:contract (meta (new-Foo))))
  
  (assoc (new-Foo) :a :zz)

  (str (new-Foo :a 77))
)
