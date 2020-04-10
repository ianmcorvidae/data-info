(use '[clojure.java.shell :only (sh)])
(require '[clojure.string :as string])

(defn git-ref
  []
  (or (System/getenv "GIT_COMMIT")
      (string/trim (:out (sh "git" "rev-parse" "HEAD")))
      ""))

(defproject org.cyverse/data-info "2.20.0-SNAPSHOT"
  :description "provides an HTTP API for interacting with iRODS"
  :url "https://github.com/cyverse-de/data-info"
  :license {:name "BSD"
            :url "http://iplantcollaborative.org/sites/default/files/iPLANT-LICENSE.txt"}
  :manifest {"Git-Ref" ~(git-ref)}
  :uberjar-name "data-info-standalone.jar"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [cheshire "5.6.3"
                   :exclusions [[com.fasterxml.jackson.dataformat/jackson-dataformat-cbor]
                                [com.fasterxml.jackson.dataformat/jackson-dataformat-smile]
                                [com.fasterxml.jackson.core/jackson-annotations]
                                [com.fasterxml.jackson.core/jackson-databind]
                                [com.fasterxml.jackson.core/jackson-core]]]
                 [com.cemerick/url "0.1.1" :exclusions [com.cemerick/clojurescript.test]]
                 [dire "0.5.3"]
                 [me.raynes/fs "1.4.6"]
                 [metosin/compojure-api "1.1.8"]
                 [org.apache.tika/tika-core "1.16"]
                 [net.sf.opencsv/opencsv "2.3"]
                 [org.cyverse/otel "0.1.0-SNAPSHOT"]
                 ;; All these io.grpc are set up explicitly to avoid "WARNING!!!version ranges found for:" spew from leiningen
                 [io.grpc/grpc-api "1.28.0"]
                 [io.grpc/grpc-core "1.28.0" :exclusions [io.grpc/grpc-api]]
                 [io.grpc/grpc-protobuf "1.28.0"]
                 [io.grpc/grpc-netty-shaded "1.28.0" :exclusions [io.grpc/grpc-core]]
                 [io.opentelemetry/opentelemetry-sdk "0.3.0"]
                 [io.opentelemetry/opentelemetry-exporters-logging "0.3.0"]
                 [io.opentelemetry/opentelemetry-exporters-jaeger "0.3.0"]
                 [de.ubercode.clostache/clostache "1.4.0" :exclusions [org.clojure/core.incubator]]
                 [slingshot "0.12.2"]
                 [org.cyverse/clj-icat-direct "2.8.6"
                   :exclusions [[org.slf4j/slf4j-log4j12]
                                [log4j]]]
                 [org.cyverse/clj-jargon "2.8.10"
                   :exclusions [[org.slf4j/slf4j-log4j12]
                                [log4j]]]
                 [org.cyverse/clojure-commons "2.8.3"]
                 [org.cyverse/common-cli "2.8.1"]
                 [org.cyverse/common-cfg "2.8.1"]
                 [org.cyverse/common-swagger-api "2.11.27"]
                 [org.cyverse/heuristomancer "2.8.6"]
                 [org.cyverse/kameleon "3.0.2"]
                 [org.cyverse/metadata-client "3.0.0"]
                 [org.cyverse/metadata-files "1.0.2"]
                 [org.cyverse/oai-ore "1.0.3"]
                 [org.cyverse/service-logging "2.8.0"]
                 [org.cyverse/tree-urls-client "2.8.1"]
                 [org.cyverse/event-messages "0.0.1"]
                 [com.novemberain/langohr "3.5.1"]]
  :eastwood {:exclude-namespaces [data-info.routes.schemas.tickets
                                  data-info.routes.schemas.stats
                                  data-info.routes.schemas.sharing
                                  data-info.routes.schemas.trash
                                  :test-paths]
             :linters [:wrong-arity :wrong-ns-form :wrong-pre-post :wrong-tag :misplaced-docstrings]}
  :plugins [[test2junit "1.1.3"]
            [jonase/eastwood "0.3.4"]]
  :profiles {:dev     {:dependencies   [[ring "1.5.0"]] ;; required for lein-ring with compojure-api 1.1.8+
                       :plugins        [[lein-ring "0.9.7"]]
                       :resource-paths ["conf/test"]}
             :uberjar {:aot :all}}
  :main ^:skip-aot data-info.core
  :ring {:handler data-info.routes/app
         :init data-info.core/lein-ring-init
         :port 31360
         :auto-reload? false}
  :uberjar-exclusions [#".*[.]SF" #"LICENSE" #"NOTICE"]
  :jvm-opts ["-Dlogback.configurationFile=/etc/iplant/de/logging/data-info-logging.xml"
             "-javaagent:/home/mian/opentelemetry-auto-intstr-java/opentelemetry-auto-0.2.2.jar"
             "-Dota.exporter.jar=/home/mian/opentelemetry-auto-intstr-java/opentelemetry-auto-exporters-jaeger-0.2.2.jar"
             "-Dota.exporter.jaeger.service.name=data-info"
             "-Dota.exporter.jaeger.endpoint=localhost:14250"])
