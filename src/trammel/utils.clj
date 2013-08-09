;; utils.clj -- Contracts programming library for Clojure

;; by Michael Fogus - <http://fogus.me/fun/trammel>
;; May 26, 2010

; Copyright (c) Michael Fogus, 2010-2013. All rights reserved.  The use
; and distribution terms for this software are covered by the Eclipse
; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file COPYING the root of this
; distribution.  By using this software in any fashion, you are
; agreeing to be bound by the terms of this license.  You must not
; remove this notice, or any other, from this software.

(ns trammel.utils
  (:use trammel.factors))

(defmacro ^:private assert-w-message
  [check message]
  `(when-not ~check
     (throw (new AssertionError (str "Trammel assertion failed: " ~message "\n"
                                     (pr-str '~check))))))

(defn check-args!
  [name slots inv-description invariants]
  (assert-w-message (and inv-description (string? inv-description))
                    (str "Expecting an invariant description for " name))
  (assert-w-message (and invariants (or (map? invariants) (vector? invariants)))
                    (str "Expecting invariants of the form "
                         "[pre-conditions => post-conditions] or "
                         "{:pre [pre-conditions]}"
                         "for record type " name)))
