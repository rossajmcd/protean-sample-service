(defproject sample-service "0.1.0"
  :description "An example Protean governed RESTful API for demonstrating
                testable endpoints."
  :url "http://www.github.com/passivsystems/protean-sample-service"
  :license {:name "Apache Livense v2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.stuartsierra/component "0.2.1"]
                 [ring/ring-core "1.2.2"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [compojure "1.1.6"]
                 [cheshire "5.3.1"]]
  :main sampleservice.core)
