(defproject trammel "0.8.0-SNAPSHOT"
  :description "A library providing contracts programming for Clojure."
  :dependencies [[org.clojure/clojure "1.5.1"] 
                 [org.clojure/core.contracts "0.0.4"]]
  :dev-dependencies [[lein-clojars "0.8.0"]
                     [jline "0.9.94"]
                     [swank-clojure "1.4.2"]
                     [lein-marginalia "0.7.1"]
                     [lein-multi "1.1.0"]]
  :profiles {:1.2   {:dependencies [[org.clojure/clojure "1.2.0"]]}
             :1.2.1 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3   {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4   {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5   {:dependencies [[org.clojure/clojure "1.5.0"]]}})

;; lein multi test
