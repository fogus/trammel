; provide_contracts_tests.clj

; by Michael Fogus - http://fogus.me/fun/
; May 26, 2010

; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
; and distribution terms for this software are covered by the Eclipse
; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file COPYING the root of this
; distribution.  By using this software in any fashion, you are
; agreeing to be bound by the terms of this license.  You must not
; remove this notice, or any other, from this software.

(ns fogus.me.provide-contracts-tests
  (:use [trammel.core :only [defcontract]])
  (:require [trammel.provide :as provide])
  (:use [clojure.test :only [deftest is]]))

(defn sqr [n]
  (* n n))

(provide/contracts
  [sqr "The constraining of sqr" [n] [number? (not= 0 n) => pos? number?]])

(deftest apply-contracts-test
  (is (= 25 (sqr 5)))
  (is (= 25 (sqr -5)))
  (is (thrown? AssertionError (sqr 0))))

(defn sqr2 [n]
  (* n n))

(defcontract sqr-contract
  "Defines the constraints on squaring."
  [n] [number? (not= 0 n) => pos? number?])

(provide/contracts
  [sqr2 "Apply the contract for squaring" sqr-contract])

(deftest apply-existing-contract-test
  (is (= 25 (sqr2 5)))
  (is (= 25 (sqr2 -5)))
  (is (thrown? AssertionError (sqr2 0))))

(defn times2
  ([x] (* 2 x))
  ([x y] (* y x 2)))

(provide/contracts
 [times2 "The constraining of times2"
  [n]   [number? => number? (== % (* 2 n))]
  [x y] [(every? number? [x y]) => number? (== % (* x y 2))]])

(deftest apply-contract-arity2-test
  (is (= 10 (times2 5)))
  (is (= -10 (times2 -5)))
  (is (thrown? AssertionError (times2 :a)))
  (is (= 20 (times2 2 5)))
  (is (= -20 (times2 -5 2)))
  (is (thrown? AssertionError (times2 5 :a))))
