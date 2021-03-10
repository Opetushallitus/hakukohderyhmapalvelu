(defproject hakukohderyhmapalvelu "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [camel-snake-kebab "0.4.1"]
                 [cheshire "5.10.0"]
                 [clj-http "3.10.3"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [com.taoensso/timbre "5.1.0"]
                 [com.fzakaria/slf4j-timbre "0.3.20"]
                 [timbre-ns-pattern-level "0.1.2"]
                 [com.stuartsierra/component "1.0.0"]
                 [org.flywaydb/flyway-core "7.0.2"]
                 [fi.vm.sade/auditlogger "9.0.0-SNAPSHOT"]
                 [fi.vm.sade.java-utils/java-cas "0.6.2-SNAPSHOT"]
                 [fi.vm.sade.java-utils/java-properties "0.1.0-SNAPSHOT"]
                 [oph/clj-access-logging "1.0.0-SNAPSHOT"]
                 [oph/clj-stdout-access-logging "1.0.0-SNAPSHOT"]
                 [oph/clj-timbre-access-logging "1.0.0-SNAPSHOT"]
                 [oph/clj-ring-db-cas-session "0.3.0-SNAPSHOT"]
                 [hikari-cp "2.13.0"]
                 [metosin/reitit "0.5.12"]
                 [metosin/schema-tools "0.12.2"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.clojure/core.match "1.0.0"]
                 [org.postgresql/postgresql "42.2.17"]
                 [yesql "0.5.3"]
                 [re-frame "1.1.1"]
                 [reagent "0.10.0"]
                 [com.fasterxml.jackson.core/jackson-core "2.12.1"]
                 [com.fasterxml.jackson.core/jackson-databind "2.12.1"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [ring/ring-session-timeout "0.2.0"]
                 [selmer "1.12.31"]
                 [stylefy "2.2.1"
                  :exclusions [[org.clojure/core.async]]]
                 [prismatic/schema "1.1.12"]
                 [thheller/shadow-cljs "2.11.5"]
                 [yogthos/config "1.1.7"]
                 [environ "1.2.0"]
                 [oph/clj-timbre-auditlog "0.1.0-SNAPSHOT"]]

  :plugins [[lein-ancient "0.6.15"]
            [lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :main hakukohderyhmapalvelu.core

  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]

  :clean-targets ^{:protect false} ["resources/public/hakukohderyhmapalvelu/js/compiled"
                                    "target"
                                    ".shadow-cljs"
                                    ".ts-out"]


  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :jvm-opts ["-Dclojure.main.report=stderr"]

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

  :repl-options {:init-ns user}

  :profiles
  {:dev
            {:dependencies [[binaryage/devtools "1.0.2"]
                            [clj-kondo "2020.10.10"]
                            [day8.re-frame/re-frame-10x "0.7.0"]
                            [day8.re-frame/tracing "0.6.0"]
                            [reloaded.repl "0.2.4"]]
             :source-paths ["dev/clj" "dev/cljs"]}

   :prod    {:dependencies [[day8.re-frame/tracing-stubs "0.6.0"]]}

   :uberjar {:source-paths ["env/prod/clj"]
             :dependencies [[day8.re-frame/tracing-stubs "0.6.0"]]
             :omit-source  true
             :aot          [hakukohderyhmapalvelu.core]
             :uberjar-name "hakukohderyhmapalvelu.jar"
             :prep-tasks   ["compile" ["frontend:prod"]]}}

  :repositories [["releases" {:url           "https://artifactory.opintopolku.fi/artifactory/oph-sade-release-local"
                              :sign-releases false
                              :snapshots     false}]
                 ["snapshots" {:url      "https://artifactory.opintopolku.fi/artifactory/oph-sade-snapshot-local"
                               :releases {:update :never}}]
                 ["ext-snapshots" {:url      "https://artifactory.opintopolku.fi/artifactory/ext-snapshot-local"
                                   :releases {:update :never}}]])
