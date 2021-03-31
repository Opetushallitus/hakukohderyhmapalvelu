(ns hakukohderyhmapalvelu.components.common.react-select
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [re-frame.core :refer [subscribe]]
            [react-select :default Select]
            [schema.core :as s]))


(def ^:private theme {:borderRadius 4
                      :colors       {:primary     colors/blue
                                     :primary75   "#4C9AFF"
                                     :primary50   "#9fe3f9"
                                     :primary25   "#cff1fc"
                                     :danger      "#DE350B"
                                     :dangerLight "#FFBDAD"
                                     :neutral0    "hsl(0, 0%, 100%)"
                                     :neutral5    colors/gray-lighten-6
                                     :neutral10   "#CCCCCC"
                                     :neutral20   "hsl(0, 0%, 80%)"
                                     :neutral30   "hsl(0, 0%, 70%)"
                                     :neutral40   "hsl(0, 0%, 60%)"
                                     :neutral50   "hsl(0, 0%, 50%)"
                                     :neutral60   "hsl(0, 0%, 40%)"
                                     :neutral70   "hsl(0, 0%, 30%)"
                                     :neutral80   "hsl(0, 0%, 20%)"
                                     :neutral90   "hsl(0, 0%, 10%)"}
                      :spacing      {:baseUnit      4
                                     :controlHeight 38
                                     :menuGutter    8}})

(defn- create-on-change-handler [on-change-fns]
  (fn [js-selected-option js-event]
    (let [event (js->clj js-event :keywordize-keys true)
          action-type (keyword (:action event))
          selected-option (js->clj js-selected-option :keywordize-keys true)
          handler (get on-change-fns action-type)]
      (when handler
        (handler selected-option)))))

(s/defschema Option
  {:value                        s/Any
   :label                        s/Str
   (s/optional-key :is-selected) s/Bool})

(s/defschema SelectProps
  {:options                      [Option]
   :on-select-fn                 s/Any
   (s/optional-key :on-clear-fn) s/Any
   (s/optional-key :placeholder) s/Str
   (s/optional-key :is-disabled) s/Bool
   (s/optional-key :is-loading)  s/Bool
   :value                        (s/maybe s/Any)})

(s/defn select [{:keys [options
                        placeholder
                        is-loading
                        is-disabled
                        on-select-fn
                        on-clear-fn
                        value]} :- SelectProps]
  (let [no-options-message @(subscribe [:translation :no-selectable-items])
        on-change-fns {:select-option on-select-fn
                       :clear         on-clear-fn}
        props {:isDisabled       (boolean is-disabled)
               :isLoading        (boolean is-loading)
               :isClearable      (fn? on-clear-fn)
               :noOptionsMessage (constantly no-options-message)
               :onChange         (create-on-change-handler on-change-fns)
               :options          options
               :placeholder      placeholder
               :theme            theme
               :value            value}]
    [:> Select props]))
