(ns trammel.factors
  "Provides some common argument checkers and factorings.")

;; # constraint functions and multimethods

(def all-numbers?  #(every? number? %&))
(def all-positive? #(and (apply all-numbers? %&) (every? pos? %&)))
(def all-negative? #(and (apply all-numbers? %&) (every? (complement pos?) %&)))
(defn anything [& _] true)

(defn in [e & args] (some #{e} (mapcat #(if (vector? %) 
                                          (apply range %) 
                                          [%]) 
                                       args)))
