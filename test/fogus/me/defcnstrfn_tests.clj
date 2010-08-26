;;; defcnstrfn-tests.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; June 04, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.defcnstrfn-tests
  (:use [fogus.me.trammel :only [defconstrainedfn]])
  (:use [clojure.test :only [deftest is]]))

(defconstrainedfn positive-nums
  "test"
  ([x] [(number? x) (pos? x) => (float? %)]
     (float x))
  ([x y] [(every? number? [x y]) (every? pos? [x y]) => (every? float? %)]
     (str "this is a noop, meant to ensure that multi-expr bodies work")
     [(float x) (float y)]))

(deftest multibody-cnstr-fn-test
  (is (= 1.0 (positive-nums 1)))
  (is (= [1.0 2.0] (positive-nums 1 2)))
  (is (thrown? Error (positive-nums -1)))
  (is (thrown? Error (positive-nums -1 2)))
  (is (thrown? Error (positive-nums 2 -1)))
  (is (thrown? Error (positive-nums -1 -2))))

(defconstrainedfn sqr
  "Calculates the square of a number."
  [n] [(number? n) (not (zero? n)) => (pos? %)]
  (* n n))

(deftest singlebody-cnstr-fn-test
  (is (= 36 (sqr  6)))
  (is (= 36 (sqr -6)))
  (is (thrown? Error (sqr 0)))
  (is (thrown? Error (sqr :monkey))))

(defconstrainedfn no-doc-sqr
  [n] [(number? n) (not (zero? n)) => (pos? %)]
  (* n n))

(deftest no-doc-cnstr-fn-test
  (is (= 36 (no-doc-sqr  6)))
  (is (= 36 (no-doc-sqr -6)))
  #_(is (thrown? Error (no-doc-sqr 0)))
  (is (thrown? Error (no-doc-sqr :gibbon))))


(defconstrainedfn sqr-isolated-fns
  "Calculates the square of a number."
  [n] [number? (not (zero? n)) => pos? number?]
  (* n n))

(deftest sqr-isolated-fns-test
  (is (= 36 (sqr-isolated-fns  6)))
  (is (= 36 (sqr-isolated-fns -6)))
  (is (thrown? Error (sqr 0)))
  (is (thrown? Error (sqr :monkey))))
