(ns hakukohderyhmapalvelu.fx.dispatch-debounced-fx
  (:require [re-frame.core :as re-frame]
            [schema.core :as s]))

(defonce debounces (atom {}))

(re-frame/reg-fx
  :dispatch-debounced
  (s/fn dispatch-debounced-fx
    [{:keys [id
             dispatch
             timeout]} :- {:id       s/Keyword
                           :dispatch s/Any
                           :timeout  s/Int}]
    (js/clearTimeout (@debounces id))
    (swap! debounces assoc id (js/setTimeout
                                (fn []
                                  (re-frame/dispatch dispatch)
                                  (swap! debounces dissoc id))
                                timeout))))
