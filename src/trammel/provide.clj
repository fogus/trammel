;; provide.clj -- Contracts programming library for Clojure

;; by Michael Fogus - <http://fogus.me/fun/trammel>
;; May 26, 2010

; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
; and distribution terms for this software are covered by the Eclipse
; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file COPYING the root of this
; distribution.  By using this software in any fashion, you are
; agreeing to be bound by the terms of this license.  You must not
; remove this notice, or any other, from this software.

(ns trammel.provide
  "Provides the Var manipulation macro offering ex post facto application of contracts
   to existing functions."
  (:use  [clojure.core.contracts :only (with-constraints contract)]))

(defmacro contracts
  [& kontracts]
  (let [fn-names  (map first kontracts)
        kontracts (for [[n ds & more] kontracts] 
                    (if (vector? (first more)) 
                      (list* `contract n ds more) 
                      (first more)))]
    `(do
       ~@(for [[n# c#] (zipmap fn-names kontracts)]
           (list `alter-var-root (list `var n#) 
                 (list `fn '[f c] (list `with-constraints 'f 'c)) c#))
       nil)))


