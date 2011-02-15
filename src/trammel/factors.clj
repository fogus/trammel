(ns trammel.factors
  "Provides some common argument checkers and factorings."
  (:require [clojure.set  :as set]))

;; # constraint functions and multimethods

(def all-numbers?  #(boolean (every? number? %&)))
(def all-positive? #(boolean (and (apply all-numbers? %&) (every? pos? %&))))
(def all-negative? #(boolean (and (apply all-numbers? %&) (every? (complement pos?) %&))))
(defn anything [& _] true)

(defn in
  "Takes an item and determines if it falls in the listed args.  This can be
   used most effectively for numbers since any numbers in a vector represent
   a range of values determined by the same arguments as given to `range`."
  [e & args]
  (boolean
   (some #{e}
         (mapcat #(if (vector? %) 
                    (apply range %) 
                    [%]) 
                 args))))

(def truthy #(when % true))
(def falsey #(not (truthy %)))

(defn whitelist
  "Takes a thing with keys (i.e. maps or sets) and checks if it contains only
   the keys listed in the given whitelist."
  [wl things]
  (set/subset? (set (keys things))
               (set wl)))

(defn implies
  "Logical implication"
  [p q]
  (or (not p) q))

(defn <-
  "Converse implication"
  [p q]
  (implies q p))

(defn except
  "P except Q"
  [p q]
  (not (implies p q)))

(defn <=>
  "Logical equality"
  [p q]
  (and (implies p q)
       (<- p q)))

(defn xor
  "Exclusive or"
  [p q]
  (not (<=> p q)))
