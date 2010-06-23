;;; impl_tests.clj -- Contracts programming library for Clojure

;; by Michael Fogus - http://fogus.me/fun/
;; Jun 23, 2010

;; Copyright (c) Michael Fogus, 2010. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file COPYING the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns fogus.me.impl-tests
  (:use fogus.me.trammel.impl)
  (:use [clojure.test :only [deftest is]]))

(def *expectations-table*
     [{:expect '{:pre [(every? foo [x y]) (bar x)] :post [(baz %) (quux %)]}
       :body   '(:requires (every? foo [x y]) (bar x) :ensures (baz %) (quux %))}

      {:expect '{:pre [(foo x) (bar x)] :post [(baz %) (quux %)]}
       :body   '(:requires (foo x) (bar x) :ensures (baz %) (quux %))}

      {:expect '{:pre [(foo x) (bar x)] :post [(baz %)]}
       :body   '(:requires (foo x) (bar x) :ensures (baz %))}

      {:expect '{:pre [(foo x)] :post [(baz %)]}
       :body   '(:requires (foo x) :ensures (baz %))}

      {:expect '{:post [(baz %)]}
       :body   '(:ensures (baz %))}

      {:expect '{:pre [(foo x)]}
       :body   '(:requires (foo x))}

      {:expect nil
       :body   '()}
      ])

(deftest build-constraints-map-test
  (doseq [t *expectations-table*]
    (is (= (build-constraints-map (:body t)) (:expect t)))))

(deftest impl-test
  (build-constraints-map-test))
