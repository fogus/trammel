(ns fogus.me.trammel
  (:use [clojure.test :as test]))

(defn- build-contract [c]
  (let [args (first c)]
    (list
     (into '[f] args)
     (apply merge
            (for [con (rest c)]            
              (cond (= (first con) 'requires)
                    (assoc {} :pre (vec (rest con)))
                    (= (first con) 'ensures)
                    (assoc {} :post (vec (rest con)))
                    :else (throw (Exception. (str "Unknown tag " (first con)))))))
     (list* 'f args))))

(defn- collect-bodies [forms]
  (for [form (partition 3 forms)]
    (build-contract form)))

(defmacro contract [& forms]
  (let [name (if (symbol? (first forms))
               (first forms) 
               nil)
        body (collect-bodies (if name
                               (rest forms)
                               forms))]
    (list* 'fn name body)))

(comment
  (def doubler
       (contract doubler 
         [x]
         (requires
          (pos? x))
         (ensures
          (= (* 2 x) %))))

  (def times2 (partial doubler #(* 2 %)))
  (times2 9)
  
  (def times3 (partial doubler #(* 3 %)))
  (times3 9)

  (def doubler-contract
       (contract doubler 
         [x]
         (requires
          (pos? x))
         (ensures
          (= (* 2 x) %))
         [x y]
         (requires
          (pos? x)
          (pos? y))
         (ensures
          (= (* 2 (+ x y)) %))))

  ((partial doubler-contract #(* 2 (+ %1 %2))) 2 3)

  ((partial doubler-contract #(+ %1 %1 %2 %2)) 2 3)

  ((partial doubler-contract #(* 3 (+ %1 %2))) 2 3))
