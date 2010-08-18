(ns fogus.me.provide)

(defn keys-apply [f ks m]
  "Takes a function, a set of keys, and a map and applies the function 
   to the map on the given keys.  A new map of the results of the function 
   applied to the keyed entries is returned."
  (let [only (select-keys m ks)] 
    (zipmap (keys only) (map f (vals only)))))

(defn manip-map [f ks m]
  "Takes a function, a set of keys, and a map and applies the function 
   to the map on the given keys.  A modified version of the original map
   is returned with the results of the function applied to each keyed entry."
  (conj m (keys-apply f ks m)))

(def tests [
            '([n] [pos? number? => pos? number?])
            '([a b] [(every? number? [a b]) => pos?])

            '([n] [pos? number?])
            '([a b] [(every? number? [a b]) => pos?])

            '([n] [=> [pos?]])
            '([a b] [(every? number? [a b]) => pos?])

            '([n] [=> [pos?]])

            '([n] [number? => pos?])

            '([n] [pos?])])

(defn build-pre-post-map [cnstr]
  (let [[L M R] (partition-by #{'=>} cnstr)]
    {:pre  (when (not= L '(=>)) L)
     :post (if (= L '(=>)) M R)}))

(defn funcify [args cnstr]
  (vec (map (fn [e]
              (if (symbol? e)
                (list* e args)
                e)) 
            cnstr)))

(defn build-map [args cnstr]
  [args 
   (manip-map (partial funcify '[%])
             [:post]
             (manip-map (partial funcify args) 
                        [:pre] 
                        (build-pre-post-map cnstr)))])

(map (fn [[a c]] (build-map a c)) (take 2 tests))
