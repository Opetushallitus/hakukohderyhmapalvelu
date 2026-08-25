(ns hakukohderyhmapalvelu.events.core-events
  (:require
    [clojure.string :as str]
    [hakukohderyhmapalvelu.db :as db]
    [hakukohderyhmapalvelu.macros.event-macros :as events]
    [hakukohderyhmapalvelu.urls :as urls]
    [re-frame.core :as re-frame]
    [day8.re-frame.tracing :refer-macros [fn-traced]]))

(events/reg-event-db-validating
  :core/initialize-db
  (fn-traced [_ _]
    db/default-db))

(def ^:private supported-langs #{:fi :sv :en})

(def ^:private hakukohderyhmapalvelu-right-prefix "APP_HAKUKOHDERYHMAPALVELU")

(defn- response->lang
  "Poimii virkailijan asiointikielen kayttooikeus-servicen me-vastauksen
   :lang-kentästä. Tuntematon tai puuttuva kieli tulkitaan suomeksi."
  [response]
  (let [lang (:lang response)]
    (or (when (string? lang)
          (-> lang str/lower-case keyword supported-langs))
        :fi)))

(defn- response->user-groups [response]
  (->> (:groups response)
       (filter #(str/starts-with? % hakukohderyhmapalvelu-right-prefix))
       vec))

;; Asettaa dokumentin kieliattribuutin, jota ruudunlukijat käyttävät. index.html
;; renderöidään kertaalleen palvelimen käynnistyessä, joten kieltä ei voi
;; välittää templateen pyyntökohtaisesti.
(re-frame/reg-fx
  :document-lang
  (fn [lang]
    (set! (.. js/document -documentElement -lang) (name lang))))

(events/reg-event-fx-validating
  :core/get-user-info
  (fn-traced [_ _]
             (let [url (urls/get-url :kayttooikeus-service.me)]
               {:http {:http-request-id  :core/get-user-info
                       :method           :get
                       :path             url
                       :response-handler [:core/handle-get-user-info]
                       :body             {}}})))

(events/reg-event-fx-validating
  :core/handle-get-user-info
  (fn-traced [{db :db} [response]]
             (let [lang (response->lang response)]
               {:db            (assoc db
                                      :lang lang
                                      :user-groups (response->user-groups response))
                :document-lang lang})))
