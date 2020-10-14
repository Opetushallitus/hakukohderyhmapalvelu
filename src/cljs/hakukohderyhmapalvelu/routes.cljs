(ns hakukohderyhmapalvelu.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:import [goog History]
           [goog.history EventType])
  (:require [secretary.core :as secretary]
            [goog.events :as gevents]
            [re-frame.core :as re-frame]))

(defn hook-browser-navigation! []
  (doto (History.)
    (gevents/listen
      EventType/NAVIGATE
      (fn [^js event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes []
  (secretary/set-config! :prefix "#")
  (defroute "/" []
            (secretary/dispatch! "/hakukohderyhmapalvelu"))
  (defroute "/hakukohderyhmapalvelu" []
            (re-frame/dispatch [:panel-menu/set-active-panel :panel-menu/hakukohderyhmapalvelu-panel]))
  (hook-browser-navigation!))
