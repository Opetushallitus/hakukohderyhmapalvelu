(ns hakukohderyhmapalvelu.components.common.checkbox
  (:require [schema.core :as s]
            [stylefy.core :as stylefy]))

(def ^:private checkbox-styles
  {:cursor "pointer"})

(s/defn checkbox
  [{:keys [id
           on-change
           checked?
           disabled?
           cypressid]} :- {:id                         s/Str
                           :on-change                  s/Any
                           :checked?                   s/Bool
                           (s/optional-key :disabled?) s/Bool
                           (s/optional-key :cypressid) s/Str}]

  [:input
   (stylefy/use-style
     checkbox-styles
     {:id        id
      :cypressid cypressid
      :checked   checked?
      :disabled  disabled?
      :on-change on-change
      :type      "checkbox"})])
