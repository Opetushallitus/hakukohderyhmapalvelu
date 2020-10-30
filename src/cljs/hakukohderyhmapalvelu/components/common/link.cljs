(ns hakukohderyhmapalvelu.components.common.link
  (:require [hakukohderyhmapalvelu.schemas.props-schemas :as ps]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [schema.core :as s]
            [schema-tools.core :as st]
            [stylefy.core :as stylefy]))

(def ^:private link-styles
  {:color         colors/blue-lighten-2
   ::stylefy/mode [[:hover {:text-decoration "none"}]]})

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
  {(s/optional-key :cypressid)        s/Str
   :href                              s/Str
   :label                             s/Str
   (s/optional-key :target)           s/Str
   (s/optional-key :on-click)         s/Any
   (s/optional-key :aria-describedby) s/Str
   (s/optional-key :role)             s/Str
   (s/optional-key :tabindex)         s/Int})

(s/defschema LinkWithExtraStylesProps
  (st/merge LinkProps
            {(s/optional-key :styles) ps/Styles}))

(s/defn link :- s/Any
  [{:keys [cypressid
           label
           href
           target
           on-click
           styles
           aria-describedby
           role
           tabindex]} :- LinkWithExtraStylesProps]
  [:a (stylefy/use-style
       (merge link-styles styles)
       (merge {:cypressid cypressid
               :href      href}
              (when target
                {:target target})
              (when on-click
                {:on-click (fn prevent-default-and-click [event]
                             (.preventDefault event)
                             (on-click event))})
              (when aria-describedby
                {:aria-describedby aria-describedby})
              (when role
                {:role role})
              (when tabindex
                {:tabIndex tabindex})))
   label])

(s/defn link-with-left-separator :- s/Any
  [{:keys [cypressid
           href
           label
           on-click]} :- LinkProps]
  [link {:cypressid cypressid
         :href      href
         :label     label
         :on-click  on-click
         :styles    link-left-margin-styles}])

