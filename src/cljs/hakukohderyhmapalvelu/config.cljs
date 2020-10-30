(ns hakukohderyhmapalvelu.config
  (:require [clojure.walk :as walk]
            [hakukohderyhmapalvelu.public-config-schemas :as cs]
            [schema.core :as s]))

(def ^:private keywordize-vals #{:environment
                                 :default-panel})

(s/defn make-config :- cs/PublicConfig
  []
  (let [parsed-object (-> js/frontendConfig
                          (js->clj :keywordize-keys true))]
    (walk/prewalk
      (fn prewalk-config [x]
        (cond->> x
                 (map? x)
                 (reduce-kv (fn reduce-val->kwd [acc k v]
                              (assoc acc
                                     k
                                     (cond-> v
                                             (contains? keywordize-vals k)
                                             keyword)))
                            {})))
      parsed-object)))


(def config
  (make-config))
