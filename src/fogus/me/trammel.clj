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

(defn build-constraints-map
  "Takes a list of the contract expectation bodies for each arity, of the form:

    (:requires (foo x) (bar x) :ensures (baz %))

   It then takes this form and builds a pre- and post-condition map of the form:

    {:pre  [(foo x) (bar x)]
     :post [(baz %)]}

   At the moment this function expects that the constraint functions are explicitly
   wrapped in a list with the argument(s) likewise explicit.
  "
  [expectations]
  (merge {}
         (when (:requires expectations)
           {:pre (:requires expectations)})
         (when (:ensures expectations)
           {:post (:ensures expectations)})))

(defn build-contract 
  "Expects a list representing an arity-based expectation of the form:

    (([x]) (:requires (foo x) :ensures (bar %)))

   This form is then destructured to pull out the arglist `[x]` and the
   contract expectation body (i.e. the constraints):

    (:requires (foo x) :ensures (bar %))

   It then uses this data to build another list reprsenting a specific arity body
   for a higher-order function with attached pre- and post-conditions that directly 
   calls the function passed in:

    ([f x] {:pre [(foo x)] :post [(bar %)]} (f x))
  "
  [arity-map]
  (let [sig (first (keys arity-map))]
    (list 
     (into '[f] sig)
     (build-constraints-map (first (vals arity-map)))
     (list* 'f sig))))

(defn collect-bodies
  "Where the magic happens.  Takes a list representing the idependent constraints for 
   an unspecified function of the form:

    ([x] :requires (foo x) :ensures (bar %) 
     [x y] :requires (baz x) (quux y) :ensures (blip %))

   Taking this form `collect-bodies` then partitions it in such a way as to identify
   the arity-based canstraints and pass each on to `build-contract` which then 
   returns the HOF body for each arity.
  "
  [arity-maps]
  (for [amap arity-maps]
    (build-contract amap)))


(defn build-forms-map
  "Works similarly to the `collect-bodies` function *except* for two differences:
   1. Recognizes and processes a `:body` element
   2. Requires that multi-arity functions *must* be enclosed in sublists similar to 
      Clojure's default `fn` form.

   The reason for the former is that the `:body` element refers to the body of the
   generated function.  Without arity-sepcific body sublists there would be no way to 
   determine when a body ends and another arity constraint specification begins.  This
   should be no problem since Clojurist are already accustomed to the multi-arity `fn` 
   form.  

   This function expects a list of the form:

    ([x] :requires (foo x) (bar n) :ensures (baz %) :body (println x) (frob x))

   or

    (([x] :requires (foo x) :ensures (baz %) :body (frob x)) 
     ([x y] :requires (foo x y) :ensures (baz %) :body (frob x y)))

   And will return a map keyed by arity vector of the form:

    {[x] {(:requires) ((foo x)), (:body) ((frob x)), (:ensures) ((baz %))}}

   Which can then be used to build a real function definition by looking up the
   constraints and body for each arity.
  "
  [forms]
  (for [[[args] & c] (map #(partition-by #{:requires :ensures :body} %)
                       (if (vector? (first forms)) 
                         (list forms) 
                         forms))]
    {args (let [m (apply array-map c)
                ks (map first (keys m))
                vs (vals m)]
            (zipmap ks (map (fn [k vs]
                              (map (fn [v]
                                     (if (symbol? v)
                                       (cond
                                        (= k :requires) (apply list v args)
                                        (= k :ensures)  (apply list v '[%])
                                        :default        v)
                                       v))
                                   vs))
                            ks
                            vs)))}))

(defmacro contract
  "The base contract form returning a higher-order function that can then be partially
   applied to an existing function to 'apply' a contract.  Take for example a simple
   contract that describes an expectation for a function that simply takes one or two
   numbers and returns the double:
   
    (def doubler-contract
       (contract doubler
         [x]
         :requires
         (number? x)
    
         :ensures
         (= (* 2 x) %)
    
         [x y]
         :requires
         (every? number? [x y])
           
         :ensures
         (= (* 2 (+ x y)) %)))

   You can then partially apply this contract with an existing function:

    (def doubler (partial doubler-contract #(* 2 %)))
    (def bad-doubler (partial doubler-contract #(* 3 %)))

   And then running these functions will be checked against the contract at runtime:

    (doubler 2)
    ;=> 4

    (bad-doubler 2)
    ; java.lang.AssertionError: Assert failed: (= (* 2 x) %)

   Similar results would occur for the 2-arity versions of `doubler` and `bad-doubler`.

   While it's fine to use `partial` directly, it's better to use the `with-constraints` macro
   found in this same library.

   If you're so inclined, you can inspect the terms of the contract via its metadata, keyed on
   the keyword `:constraints`.
  "
  [& forms]
  (let [name (when (symbol? (first forms))
               (first forms))
        arity-maps (build-forms-map
                    (map #(mapcat identity %) 
                         (partition 2
                                    (partition-by
                                     vector?
                                     (if name
                                       (rest forms)
                                       forms)))))]
    (list `with-meta 
          (list* `fn (if name name (gensym))
                 (collect-bodies arity-maps))
          `{:constraints (into {} '~arity-maps)})))

(defmacro with-constraints
  "Takes a target function and a number of contracts and returns a function with the contracts
   applied to the original.  This is the preferred way to apply a contract previously created
   using `contract` as the use of `partial` may not work as implementation details change.
  "
  ([f] f)
  ([f contract] (list `partial contract f))
  ([f contract & more]
     `(with-constraints (with-constraints ~f ~contract) ~@more)))

(defmacro defcontract
  "Convenience function for defining a named contract.  Equivalent to `(def fc (contract ...))`"
  [name & forms]
  `(def ~name
     (contract ~@forms)))

(defmacro defconstrainedfn
  "Defines a function using the `contract` scheme with an additional `:body` element.

    (defconstrainedfn sqr
      \"Squares a number\"
      [n]
      :requires
      (number? n)
      (not= 0 n)

      :ensures
      (pos? %)
      (number? %)

      :body
      (* n n))

   The order of the `:requires`, `:ensures`, and `:body` sections are not relevant, but it's likely a
   good idea to maintain consistency.  Like the `contract` macro, multiple arity functions can be defined
   where each argument vector is followed by the relevent arity expectations.  The `:body` element is the
   only required section for each defined function arity.
  "
  [name & body]
  (let [mdata (if (string? (first body))
                {:doc (first body)}
                {})
        body  (if (:doc mdata)
                (next body)
                body)
        body  (for [bd (build-forms-map body)] 
                (let [arg (first (keys bd))
                      b   (first (vals bd))]
                  (list* arg
                         {:pre  (vec (b :requires))
                          :post (vec (b :ensures))}
                         (b :body))))]
    `(defn ~name
       ~(if (:doc mdata) (:doc mdata) "")
       ~@body)))

(defmacro apply-contracts [& contracts]
  (let [fn-names  (map first contracts)
        contracts (for [c contracts] (if (vector? (second c)) (list* `contract c) (second c)))]
    `(do
       ~@(for [[n# c#] (zipmap fn-names contracts)]
           (list `alter-var-root (list `var n#) 
                 (list `fn '[f c] (list `with-constraints 'f 'c)) c#))
       nil)))

(defmacro defconstrainedrecord
  [name slots & etc]
  (let [fields   (->> slots (partition 2) (map first) vec)
        defaults (->> slots (partition 2) (map second))
        fmap     (first (build-forms-map (list* '[] etc)))
        body     (:body (fmap []))
        reqs     (:requires (fmap []) '(()))
        ens      (:ensures (fmap []) '(()))]
    `(do
       (defrecord ~name
         ~fields
         ~@body)
       (defconstrainedfn ~(symbol (str "new-" name))
         [& {:keys ~fields :as kwargs#}]
         :requires ~@reqs
         :ensures  ~@ens
         :body
         (-> (~(symbol (str name \.)) ~@defaults)
             (merge kwargs#)))
       ~name)))

(comment
  (defconstrainedrecord Foo [a 1 b 2]
    :requires (every? number? [a b])
    :body
    Object
    (toString [this] (str "record Foo has " a " and " b)))

  (str (Foo. 3 4))
  (str (new-Foo :a 3 :b 5))

  (macroexpand '(defconstrainedrecord Foo [a b]
    :requires (every? number? [a b])
    :body
    Object
    (toString [this] (str "record Foo has " a " and " b))))
)
