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
  (:use fogus.me.trammel.impl))

(defmacro contract 
  [& forms]
  (let [name (if (symbol? (first forms))
               (first forms) 
               nil)
        body (collect-bodies (if name
                               (rest forms)
                               forms))]
    (list* 'fn name body)))

(defmacro defconstrainedfn
  [name & body]
  (let [mdata (if (string? (first body))
                {:doc (first body)}
                {})
        body  (if (:doc mdata)
                (next body)
                body)
        parts 
        ]
    `(merge {:name '~name} ~mdata {:body '~body})))

(defconstrainedfn sqr
  "Calculates the square of a number."
  ([n]
     :requires
     (number? n)
     (not (zero? n))

     :ensures
     (pos? %)

     :body
     (* n n)))

(comment
  (def doubler-contract
       (contract doubler
                 [x]
                 :requires
                 (pos? x)

                 :ensures
                 (= (* 2 x) %)

                 [x y]
                 :requires
                 (pos? x)
                 (pos? y)
       
                 :ensures
                 (= (* 2 (+ x y)) %)))

  (def doubler-fn
       (fn doubler
         ([f x] 
            {:post [(= (* 2 x) %)], 
             :pre  [(pos? x)]} 
            (f x)) 
         ([f x y] 
            {:post [(= (* 2 (+ x y)) %)], 
             :pre  [(pos? x) (pos? y)]} 
            (f x y))))

  ((partial doubler-contract #(* 2 (+ %1 %2))) 2 3)

  ((partial doubler-contract #(+ %1 %1 %2 %2)) 2 3)

  ((partial doubler-contract #(* 3 (+ %1 %2))) 2 3)

  ((partial doubler-contract #(* 2 %)) 3)

  ((partial doubler-contract #(* 3 %)) 3))


(defmacro kontract [& forms]
  (let [name (if (symbol? (first forms))
               (first forms) 
               nil)
        body (kollect-bodies (if name
                               (rest forms)
                               forms))]
    (list* 'fn name body)))

;; USAGE

(comment
  (def doubler
       (kontract doubler 
         [x]
         (requires
          (pos? x))
         (ensures
          (= (* 2 x) %))))

  (def times2 (partial doubler #(* 2 %)))
  (times2 9)
  
  (def times3 (partial doubler #(* 3 %)))
  (times3 9)

  (def doubler-kontract
       (kontract doubler 
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

  ((partial doubler-kontract #(* 2 (+ %1 %2))) 2 3)

  ((partial doubler-kontract #(+ %1 %1 %2 %2)) 2 3)

  ((partial doubler-kontract #(* 3 (+ %1 %2))) 2 3))

(filter identity [1 nil 2 nil 3 nil 4])
(doc remove)
