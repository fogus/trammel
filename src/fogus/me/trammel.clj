(ns fogus.me.trammel
  (:require '[clojure.test :as test]))

(defn- build-contract [c]
  (let [args (first c)]
    (list
     (into '[f] args)
     (apply merge
            (for [con (rest c)]            
              (cond (= (first con) 'require)
                    (assoc {} :pre (vec (rest con)))
                    (= (first con) 'ensure)
                    (assoc {} :post (vec (rest con)))
                    :else (throw (Exception. (str "Unknown tag " (first con)))))))
     (list* 'f args)))) 

(defn- collect-bodies [forms]
  (for [form (partition 3 forms)]
    (build-contract form)))

(defmacro defcontract [& forms]
  (let [name (if (symbol? (first forms))
               (first forms) 
               nil)
        body (collect-bodies (if name
                               (rest forms)
                               forms))]
    (list* 'fn name body)))
