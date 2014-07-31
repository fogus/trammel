(defproject trammel "0.8.0"
  :description "A library providing contracts programming for Clojure."
  :dependencies [[org.clojure/clojure "1.6.0"] 
                 [org.clojure/core.contracts "0.0.5"]]
  :dev-dependencies [[lein-marginalia "0.7.1"]]
  :profiles {:1.2   {:dependencies [[org.clojure/clojure "1.2.0"]]}
             :1.2.1 {:dependencies [[org.clojure/clojure "1.2.1"]]}
             :1.3   {:dependencies [[org.clojure/clojure "1.3.0"]]}
             :1.4   {:dependencies [[org.clojure/clojure "1.4.0"]]}
             :1.5.0 {:dependencies [[org.clojure/clojure "1.5.0"]]}
             :1.5.1 {:dependencies [[org.clojure/clojure "1.5.1"]]}})
