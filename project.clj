;; Riippuvuusperheiden versiot yhtenä totuutena. Leiningen ei tue Maven-BOM importia
;; (:scope "import"), joten artefaktit listataan eksplisiittisesti mutta versio jaetaan muuttujalla.
(def jackson-version "2.21.6")     ; CVE-2026-54512/54513 (CRITICAL); 2.15-linjalle ei backporttia
(def netty-version "4.2.17.Final") ; CVE-2026-44249, CVE-2026-75595 (CRITICAL) ym.; java-cas 2.3.0 / AHC 3.0.12
(def bouncycastle-version "1.85")  ; CVE-2026-8763 ym. (CRITICAL); pois *-jdk15on 1.62:sta buddy-auth 3.x:llä
;; jetty: ring 1.9.1 (Jetty 9.4.57 EOL) -> ring 1.15.5 (Jetty 12.1.x, ee9-yhteensopivuuskerros).
;; Pinni 12.1.12:een: CVE-2024-7708, CVE-2024-8184, CVE-2026-2332 (fix 12.1.7), CVE-2026-10050 (fix 12.1.10).
(def jetty-version "12.1.12")

(defproject hakukohderyhmapalvelu "0.1.0-SNAPSHOT"
  :managed-dependencies [[org.apache.commons/commons-compress "1.21"]
                         [commons-io "2.14.0"]
                         [commons-fileupload "1.6.0"]
                         [org.yaml/snakeyaml "2.0"]
                         [com.google.protobuf/protobuf-java "3.25.5"]
                         ;; Tietoturvapäivitykset 2026-09
                         [io.undertow/undertow-core "2.3.25.Final"]
                         [com.fasterxml.jackson.core/jackson-annotations "2.21"]
                         ;; auditlogger -> json-patch tuo 3.11 (CVE-2025-48924)
                         [org.apache.commons/commons-lang3 "3.20.0"]
                         ;; ring-core -> commons-fileupload2-core 2.0.0-M1 (CVE-2025-48976)
                         [org.apache.commons/commons-fileupload2-core "2.0.0-M4"]
                         ;; pidä ring-core yhdessä versiossa ring 1.15.5:n kanssa
                         [ring/ring-core "1.15.5"]
                         ;; netty: java-cas 2.3.0 -> AHC 3.0.12 vaatii 4.2.x -> pakota koko perhe (~netty-version)
                         [io.netty/netty-buffer ~netty-version]
                         [io.netty/netty-common ~netty-version]
                         [io.netty/netty-codec ~netty-version]
                         [io.netty/netty-codec-base ~netty-version]
                         [io.netty/netty-codec-compression ~netty-version]
                         [io.netty/netty-codec-dns ~netty-version]
                         [io.netty/netty-codec-http ~netty-version]
                         [io.netty/netty-codec-http2 ~netty-version]
                         [io.netty/netty-codec-socks ~netty-version]
                         [io.netty/netty-handler ~netty-version]
                         [io.netty/netty-handler-proxy ~netty-version]
                         [io.netty/netty-resolver ~netty-version]
                         [io.netty/netty-resolver-dns ~netty-version]
                         [io.netty/netty-transport ~netty-version]
                         [io.netty/netty-transport-classes-epoll ~netty-version]
                         [io.netty/netty-transport-classes-kqueue ~netty-version]
                         [io.netty/netty-transport-native-unix-common ~netty-version]
                         [io.netty/netty-transport-native-epoll ~netty-version :classifier "linux-x86_64"]
                         [io.netty/netty-transport-native-epoll ~netty-version :classifier "linux-aarch_64"]
                         [io.netty/netty-transport-native-kqueue ~netty-version :classifier "osx-x86_64"]
                         [org.eclipse.jetty/jetty-server ~jetty-version]
                         [org.eclipse.jetty/jetty-http ~jetty-version]
                         [org.eclipse.jetty/jetty-io ~jetty-version]
                         [org.eclipse.jetty/jetty-util ~jetty-version]
                         [org.eclipse.jetty/jetty-security ~jetty-version]
                         [org.eclipse.jetty/jetty-session ~jetty-version]
                         [org.eclipse.jetty/jetty-xml ~jetty-version]
                         [org.eclipse.jetty/jetty-client ~jetty-version]
                         [org.eclipse.jetty/jetty-alpn-server ~jetty-version]
                         [org.eclipse.jetty/jetty-unixdomain-server ~jetty-version]
                         [org.eclipse.jetty.ee9/jetty-ee9-nested ~jetty-version]
                         [org.eclipse.jetty.ee9/jetty-ee9-servlet ~jetty-version]
                         [org.eclipse.jetty.ee9/jetty-ee9-security ~jetty-version]
                         [org.eclipse.jetty.ee9/jetty-ee9-webapp ~jetty-version]
                         [org.eclipse.jetty.ee9.websocket/jetty-ee9-websocket-jetty-server ~jetty-version]
                         [org.eclipse.jetty.ee9.websocket/jetty-ee9-websocket-jetty-api ~jetty-version]
                         [org.eclipse.jetty.ee9.websocket/jetty-ee9-websocket-jetty-common ~jetty-version]
                         [org.eclipse.jetty.websocket/jetty-websocket-core-common ~jetty-version]
                         [org.eclipse.jetty.websocket/jetty-websocket-core-server ~jetty-version]
                         [org.eclipse.jetty.websocket/jetty-websocket-core-client ~jetty-version]]
  :dependencies [[org.clojure/clojure "1.11.2"]
                 [org.clojure/clojurescript "1.11.132"]
                 [com.google.javascript/closure-compiler-unshaded "v20240317"]
                 [camel-snake-kebab "0.4.1"]
                 [cheshire "5.10.2"]
                 [clj-http "3.10.3"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [com.taoensso/timbre "4.10.0"]
                 [com.fzakaria/slf4j-timbre "0.3.20"]
                 [timbre-ns-pattern-level "0.1.2"]
                 [com.stuartsierra/component "1.0.0"]
                 [org.flywaydb/flyway-core "7.0.2"]
                 ;; 9.0.0-SNAPSHOT oli vain Artifactoryssa -> 9.2.7-SNAPSHOT (GitHub Packages, sama kuin ataru/maksut/liiteri)
                 [fi.vm.sade/auditlogger "9.2.7-SNAPSHOT"]
                 [opiskelijavalinnat-utils/java-cas "2.3.0-SNAPSHOT"]
                 [fi.vm.sade.java-utils/java-properties "0.1.0-SNAPSHOT"]
                 [hikari-cp "2.13.0"]
                 [metosin/reitit "0.5.12"]
                 [metosin/schema-tools "0.12.2"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.clojure/core.match "1.0.0"]
                 [org.postgresql/postgresql "42.7.12"]
                 [com.layerware/hugsql "0.5.1"]
                 [yesql "0.5.3"]
                 [re-frame "1.4.3"]
                 [reagent "1.3.0"]
                 [reagent-utils "0.3.8"]
                 ;; 2.15-linjalle ei korjausta CVE-2026-54512/54513 (CRITICAL) -> 2.21.x
                 [com.fasterxml.jackson.core/jackson-core ~jackson-version]
                 [com.fasterxml.jackson.core/jackson-databind ~jackson-version]
                 ;; ring-perhe nostettava yhdessä ring 1.15.5:n kanssa (ring.websocket ns lisätty ring-core 1.11:ssä)
                 [metosin/ring-http-response "0.9.5"]
                 [ring/ring-defaults "0.6.0"]
                 [ring/ring-json "0.5.1"]
                 [ring/ring-session-timeout "0.3.0"]
                 [selmer "1.12.31"]
                 [stylefy "2.2.1"
                  :exclusions [[org.clojure/core.async]]]
                 [prismatic/schema "1.1.12"]
                 [thheller/shadow-cljs "2.28.23"]
                 [yogthos/config "1.1.7"]
                 [environ "1.2.0"]
                 ;; 2.2.0 -> buddy-core 1.6.0 -> bouncycastle *-jdk15on 1.62 (CRITICAL, ei korjausta).
                 ;; 3.0.323 on sama versio jonka clj-ring-db-cas-session muutenkin haluaa.
                 [buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-core "1.12.0-430"]
                 [org.bouncycastle/bcprov-jdk18on ~bouncycastle-version]
                 [org.bouncycastle/bcpkix-jdk18on ~bouncycastle-version]
                 [org.bouncycastle/bcutil-jdk18on ~bouncycastle-version]
                 [ring "1.15.5"]
                 [fi.vm.sade.dokumenttipalvelu/dokumenttipalvelu "6.15-SNAPSHOT"]
                 [opiskelijavalinnat-utils/clj-ring-db-cas-session "1.0.0-SNAPSHOT"]
                 [day8.re-frame/tracing "0.6.2"]]

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

  :auto-clean false

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
            {:dependencies [[binaryage/devtools "1.0.7"]
                            [clj-kondo "2025.04.07"]
                            [day8.re-frame/re-frame-10x "1.10.0"]
                            [reloaded.repl "0.2.4"]]
             :source-paths ["dev/clj" "dev/cljs"]}

   :prod    {:dependencies [[day8.re-frame/tracing-stubs "0.6.2"]]
             :aot          [hakukohderyhmapalvelu.core]
             :uberjar-name "hakukohderyhmapalvelu.jar"}

   :uberjar {:source-paths ["env/prod/clj"]
             :dependencies [[day8.re-frame/tracing-stubs "0.6.2"]]
             :omit-source  false
             :prep-tasks   ["compile" ["frontend:prod"]]}

   :ovara {:main hakukohderyhmapalvelu.siirtotiedosto.ajastus.siirtotiedosto-app
           :aot  [hakukohderyhmapalvelu.siirtotiedosto.ajastus.siirtotiedosto-app]
           :uberjar-name "ovara-hakukohderyhmapalvelu.jar"
           :env            {:config "src/clj/hakukohderyhmapalvelu/config.edn"}}
   }

  ;; Artifactory (artifactory.opintopolku.fi) poistettu -> riippuvuudet
  ;; GitHub Packagesista / Maven Centralista / Clojarsista
  :repositories [["github" {:url "https://maven.pkg.github.com/Opetushallitus/packages"
                            :username "private-token"
                            :password :env/GITHUB_TOKEN}]])
