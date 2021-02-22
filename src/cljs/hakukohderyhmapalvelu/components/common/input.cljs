(ns hakukohderyhmapalvelu.components.common.input
  (:require [hakukohderyhmapalvelu.components.common.material-icons :as icon]
            [hakukohderyhmapalvelu.debounce :as d]
            [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [hakukohderyhmapalvelu.styles.styles-effects :as effects]
            [hakukohderyhmapalvelu.styles.styles-fonts :as fonts]
            [hakukohderyhmapalvelu.styles.layout-styles :as layout]
            [hakukohderyhmapalvelu.validators.input-date-validator :as idv]
            [hakukohderyhmapalvelu.validators.input-date-time-validator :as idtv]
            [hakukohderyhmapalvelu.validators.input-number-validator :as inv]
            [hakukohderyhmapalvelu.validators.input-text-validator :as itv]
            [hakukohderyhmapalvelu.validators.input-time-validator :as itiv]
            [reagent.core :as reagent]
            [schema.core :as s]
            [stylefy.core :as stylefy]))

(def input-row-height "40px")

(def ^:private input-container-styles
  (let [input-left-right-padding "10px"]
    {:border-width  "2px"
     :border-style  "solid"
     :border-color  colors/gray-lighten-3
     :border-radius "3px"
     :box-sizing    "border-box"
     :box-shadow    effects/inset-box-shadow-effect-black
     :color         colors/black
     :font-size     "16px"
     :font-weight   fonts/font-weight-regular
     :height        input-row-height
     :padding       (str "0 " input-left-right-padding)
     :width         "100%"}))

(def ^:private input-text-styles
  (merge input-container-styles
         {:outline       "none"
          :position      "relative"
          ::stylefy/mode [["::placeholder" {:color colors/gray-lighten-1}]
                          [:focus {:border-color colors/blue-lighten-2
                                   :box-shadow   effects/inset-box-shadow-effect-blue}]
                          [:disabled {:background-color colors/gray-lighten-5}]]}))

(def ^:private input-text-invalid-styles
  (merge
    input-text-styles
    {:border-color  colors/red
     :color         colors/red
     ::stylefy/mode [["::placeholder" {:color colors/red}]
                     [:focus {:border-color colors/red
                              :box-shadow   effects/inset-box-shadow-effect-blue}]
                     [:disabled {:background-color colors/red}]]}))

(def ^:private input-date-time-styles
  {})

(def ^:private input-date-time-invalid-styles
  (merge
    input-date-time-styles
    {:color "red"}))

(def ^:private input-debounce-timeout 500)

(defn input-text
  []
  (let [on-change-debounced (d/debounce
                              (fn [on-change value]
                                (on-change value))
                              input-debounce-timeout)
        invalid?            (reagent/atom false)]
    (s/fn render-input-text
      [{:keys [cypressid
               input-id
               on-change
               placeholder
               aria-label
               aria-describedby]} :- {(s/optional-key :cypressid)        s/Str
                                      :input-id                          s/Str
                                      :on-change                         s/Any
                                      :placeholder                       s/Str
                                      :aria-label                        s/Str
                                      (s/optional-key :aria-describedby) s/Str}]
      (let [validate (itv/input-text-validator)]
        [:input (stylefy/use-style
                  (cond-> input-text-styles
                          @invalid?
                          (merge input-text-invalid-styles))
                  {:cypressid        cypressid
                   :id               input-id
                   :on-change        (fn [event]
                                       (let [value  (.. event -target -value)
                                             valid? (validate value)]
                                         (reset! invalid? (not valid?))
                                         (when valid?
                                           (on-change-debounced on-change value))))
                   :placeholder      placeholder
                   :type             "text"
                   :aria-label       aria-label
                   :aria-describedby aria-describedby})]))))

(s/defn input-number :- s/Any
  [{:keys [value]} :- {(s/optional-key :value) s/Str
                       s/Any                   s/Any}]
  (let [debounced       (d/debounce
                         (fn [handler & args]
                           (apply handler args))
                         input-debounce-timeout)
        prev-prop-value (atom value)
        local-value     (reagent/atom value)]
    (s/fn render-input-number
      [{:keys [input-id
               value
               required?
               on-change
               placeholder
               aria-label
               min
               disabled?
               cypressid]} :- {:input-id                     s/Str
                               (s/optional-key :value)       s/Str
                               :required?                    s/Bool
                               :on-change                    s/Any
                               (s/optional-key :placeholder) s/Str
                               (s/optional-key :aria-label)  s/Str
                               :min                          s/Int
                               (s/optional-key :disabled?)   s/Bool
                               (s/optional-key :cypressid)   s/Str}]
      (when (not= value @prev-prop-value)
        (reset! prev-prop-value value)
        (reset! local-value value))
      (let [validate (inv/input-number-validator
                       {:min       min
                        :required? required?})]
        [:input (stylefy/use-style
                  (cond-> input-text-styles
                          (not (validate @local-value))
                          (merge input-text-invalid-styles))
                  (merge {:id        input-id
                          :value     @local-value
                          :on-change (fn [event]
                                       (let [value' (.. event -target -value)]
                                         (reset! local-value value')
                                         (when (validate value')
                                           (debounced on-change value'))))
                          :type      "number"
                          :min       min
                          :required  required?
                          :disabled  disabled?
                          :cypressid cypressid}
                         (when placeholder
                           {:placeholder placeholder})
                         (when aria-label
                           {:aria-label aria-label})))]))))

(s/defn input-date :- s/Any
  [{:keys [value]} :- {(s/optional-key :value) s/Str
                       s/Any                   s/Any}]
  (let [debounced       (d/debounce
                         (fn [handler & args]
                           (apply handler args))
                         input-debounce-timeout)
        prev-prop-value (atom value)
        local-value     (reagent/atom value)]
    (s/fn render-input-date
      [{:keys [id
               value
               required?
               disabled?
               on-change
               aria-describedby
               aria-labelledby]} :- {:id                                s/Str
                                     (s/optional-key :value)            s/Str
                                     :required?                         s/Bool
                                     :disabled?                         s/Bool
                                     :on-change                         s/Any
                                     (s/optional-key :aria-describedby) s/Str
                                     (s/optional-key :aria-labelledby)  s/Str}]
      (when (not= value @prev-prop-value)
        (reset! prev-prop-value value)
        (reset! local-value value))
      (let [validate (idv/input-date-validator
                      {:required? required?})]
        [:input (stylefy/use-style
                 (cond-> input-date-time-styles
                         (not (validate @local-value "date"))
                         (merge input-date-time-invalid-styles))
                 {:id               id
                  :type             "date"
                  :value            @local-value
                  :on-change        (fn [event]
                                      (let [value' (.. event -target -value)
                                            type   (.. event -target -type)]
                                        (reset! local-value value')
                                        (when (validate value' type)
                                          (debounced on-change value'))))
                  :required         required?
                  :disabled         disabled?
                  :aria-describedby aria-describedby
                  :aria-labelledby  aria-labelledby})]))))

(s/defn input-time :- s/Any
  [{:keys [value]} :- {(s/optional-key :value) s/Str
                       s/Any                   s/Any}]
  (let [debounced       (d/debounce
                         (fn [handler & args]
                           (apply handler args))
                         input-debounce-timeout)
        prev-prop-value (atom value)
        local-value     (reagent/atom value)]
    (s/fn render-input-time
      [{:keys [id
               value
               on-change
               required?
               disabled?
               aria-describedby
               aria-labelledby]} :- {:id                                s/Str
                                     (s/optional-key :value)            s/Str
                                     :required?                         s/Bool
                                     :disabled?                         s/Bool
                                     :on-change                         s/Any
                                     (s/optional-key :aria-describedby) s/Str
                                     (s/optional-key :aria-labelledby)  s/Str}]
      (when (not= value @prev-prop-value)
        (reset! prev-prop-value value)
        (reset! local-value value))
      (let [validate (itiv/input-time-validator
                      {:required? required?})]
        [:input (stylefy/use-style
                 (cond-> input-date-time-styles
                         (not (validate @local-value "time"))
                         (merge input-date-time-invalid-styles))
                 {:id               id
                  :type             "time"
                  :value            @local-value
                  :on-change        (fn [event]
                                      (let [value' (.. event -target -value)
                                            type   (.. event -target -type)]
                                        (reset! local-value value')
                                        (when (validate value' type)
                                          (debounced on-change value'))))
                  :required         required?
                  :disabled         disabled?
                  :aria-describedby aria-describedby
                  :aria-labelledby  aria-labelledby})]))))

(s/defn input-datetime-local :- s/Any
  [{:keys [value]} :- {(s/optional-key :value) s/Str
                       s/Any                   s/Any}]
  (let [debounced       (d/debounce
                         (fn [handler & args]
                           (apply handler args))
                         input-debounce-timeout)
        prev-prop-value (atom value)
        local-value     (reagent/atom value)]
    (s/fn render-input-datetime-local
      [{:keys [id
               value
               required?
               disabled?
               on-change]} :- {:id                     s/Str
                               :required?              s/Bool
                               :disabled?              s/Bool
                               (s/optional-key :value) s/Str
                               :on-change              s/Any}]
      (when (not= value @prev-prop-value)
        (reset! prev-prop-value value)
        (reset! local-value value))
      (let [validate (idtv/input-date-time-validator
                       {:required? required?})]
        [:input (stylefy/use-style
                  (cond-> input-date-time-styles
                          (not (validate @local-value "datetime-local"))
                          (merge input-date-time-invalid-styles))
                  {:id        id
                   :value     @local-value
                   :type      "datetime-local"
                   :required  required?
                   :disabled  disabled?
                   :on-change (fn [event]
                                (let [value' (.. event -target -value)
                                      type   (.. event -target -type)]
                                  (reset! local-value value')
                                  (when (validate value' type)
                                    (debounced on-change value'))))})]))))
