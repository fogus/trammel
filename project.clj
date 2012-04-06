(defproject trammel "0.7.0-SNAPSHOT"
  :description "A library providing contracts programming for Clojure."
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [thneed "1.0.0-SNAPSHOT"]]
  :dev-dependencies [[lein-clojars "0.8.0"]
                     [jline "0.9.94"]
                     [swank-clojure "1.4.0"]
                     [lein-marginalia "0.7.0"]
                     [lein-multi "1.1.0"]]
  :multi-deps {:all [[thneed "1.0.0-SNAPSHOT"]]
               "1.3" [[org.clojure/clojure "1.3.0"]]
               "1.4" [[org.clojure/clojure "1.4.0-beta1"]]})

;; lein multi test
