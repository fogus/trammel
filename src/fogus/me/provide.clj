(ns fogus.me.provide)

(defn- keys-apply [f ks m]
  "Takes a function, a set of keys, and a map and applies the function 
   to the map on the given keys.  A new map of the results of the function 
   applied to the keyed entries is returned."
  (let [only (select-keys m ks)] 
    (zipmap (keys only) (map f (vals only)))))

(defn- manip-map [f ks m]
  "Takes a function, a set of keys, and a map and applies the function 
   to the map on the given keys.  A modified version of the original map
   is returned with the results of the function applied to each keyed entry."
  (conj m (keys-apply f ks m)))

(defn- build-pre-post-map [cnstr]
  (let [[L M R] (partition-by #{'=>} cnstr)]
    {:pre  (when (not= L '(=>)) L)
     :post (if (= L '(=>)) M R)}))

(defn- funcify [args cnstr]
  (vec (map (fn [e]
              (if (or (symbol? e) (keyword? e))
                (list* e args)
                e)) 
            cnstr)))

(defn- build-constraints-map [args cnstr]
  [args 
   (->> (build-pre-post-map cnstr)
        (manip-map (partial funcify '[%]) [:post])
        (manip-map (partial funcify args) [:pre]))])

(defn- build-contract 
  [cnstr]
  (let [[args pre-post-map] cnstr]
    (list (into '(f) args)
          pre-post-map
          (list* 'f args))))

(defmacro contract
  [name docstring & constraints]
  (let [raw-cnstr   (->> (partition-by vector? constraints) 
                         (partition 2))
        arity-cnstr (for [[[a] c] raw-cnstr]
                      (build-constraints-map a c))
        fn-arities  (for [b arity-cnstr]
                      (build-contract b))]
    (list `with-meta 
          (list* `fn name fn-arities)
          `{:constraints (into {} '~arity-cnstr)})))

(defn with-constraints
  ([f] f)
  ([f c] (partial c f))
  ([f c & more]
     (apply with-constraints (with-constraints f c) more)))


(defmacro apply-contracts [& contracts]
  (let [fn-names  (map first contracts)
        contracts (for [c contracts] (if (vector? (second c)) (list* `contract c) (second c)))]
    `(do
      ~@(for [[n# c#] (zipmap fn-names contracts)]
          (list `alter-var-root (list `var n#) 
                (list `fn '[f c] (list `with-constraints 'f 'c)) c#))
      nil)))

(comment
(defn sqr [n]
  (* n n))

(apply-contracts [sqr [n] number? (not= 0 n) => pos? number?])
)


;; constraint functions and multimethods

(def all-numbers?  #(every? number? %))
(def all-positive? #(and (all-numbers? %) (every? pos? %)))
(def all-negative? #(and (all-numbers? %) (every? (complement pos?) %)))
