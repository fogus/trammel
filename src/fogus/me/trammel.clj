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
               (gensym))]
    (list* 'fn name 
      (collect-bodies 
       (if (symbol? (first forms))
         (rest forms)
         forms)))))

(defmacro ^{:private true} chain-fn
  ([f] f)
  ([f contract] (list 'partial contract f))
  ([f contract & more]
     `(with-contracts (with-contracts ~f ~contract) ~@more)))

(defmacro defcontract 
  [name & forms]
  `(def ~name
     (contract ~@forms)))

(defmacro defconstrainedfn
  [name & body]
  (let [mdata (if (string? (first body))
                {:doc (first body)}
                {})
        body  (if (:doc mdata)
                (next body)
                body)
        body  (for [bd (build-forms-map body)] 
                (let [arg (first (keys bd))
                      b   (first (vals bd))]
                  (list* arg
                         {:pre  (vec (b '(:requires)))
                          :post (vec (b '(:ensures)))}
                         (b '(:body)))))]
    `(defn ~name
       ~(:doc mdata)
       ~@body)))

(defmacro kontract [& forms]
  (let [name (if (symbol? (first forms))
               (first forms) 
               nil)]
    (list* 'fn name 
      (kollect-bodies 
       (if name
         (rest forms)
         forms)))))
