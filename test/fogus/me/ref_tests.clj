;;; ref_tests.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; Mar 30, 2012

;; Copyright (c) Michael Fogus, 2010-2012. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.ref-tests
  (:use [trammel.core])
  (:use [clojure.test :only [deftest is]]))

(deftest test-atom-constraints
  (let [a (constrained-atom 0
            "only numbers allowed"
            [number?])]
    (is (= 0 @a))
    (is (= 1 (swap! a inc)))
    (is (thrown? Error (do (swap! a str) @a)))
    (is (= 2 (do (compare-and-set! a 1 2) @a)))
    (is (thrown? Error (compare-and-set! a 1 :a)))))

(deftest test-ref-constraints
  (let [a (constrained-ref 0
            "only numbers allowed"
            [number?])]
    (is (= 0 @a))
    (is (= 1 (dosync (alter a inc))))
    (is (thrown? Error (dosync (alter a str))))
    (is (= 0 (dosync (ref-set a 0))))
    (is (thrown? Error (dosync (ref-set a :a))))))

(defconstrainedvar ^:dynamic foo 0
  "only numbers allowed"
  [number?])

(deftest test-var-constraints
  (is (= 0 foo))
  (is (= 42 (binding [foo 42] foo)))
  (is (thrown? Error (binding [foo :a] foo))))

(deftest test-agent-constraints
  (let [a (constrained-agent 0
            "only numbers allowed"
            [number?])]
    (is (= 0 @a))
    (is (= 1 (do (send a inc) (await-for 2000 a) @a)))
    (is (= AssertionError (do (send a str) (await-for 2000 a) (class (agent-error a)))))))
