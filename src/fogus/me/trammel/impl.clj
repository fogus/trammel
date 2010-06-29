;;; impl.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; June 1, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.trammel.impl)

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
  (apply merge
         (for [[[dir] & [cnstr]] (->> expectations
                                      (partition-by #{:requires :ensures})
                                      (partition 2))] 
           {(case dir
                  :requires :pre
                  :ensures  :post)
            (vec cnstr)})))

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
  [[[sig] expectations :as c]]
  (list 
    (into '[f] sig)
    (build-constraints-map expectations)
    (list* 'f sig)))

(defn collect-bodies
  "Where the magic happens.  Takes a list representing the idependent constraints for 
   an unspecified function of the form:

    ([x] :requires (foo x) :ensures (bar %) 
     [x y] :requires (baz x) (quux y) :ensures (blip %))

   Taking this form `collect-bodies` then partitions it in such a way as to identify
   the arity-based canstraints and pass each on to `build-contract` which then 
   returns the HOF body for each arity.
  "
  [forms]
  (for [body (->> (partition-by vector? forms)
                  (partition 2))]
    (build-contract body)))

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
