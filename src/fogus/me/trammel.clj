;;; trammel.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; May 26, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

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


(declare kollect-bodies build-kontract)

(defmacro kontract [& forms]
  (let [name (if (symbol? (first forms))
               (first forms) 
               nil)
        body (kollect-bodies (if name
                               (rest forms)
                               forms))]
    `(quote ~body)))

(defn- kollect-bodies [forms]
  (let [bodies (->> (partition-by vector? forms)
                    (partition 2))]
    (for [body bodies]
      (build-kontract (vec body)))))

(defn- build-kontract [[[sig] expectations :as c]]
  (let [[L R] (->> expectations
                   (partition-by #{:requires :ensures})
                   (partition 2))]
    (println L)
    (println)))

(comment
  (count (kontract doubler
                   [x]
                   #_:requires
                   #_(pos? x)

                   :ensures
                   (= (* 2 x) %)

                   [x y]
                   :requires
                   (pos? x)
                   (pos? y)
       
                   :ensures
                   (= (* 2 (+ x y)) %))))


;; USAGE

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
