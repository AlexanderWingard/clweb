(defproject clweb "0.1.0-SNAPSHOT"
  :dependencies [
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.229"]
                 [compojure "1.5.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [http-kit "2.1.19"]
                 [cljsjs/d3 "4.3.0-4"]
                 ]
  :plugins [[lein-figwheel "0.5.9"]
            [lein-cljsbuild "1.1.3"]]
  :main ^:skip-aot clweb.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :figwheel {
             :ring-handler clweb.core/ring-handler
             }
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src/"]
                        :figwheel {
                                   :on-jsload clweb.core/figwheel-reload
                                   }
                        :compiler {:main clweb.core
                                   :asset-path "js/out"
                                   :output-to "resources/public/js/script.js"
                                   :output-dir "resources/public/js/out"}}
                       {:id "min"
                        :source-paths ["src/"]
                        :compiler {:main clweb.core
                                   :output-to "resources/public/js/script.js"
                                   :output-dir "resources/public/js/out-min"
                                   :source-map-timestamp true
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  )
