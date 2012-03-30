(ns trammel.utils
  (:use trammel.factors))

(defmacro ^:private assert-w-message
  [check message]
  `(when-not ~check
     (throw (new AssertionError (str "Trammel asserttion failed: " ~message "\n" (pr-str '~check))))))

(defn check-args!
  [name slots inv-description invariants]
  (assert-w-message (and inv-description (string? inv-description))
                    (str "Expecting an invariant description for " name))
  (assert-w-message (and invariants (or (map? invariants) (vector? invariants)))
                    (str "Expecting invariants of the form "
                         "[pre-conditions => post-conditions] or "
                         "{:pre [pre-conditions]}"
                         "for record type " name)))

(defn ^:private keys-apply
  "Takes a function, a set of keys, and a map and applies the function to the map on the given keys.  
   A new map of the results of the function applied to the keyed entries is returned.
  "
  [f ks m]
  {:pre  [(or (fn? f) (keyword? f) (symbol? f))
          (except (coll? ks) (map? ks))
          (map? m)]
   :post [(map? %)]}
  (let [only (select-keys m ks)] 
    (zipmap (keys only)
            (map f (vals only)))))

(defn manip-map
  "Takes a function, a set of keys, and a map and applies the function to the map on the given keys.  
   A modified version of the original map is returned with the results of the function applied to each 
   keyed entry.
  "
  [f ks m]
  {:pre  [(or (fn? f) (keyword? f) (symbol? f))
          (except (coll? ks) (map? ks))
          (map? m)]
   :post [(map? %)
          (= (keys %) (keys m))]}  
  (conj m (keys-apply f ks m)))
