;;; trammel.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; May 26, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.trammel
  (:use fogus.me.trammel.impl))

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
  "
  [& forms]
  (let [name (if (symbol? (first forms))
               (first forms) 
               (gensym))]
    (list* 'fn name 
      (collect-bodies 
       (if (symbol? (first forms))
         (rest forms)
         forms)))))

(defmacro with-constraints
  "Takes a target function and a number of contracts and returns a function with the contracts
   applied to the original.  This is the preferred way to apply a contract previously created
   using `contract` as the use of `partial` may not work as implementation details change.
  "
  ([f] f)
  ([f contract] (list 'partial contract f))
  ([f contract & more]
     `(with-contracts (with-contracts ~f ~contract) ~@more)))

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
                         {:pre  (vec (b '(:requires)))
                          :post (vec (b '(:ensures)))}
                         (b '(:body)))))]
    `(defn ~name
       ~(if (:doc mdata) (:doc mdata) "")
       ~@body)))
