;;; defcontract_tests.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; June 04, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.defcontract-tests
  (:use [fogus.me.trammel :only [defcontract]])
  (:use [clojure.test :only [deftest is]]))

(defcontract 
  doubler-defcontract
  [n]
  :requires 
  (number? n)
  
  :ensures
  (= % (* 2 n))

  [x y]
  :requires
  (every? number? [x y])
  
  :ensures
  (= (* 2 (+ x y)) %))

(deftest doubler-defcontract-test
  (is (= 10 ((partial doubler-defcontract #(* 2 (+ %1 %2))) 2 3)))
  (is (= 10 ((partial doubler-defcontract #(+ %1 %1 %2 %2)) 2 3)))
  (is (= 10 ((partial doubler-defcontract #(* 2 %)) 5)))
  (is (thrown? Error ((partial doubler-defcontract #(* 3 (+ %1 %2))) 2 3))))
