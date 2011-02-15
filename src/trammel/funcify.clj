(ns trammel.funcify)


(declare funcify*)
(declare funcify-factor)

(defn funcify
  "Performs the *magic* of the Trammel syntax.  That is, it currently identifies isolated functions and
   wraps them in a list with the appropriate args.  It also recognizes keywords and does the same under 
   the assumption that a map access is required.  It then returns the vector of calls expected by the
   Clojure pre- and post-conditions map."
  [args cnstr]
  (vec (map #(funcify* % args) cnstr)))


(defmulti funcify* (fn [e _] (class e)))

(defmethod funcify* clojure.lang.IFn        [e args] (list* e args))
(defmethod funcify* java.util.regex.Pattern [e args] (list* 'clojure.core/re-matches e args))
(defmethod funcify* java.lang.String        [e args] (list* 'clojure.core/= e args))
(defmethod funcify* java.lang.Number        [e args] (list* 'clojure.core/= e args))
(defmethod funcify* :default                [e args] (funcify-factor e args))


;; funcify-factor

(defmulti funcify-factor (fn [[h & _] _] h))

(defmethod funcify-factor 'or
  [e args]
  (list* 'or (funcify args (rest e))))

(defmethod funcify-factor 'in
  [e args]
  (concat (list* 'in args) (rest e)))

(defmethod funcify-factor 'whitelist
  [e args]
  (concat (list* 'whitelist args) (rest e)))

(defmethod funcify-factor :default
  [e args]
  e)
