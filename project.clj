(defproject trammel "0.7.0-SNAPSHOT-ambrosebs"
  :description "A library providing contracts programming for Clojure."
  :dependencies [[org.clojure/clojure "1.3.0"]]
  :dev-dependencies [[lein-clojars "0.5.0-SNAPSHOT"]
                     [jline "0.9.94"]
                     [swank-clojure "1.4.0"]
                     [lein-marginalia "0.7.0-SNAPSHOT"]
                     [lein-multi "1.1.0"]]
  :multi-deps {"1.2" [[org.clojure/clojure "1.2.0"]]
               "1.3" [[org.clojure/clojure "1.3.0"]]
               "1.4" [[org.clojure/clojure "1.4.0-beta1"]]})

;; lein multi test
