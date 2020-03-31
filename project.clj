(defproject hakukohderyhmapalvelu "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [clj-http "3.10.0"]
                 [cljs-http "0.1.46"]
                 [com.fzakaria/slf4j-timbre "0.3.19"]
                 [timbre-ns-pattern-level "0.1.2"]
                 [com.stuartsierra/component "1.0.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [org.flywaydb/flyway-core "6.3.2"]
                 [hikari-cp "2.11.0"]
                 [metosin/compojure-api "2.0.0-alpha13"]
                 [metosin/schema-tools "0.12.2"]
                 [org.clojure/core.async "1.0.567"]
                 [org.postgresql/postgresql "42.2.11"]
                 [radicalzephyr/ring.middleware.logger "0.6.0"]
                 [re-frame "0.12.0"]
                 [reagent "0.10.0"]
                 [ring "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [secretary "1.2.3"]
                 [stylefy "1.14.1"
                  :exclusions [[org.clojure/core.async]]]
                 [prismatic/schema "1.1.12"]
                 [thheller/shadow-cljs "2.8.93"]
                 [yogthos/config "1.1.7"]
                 [environ "1.1.0"]
                 [oph/clj-timbre-auditlog "0.1.0-SNAPSHOT"]
                 [fi.vm.sade/auditlogger "9.0.0-SNAPSHOT"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-less "1.7.5"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :main hakukohderyhmapalvelu.core

  :source-paths ["src/clj" "src/cljs" "src/cljc"]

  :clean-targets ^{:protect false} ["resources/public/hakukohderyhmapalvelu/js/compiled"
                                    "target"
                                    ".shadow-cljs"
                                    ".ts-out"]


  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :aliases {"server:dev"    ["with-profile" "dev" "run"]
            "frontend:dev"  ["with-profile" "dev" "do"
                             ["run" "-m" "shadow.cljs.devtools.cli" "watch" "hakukohderyhmapalvelu"]]
            "frontend:prod" ["with-profile" "prod" "do"
                             ["run" "-m" "shadow.cljs.devtools.cli" "release" "hakukohderyhmapalvelu"]]
            "build-report"  ["with-profile" "prod" "do"
                             ["run" "-m" "shadow.cljs.devtools.cli" "run" "shadow.cljs.build-report" "hakukohderyhmapalvelu" "target/build-report.html"]
                             ["shell" "open" "target/build-report.html"]]
            "lint"          ["with-profile" "dev" "do"
                             ["run" "-m" "clj-kondo.main" "--config" "oph-configuration/clj-kondo.config.edn" "--lint" "src"]]}

  :profiles
  {:dev
            {:dependencies [[binaryage/devtools "1.0.0"]
                            [clj-kondo "2020.03.20"]
                            [day8.re-frame/re-frame-10x "0.6.0"]
                            [day8.re-frame/tracing "0.5.3"]]
             :source-paths ["dev"]}

   :prod    {:dependencies [[day8.re-frame/tracing-stubs "0.5.3"]]}

   :uberjar {:source-paths ["env/prod/clj"]
             :dependencies [[day8.re-frame/tracing-stubs "0.5.3"]]
             :omit-source  true
             :aot          [hakukohderyhmapalvelu.core]
             :uberjar-name "hakukohderyhmapalvelu.jar"
             :prep-tasks   ["compile" ["frontend:prod"]]}})
