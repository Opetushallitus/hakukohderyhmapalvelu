(ns hakukohderyhmapalvelu.fx.http-fx
  (:require [cljs.core.async :as async]
            [cljs.core.async.interop]
            [clojure.string :as string]
            [hakukohderyhmapalvelu.config :as c]
            [hakukohderyhmapalvelu.urls :as urls]
            [re-frame.core :as re-frame]
            [schema.core :as s]
            [goog.net.cookies])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cljs.core.async.interop :refer [<p!]])
  (:import [goog.net.XhrIo]))

(defn- get-cookie-value [name]
  (.get goog.net.cookies name))

(defn- create-search-params [url search-params]
  (let [search-params' (-> (js/URL. url (.-href js/location))
                           (.-searchParams))]
    (doseq [[key val] search-params]
      (.append search-params' (name key) val))
    (.toString search-params')))

(defn- create-url [url search-params]
  (let [url' (js/URL. url (.-href js/location))
        search-params' (create-search-params url search-params)]
    (if (string/blank? search-params')
      url
      (str (.-origin url') (.-pathname url') "?" search-params'))))

(defn- redirect-to-login [response]
  (->> (get-in response [:body :redirect])
       (js/URL.)
       (set! (.-href (.-location js/window)))))

(defn- error-status? [status]
  (<= 400 status 599))

(defn- fetch [{:keys [url
                      method
                      redirect?
                      body
                      search-params]}]
  (let [url' (create-url url search-params)
        method'   (case method
                    :get "GET"
                    :post "POST"
                    :put "PUT"
                    :delete "DELETE")
        caller-id (:caller-id c/config)
        csrf (get-cookie-value "CSRF")
        headers (cond-> {"caller-id"    caller-id
                         "content-type" "application/json"}
                        (#{:post :put :delete} method) (assoc "CSRF" csrf))
        redirect  (if redirect?
                    "follow"
                    "error")]
    (go
      (let [response        (<p! (js/fetch
                                   url'
                                   (clj->js (cond-> {:method   method'
                                                     :headers  headers
                                                     :redirect redirect}
                                                    (seq body)
                                                    (assoc
                                                      :body
                                                      (->> body clj->js (.stringify js/JSON)))))))
            status          (.-status response)
            redirected?     (.-redirected response)
            response-common {:status      status
                             :redirected? redirected?}]
        (try
          (let [body (<p! (.json response))]
            (assoc response-common
                   :body
                   (js->clj body :keywordize-keys true)))
          (catch js/Error _
            response-common))))))



(s/defschema HttpSpec
  {:http-request-id                  s/Keyword
   :method                           (s/enum :get :post :put :delete)
   :path                             s/Str
   (s/optional-key :request-schema)  s/Any
   (s/optional-key :response-schema) s/Any
   :response-handler                 [(s/one s/Keyword "handler ID") s/Any]
   (s/optional-key :error-handler)   [(s/one s/Keyword "handler ID") s/Any]
   (s/optional-key :cas)             s/Keyword
   (s/optional-key :body)            s/Any
   (s/optional-key :search-params)   [[(s/one s/Keyword "key") (s/one s/Str "value")]]})

(re-frame/reg-fx
  :http
  (s/fn http-fx [{:keys [http-request-id
                         method
                         path
                         request-schema
                         response-schema
                         response-handler
                         error-handler
                         cas
                         body
                         search-params]} :- HttpSpec]
    (when request-schema
      (s/validate request-schema body))
    (go
      (let [do-request            (fn do-request []
                                    (fetch (cond-> {:url           path
                                                    :method        method
                                                    :redirect?     true
                                                    :search-params search-params}
                                                   (seq body)
                                                   (assoc :body body))))
            do-cas-authentication (fn do-cas-authentication []
                                    (let [url (urls/get-url cas)]
                                      (fetch {:url           url
                                              :method        :get
                                              :redirect?     true
                                              :search-params search-params})))
            {body :body status :status} (let [response' (async/<! (do-request))]
                           (cond (and (:redirected? response')
                                      (not= method :get))
                                 (async/<! (do-request))

                                 (and cas
                                      (= (:status response') 401))
                                 (do
                                   (async/<! (do-cas-authentication))
                                   (async/<! (do-request)))
                                 (and (nil? cas)
                                      (= (:status response') 401)
                                      (string? (get-in response' [:body :redirect])))
                                 (redirect-to-login response')
                                 :else
                                 response'))]
        (try
          (when (error-status? status)
            (throw (js/Error. (str "HTTP-request failed with status " status))))
          (when response-schema
            (s/validate response-schema body))
          (re-frame/dispatch (conj response-handler body))
          (catch js/Error e
            (js/console.error e)
            (when error-handler
              (re-frame/dispatch (conj error-handler body))))
          (finally
            (re-frame/dispatch [:http/remove-http-request-id http-request-id])))))))
