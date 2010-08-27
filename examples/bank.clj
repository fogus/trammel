(ns bank
  (:use [fogus.me.trammel :only [provide-contracts anything]]))

(def amount (ref 0))
(defn deposit [a] (dosync (alter amount + a)))
(defn balance [] @amount)

(provide-contracts
 [deposit "Defines the contract for a bank deposit"
   [a] [number? => anything]]

 [balance "Defines the contarct for a balance check"
   [ ] [=> number?]])



(comment
  (deposit 100)
  ;=> 100
  
  @amount
  ;=> 100

  (balance)
  ;=> 100

  (deposit :a)
  ; java.lang.AssertionError: Assert failed: (number? a)
)
