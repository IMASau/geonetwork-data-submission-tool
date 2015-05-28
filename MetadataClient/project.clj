(defproject imas-metcalf "1.0.0"
            :description "Metadata management tool designed for academics"
            :url "https://github.com/IMASau/geonetwork-data-submission-tool"
            :license {:name "Eclipse Public License"
                      :url  "http://www.eclipse.org/legal/epl-v10.html"}

            :source-paths ["src/cljs" "test/cljs"]

            :clean-targets ^{:protect false}
            ["resources/public/js/dev"
             "resources/public/js/dev.js"
             "resources/public/js/prod"
             "resources/public/js/prod.js"]

            :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                           [org.clojure/clojurescript "0.0-3291" :scope "provided"]
                           [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                           [org.clojure/core.match "0.3.0-alpha4"]
                           [figwheel "0.3.3"]
                           [org.omcljs/om "0.8.8"]
                           [sablono "0.3.4"]
                           ;; FIXME upgrade react after select-om-all upgrade to 0.1.0 (not SNAPSHOT) or higher,
                           ;; which has fixed-data-table 0.2.0, compatible with React 0.13
                           [cljsjs/react "0.12.2-5"]
                           [cljsjs/openlayers "3.3.0-0"]
                           [cljsjs/pikaday "1.3.2-0"]
                           [cljsjs/moment "2.9.0-0"]
                           [cljsjs/fixed-data-table "0.1.2-2"]
                           [tailrecursion/cljs-priority-map "1.1.0"]
                           [cljs-ajax "0.3.11"]
                           [com.andrewmcveigh/cljs-time "0.3.5"]
                           [condense/om-tick "0.1.0-SNAPSHOT"]
                           [condense/select-om-all "0.1.0-SNAPSHOT"]]

            :plugins [[lein-cljsbuild "1.0.6"]
                      [lein-marginalia "0.8.0"]
                      [com.cemerick/clojurescript.test "0.3.3"]]

            :min-lein-version "2.5.0"

            :cljsbuild {:builds {:app {:source-paths ["src/cljs"]
                                       :compiler     {:output-to      "resources/public/js/dev.js"
                                                      :output-dir     "resources/public/js/dev"
                                                      :source-map     "resources/public/js/dev.js.map"
                                                      :libs           ["lib"]
                                                      :cache-analysis true
                                                      :optimizations  :none
                                                      :pretty-print   true}}}}

            :profiles {:dev     {:plugins   [[lein-figwheel "0.3.3"]]

                                 :figwheel  {:http-server-root "public"
                                             :port             3449
                                             :nrepl-port       7888
                                             :css-dirs         ["resources/public/css"]}

                                 :cljsbuild {:builds {:app {:source-paths ["env/dev/cljs" "test/cljs"]
                                                            :figwheel {:on-jsload "metcalf.dev/test-and-main"}}}}}

                       :uberjar {:hooks       [leiningen.cljsbuild]
                                 :omit-source true
                                 :aot         :all
                                 :cljsbuild   {:builds {:app
                                                        {:source-paths ["env/prod/cljs"]
                                                         :compiler
                                                                       {:output-to        "resources/public/js/prod.js"
                                                                        :output-dir       "resources/public/js/prod"
                                                                        :source-map       "resources/public/js/prod.js.map"
                                                                        :libs             ["lib"]
                                                                        :externs          ["ext/olx.js"]
                                                                        :optimizations    :advanced
                                                                        ;:pretty-print     true
                                                                        ;:pseudo-names     true
                                                                        :closure-warnings {:externs-validation :off
                                                                                           :non-standard-jsdoc :off}}}}}}})
