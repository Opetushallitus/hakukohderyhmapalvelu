(ns hakukohderyhmapalvelu.components.common.link
  (:require [hakukohderyhmapalvelu.browser-events :as events]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [schema.core :as s]
            [schema-tools.core :as st]
            [stylefy.core :as stylefy]))

(def ^:private link-styles
  {:color           colors/blue-lighten-2
   :text-decoration "none"})

(def ^:private link-left-margin-styles
  {:position      "relative"
   ::stylefy/mode [["::before" {:content     "''"
                                :width       "0px"
                                :height      "20px"
                                :border-left "1px solid #a2a2a2"
                                :position    "absolute"
                                :left        "-11px"
                                :top         "1px"}]]})

(s/defschema LinkProps
  {(s/optional-key :cypressid) s/Str
   :href                       s/Str
   :label                      s/Str})

(s/defschema LinkWithExtraStylesProps
  (st/merge LinkProps
            {(s/optional-key :styles) s/Any}))


(s/defn link :- s/Any
  [{:keys [cypressid
           label
           href
           styles]} :- LinkWithExtraStylesProps]
  [:a (stylefy/use-style
        (cond-> link-styles
                (not-empty styles)
                (merge styles))
        {:cypressid cypressid
         :href      href
         :on-click  events/preventDefault})
   label])

(s/defn link-with-left-separator :- s/Any
  [{:keys [cypressid
           href
           label]} :- LinkProps]
  [link {:cypressid cypressid
         :href      href
         :label     label
         :styles    link-left-margin-styles}])

