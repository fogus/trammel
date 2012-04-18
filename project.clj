(defproject trammel "0.8.0-SNAPSHOT"
  :description "A library providing contracts programming for Clojure."
  :dev-dependencies [[lein-clojars "0.8.0"]
                     [jline "0.9.94"]
                     [swank-clojure "1.4.0"]
                     [lein-marginalia "0.7.0"]
                     [lein-multi "1.1.0"]]
  :multi-deps {:all [[thneed "1.0.0-SNAPSHOT"]]
               "1.2"   [[org.clojure/clojure "1.2.0"]]
               "1.2.1" [[org.clojure/clojure "1.2.1"]]
               "1.3"   [[org.clojure/clojure "1.3.0"]]
               "1.4"   [[org.clojure/clojure "1.4.0"]]})

;; lein multi test
