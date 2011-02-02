(ns fogus.me.factors-test
  (:use [trammel.core :only (contract)])
  (:use trammel.factors)
  (:use [clojure.test :only (deftest is are)]))

(deftest in-test
  (are [expr] (true? expr)
       (in 5 [1 10])
       (in 1 2 3 4 5 6 7 1)
       (in 7 1 2 3 4 [5 10] 20)
       (in :a :b :c :a :d)
       (not (in 1 [2 10]))
       (not (in :a :b :c :d))
       (not (in 700 [1 700] [701 800]))))

(deftest all-numbers-test
  (are [expr] (true? expr)
    (all-numbers? 1 2 3 4 5 6 7)
    (all-numbers? 10 20 3 4 5 6 7)
    (all-numbers? 1 2 3.0 4.5 1/2 1.0e7 2.0e-7 100M -66666)
    (apply all-numbers? (range 100))
    (not (all-numbers? 1 2 3 4 5 6 7 :a 9 10))
    (not (all-numbers? 1 2 3 nil 9 10))))

(deftest all-positive-test
  (are [expr] (true? expr)
    (all-positive? 1 2 3 4 5 6 7)
    (all-positive? 10 20 3 4 5 6 7)
    (all-positive? 1 2 3.0 4.5 1/2 1.0e7 2.0e-7 100M 66666)
    (apply all-positive? (range 1 100))
    (not (all-positive? 1 2 3 4 5 6 7 :a 9 10))
    (not (all-positive? 1 2 3 nil 9 10))
    (not (all-positive? 0 1 2 3 9 10))))

(deftest all-negative-test
  (are [expr] (true? expr)
    (all-negative? -1 -2 -3 -4 -5 -6 -7)
    (all-negative? -10)
    (all-negative? -1 -2 -3.0 -4.5 -1/2 -1.0e7 -2.0e-7 -100M -66666 -0.0)
    (apply all-negative? (map - (range 1 100)))
    (not (all-negative? 1 2 3 4 5 6 7 :a 9 10))
    (not (all-negative? 1 2 3 nil 9 10))
    (not (all-negative? 0 1 2 3 9 10))))

(deftest anything-test
  (are [expr] (true? expr)
    (anything 1)
    (anything #{})
    (anything nil)
    (anything false)))

(deftest truthy-test
  (are [expr] (true? expr)
    (truthy 1)
    (truthy #{})
    (truthy #(nil? 1))
    (truthy [])
    (truthy "")
    (truthy (Boolean. false))
    (not (truthy nil))
    (not (truthy false))))

(deftest falsey-test
  (are [expr] (true? expr)
    (not (falsey 1))
    (not (falsey #{}))
    (not (falsey #(nil? 1)))
    (not (falsey []))
    (not (falsey ""))
    (not (falsey (Boolean. false)))
    (falsey nil)
    (falsey false)))