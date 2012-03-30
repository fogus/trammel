;;; invariant_tests.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; Sept 16, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.invariant-tests
  (:import (clojure.lang ArityException))
  (:require [clojure.reflect :as reflect])
  (:use [trammel.core :only [defconstrainedrecord defconstrainedtype]])
  (:use [clojure.test :only [deftest is]]))


(defconstrainedrecord AllNumbersRecord [a b]
  [(every? number? [a b])]
  Object
  (toString [this] (str "record AllNumbersRecord has " a " and " b)))

(defconstrainedtype AllNumbersType [a b]
  [(every? number? [a b])])

(deftest test-constrained-record-with-vector-spec
  (is (= (:a (->AllNumbersRecord :a 42 :b 108)) 42))
  (is (= (:b (->AllNumbersRecord :a 42 :b 108)) 108))
  (is (= (:a (->AllNumbersRecord :a 42 :b 108 :c 36)) 42))
  (is (= (:b (->AllNumbersRecord :a 42 :b 108 :c 36)) 108))
  (is (= (:c (->AllNumbersRecord :a 42 :b 108 :c 36)) 36))
  (is (thrown? Error (->AllNumbersRecord)))
  (is (thrown? Error (->AllNumbersRecord :a 12)))
  (is (thrown? Error (->AllNumbersRecord :b 12)))
  (is (thrown? Error (->AllNumbersRecord :a :b)))
  (is (thrown? Error (->AllNumbersRecord :a 42 :b nil))))

(deftest test-constrained-type-with-vector-spec
  (is (= (.a (->AllNumbersType 1 2)) 1))
  (is (= (.b (->AllNumbersType 1 2)) 2))
  (is (thrown? ArityException (->AllNumbersType)))
  (is (thrown? ArityException (->AllNumbersType 1)))
  (is (thrown? Error (->AllNumbersType :a :b))))

;; test constructors without contracts

(defconstrainedrecord NoConstraintRecord [a b]
  [])

(deftest test-record-constructor-with-no-constraints
  (is (= (:a (->NoConstraintRecord)) nil))
  (is (= (:b (->NoConstraintRecord) nil)))
  (is (= (:a (->NoConstraintRecord :a 1)) 1))
  (is (= (:b (->NoConstraintRecord :a 1)) nil))
  (is (= (:a (->NoConstraintRecord :b 1)) nil))
  (is (= (:b (->NoConstraintRecord :b 1)) 1))
  (is (= (:a (->NoConstraintRecord :a 1 :b 2)) 1)))

;; testing default clojure pre/post maps

(defconstrainedrecord Buzz [a b]
  {:pre [(every? number? [a b])]}
  Object
  (toString [this] (str "record Buzz has " a " and " b)))

(deftest test-constrained-record-with-map-spec
  (is (= (:a (->Buzz :a 42 :b 108)) 42))
  (is (= (:b (->Buzz :a 42 :b 108)) 108))
  (is (= (:a (->Buzz :a 42 :b 108 :c 36)) 42))
  (is (= (:b (->Buzz :a 42 :b 108 :c 36)) 108))
  (is (= (:c (->Buzz :a 42 :b 108 :c 36)) 36))
  (is (thrown? Error (->Buzz)))
  (is (thrown? Error (->Buzz :a 12)))
  (is (thrown? Error (->Buzz :b 12)))
  (is (thrown? Error (->Buzz :a :b)))
  (is (thrown? Error (->Buzz :a 42 :b nil))))
