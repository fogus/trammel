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
  (:use [trammel.core :only [defconstrainedrecord defconstrainedtype]])
  (:use [clojure.test :only [deftest is]]))

(defconstrainedrecord AllNumbersRecord [a b]
  "AllNumbersRecord record fields are expected to hold only numbers."
  [(every? number? [a b])]
  Object
  (toString [this] (str "record AllNumbersRecord has " a " and " b)))

(defconstrainedtype AllNumbersType [a b]
  "AllNumbersType type fields are expected to hold only numbers."
  [(every? number? [a b])])

(deftest test-constrained-record-with-vector-spec
  (is (= (:a (->AllNumbersRecord 42 108)) 42))
  (is (= (:b (->AllNumbersRecord 42 108)) 108))
  (is (thrown? Exception (->AllNumbersRecord)))
  (is (thrown? Exception (->AllNumbersRecord 12))))

(defconstrainedtype Bar [a b]
  "Bar type fields are expected to hold only numbers."
  [(every? number? [a b])])

(deftest test-constrained-type-with-vector-spec
  (is (= (.a (->AllNumbersType 1 2)) 1))
  (is (= (.b (->AllNumbersType 1 2)) 2))
  (is (thrown? Exception (->AllNumbersType)))
  (is (thrown? Exception (->AllNumbersType 1)))
  (is (thrown? Error (->AllNumbersType :a :b))))

;; testing default clojure pre/post maps

(defconstrainedrecord Buzz [a b]
  "Baz record fields are expected to hold only numbers."
  {:pre [(every? number? [a b])]}
  Object
  (toString [this] (str "record Buzz has " a " and " b)))

(deftest test-constrained-record-with-map-spec
  (is (= (:a (->Buzz 42 108)) 42))
  (is (= (:b (->Buzz 42 108)) 108))
  (is (nil? (merge)))
  (is (thrown? Exception (->Buzz)))
  (is (thrown? Exception (->Buzz 12))))

; map->* factory

(deftest test-map-factory-for-defconstrainedrecord
  (is (= (:a (map->Buzz {:a 1 :b 2})) 1))
  (is (= (:b (map->Buzz {:a 1 :b 2})) 2))
  (is (= (:c (map->Buzz {:a 1 :b 2 :c "a"})) "a"))
  (is (thrown? Error (map->Buzz {:a nil})) "a"))
