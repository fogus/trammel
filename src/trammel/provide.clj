(ns trammel.provide
  "Provides the Var manipulation macro offering ex post facto application of contracts
   to existing functions."
  (:use  [fogus.me.trammel :only (with-constraints contract)]))

(defmacro contracts
  [& kontracts]
  (let [fn-names  (map first kontracts)
        kontracts (for [[n ds & more] kontracts] 
                    (if (vector? (first more)) 
                      (list* `contract n ds more) 
                      (first more)))]
    `(do
       ~@(for [[n# c#] (zipmap fn-names kontracts)]
           (list `alter-var-root (list `var n#) 
                 (list `fn '[f c] (list `with-constraints 'f 'c)) c#))
       nil)))
