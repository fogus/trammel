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
  ([x]
     :requires
     (number? x)
     (pos? x)
     
     :ensures
     (float? %)
     
     :body
     (float x))
  ([x y]
     :requires
     (every? number? [x y])
     (every? pos?    [x y])
     
     :ensures
     (every? float? %)
     
     :body
     (str "this is a noop, meant to ensure that multi-expr bodies work")
     [(float x) (float y)]))

(deftest multibody-cnstr-fn-test
  (is (= 1.0 (positive-nums 1)))
  (is (= [1.0 2.0] (positive-nums 1 2)))
  (is (= 42 
         (try (positive-nums -1)
              (catch Error e 42))))
  (is (= 42 
         (try (positive-nums -1 2)
              (catch Error e 42))))
  (is (= 42 
         (try (positive-nums 2 -1)
              (catch Error e 42))))
  (is (= 42 
         (try (positive-nums -1 -2)
              (catch Error e 42)))))


(defconstrainedfn sqr
  "Calculates the square of a number."
  [n]
     :requires
     (number? n)
     (not (zero? n))

     :ensures
     (pos? %)

     :body
     (* n n))

(deftest singlebody-cnstr-fn-test
  (is (= 36 (sqr  6)))
  (is (= 36 (sqr -6)))
  (is (= 42 
         (try (sqr 0)
              (catch Error e 42))))
  (is (= 42 
         (try (sqr :a)
              (catch Error e 42)))))

(defconstrainedfn no-doc  [n]
     :requires
     (number? n)
     (not (zero? n))

    :ensures
     (pos? %)

     :body
     (* n n))


(deftest no-doc-cnstr-fn-test
  (is (= 36 (no-doc  6)))
  (is (= 36 (no-doc -6)))
  (is (= 42 
         (try (no-doc 0)
              (catch Error e 42))))
  (is (= 42 
         (try (no-doc :a)
             (catch Error e 42)))))

(deftest defconstrainedfn-test
  (multibody-cnstr-fn-test)
  (singlebody-cnstr-fn-test)
  (no-doc-cnstr-fn-test))
