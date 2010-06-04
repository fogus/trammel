;;; impl.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; June 1, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.trammel.impl)

(defn build-constraints-map [expectations]
  (apply merge
         (for [[[dir] & [cnstr]] (->> expectations
                                      (partition-by #{:requires :ensures})
                                      (partition 2))]
           {(case dir
                  :requires :pre
                  :ensures  :post)
            (vec cnstr)})))

(defn build-contract [[[sig] expectations :as c]]
  (list 
    (into '[f] sig)
    (build-constraints-map expectations)
    (list* 'f sig)))


(defn collect-bodies [forms]
  (for [body (->> (partition-by vector? forms)
                  (partition 2))]
    (build-contract body)))

(defn build-forms-map
  [forms]
  (for [[[e] & c] (map #(partition-by keyword? %) forms)]
    {e (apply hash-map c)}))

(comment
  {:cnstr ({[x] {(:requires)
                 ((number? x) (pos? x)),
                 (:body) ((float x)),
                 (:ensures) ((float? %))}}
           
           {[x y] {(:requires) ((every? number? [x y]) (every? pos? [x y])),
                   (:body) ([(float x) (float y)]),
                   (:ensures) ((every? float? %))}}), :doc "test", :name foo})


(defn build-kontract [c]
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


(defn kollect-bodies [forms]
  (for [form (partition 3 forms)]
    (build-kontract form)))

