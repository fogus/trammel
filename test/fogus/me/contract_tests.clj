;;; defcontract_tests.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; May 26, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.contract-tests
  (:use [fogus.me.trammel :only [contract]])
  (:use [clojure.test :only [deftest is]]))

(def doubler-contract-full
     (contract doubler 
       [x]
       :requires
       (pos? x)
       :ensures
       (= (* 2 x) %)

       [x y]
       :requires
       (pos? x)
       (pos? y)
       :ensures
       (= (* 2 (+ x y)) %)))

(deftest doubler-contract-test
  (is (= 10 ((partial doubler-contract-full #(* 2 (+ %1 %2))) 2 3)))
  (is (= 10 ((partial doubler-contract-full #(+ %1 %1 %2 %2)) 2 3)))
  (is (= 10 ((partial doubler-contract-full #(* 2 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-full #(* 3 (+ %1 %2))) 2 3))))

(def doubler-contract-full-and-isolated-fn
     (contract doubler 
       [x]
       :requires
       pos?
       :ensures
       (= (* 2 x) %)

       [x y]
       :requires
       (pos? x)
       (pos? y)
       :ensures
       (= (* 2 (+ x y)) %)))

(deftest doubler-contract-full-and-isolated-fn-test
  (is (= 10 ((partial doubler-contract-full-and-isolated-fn #(* 2 (+ %1 %2))) 2 3)))
  (is (= 10 ((partial doubler-contract-full-and-isolated-fn #(+ %1 %1 %2 %2)) 2 3)))
  (is (= 10 ((partial doubler-contract-full-and-isolated-fn #(* 2 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-full-and-isolated-fn #(* 3 (+ %1 %2))) 2 3))))


(def doubler-contract-arity1
     (contract doubler 
       [x]
       :requires
       (pos? x)
       :ensures
       (= (* 2 x) %)))

(deftest doubler-contract-arity1-test
  (is (= 10 ((partial doubler-contract-arity1 #(* 2 %)) 5)))
  (is (= 10 ((partial doubler-contract-arity1 #(* 2 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-arity1 #(* 3 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-arity1 #(* 2 %)) -5))))

(def doubler-contract-arity1-and-isolated-fn
     (contract doubler 
       [x]
       :requires
       pos?
       :ensures
       (= (* 2 x) %)))

(deftest doubler-contract-arity1-and-isolated-fn-test
  (is (= 10 ((partial doubler-contract-arity1-and-isolated-fn #(* 2 %)) 5)))
  (is (= 10 ((partial doubler-contract-arity1-and-isolated-fn #(* 2 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-arity1-and-isolated-fn #(* 3 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-arity1-and-isolated-fn #(* 2 %)) -5))))


(def doubler-contract-no-requires
     (contract doubler 
       [x]
       :ensures
       (= (* 2 x) %)))

(deftest doubler-contract-no-requires-test
  (is (= 10 ((partial doubler-contract-no-requires #(* 2 %)) 5)))
  (is (= -10 ((partial doubler-contract-no-requires #(* 2 %)) -5)))
  (is (thrown? Error ((partial doubler-contract-no-requires #(* 3 %)) 5))))


(def doubler-contract-no-requires-and-isolated-fn
     (contract doubler 
       [x]
       :ensures
       pos?
       (= (* 2 x) %)))

(deftest doubler-contract-no-requires-and-isolated-fn-test
  (is (= 10 ((partial doubler-contract-no-requires-and-isolated-fn #(* 2 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-no-requires-and-isolated-fn #(* 2 %)) -5)))
  (is (thrown? Error ((partial doubler-contract-no-requires-and-isolated-fn #(* 3 %)) 5))))


(def doubler-contract-no-ensures
     (contract doubler 
       [x]
       :requires
       (pos? x)))

(deftest doubler-contract-no-ensures-test
  (is (= 10 ((partial doubler-contract-no-ensures #(* 2 %)) 5)))
  (is (= 15 ((partial doubler-contract-no-ensures #(* 3 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-no-ensures #(* 2 %)) -5))))


(def doubler-contract-no-ensures-and-isolated-fn
     (contract doubler 
       [x]
       :requires
       pos?))

(deftest doubler-contract-no-ensures-and-isolated-fn-test
  (is (= 10 ((partial doubler-contract-no-ensures-and-isolated-fn #(* 2 %)) 5)))
  (is (= 15 ((partial doubler-contract-no-ensures-and-isolated-fn #(* 3 %)) 5)))
  (is (thrown? Error ((partial doubler-contract-no-ensures-and-isolated-fn #(* 2 %)) -5))))
