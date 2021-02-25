(ns hakukohderyhmapalvelu.components.common.react-select
  (:require [hakukohderyhmapalvelu.styles.styles-colors :as colors]
            [re-frame.core :refer [subscribe]]
            [react-select :default Select]
            [schema.core :as s]))


(def theme {:borderRadius 4
            :colors       {:primary     colors/blue
                           :primary75   "#4C9AFF"
                           :primary50   "#9fe3f9"
                           :primary25   "#cff1fc"
                           :danger      "#DE350B"
                           :dangerLight "#FFBDAD"
                           :neutral0    "hsl(0, 0%, 100%)"
                           :neutral5    "hsl(0, 0%, 95%)"
                           :neutral10   "hsl(0, 0%, 90%)"
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

(s/defschema SelectProps
  {:options                      [{:value s/Any :label s/Str}]
   (s/optional-key :placeholder) s/Str
   (s/optional-key :is-disabled) s/Bool
   (s/optional-key :is-loading)  s/Bool})

(s/defn select [{:keys [options placeholder is-loading is-disabled]} :- SelectProps]
  (let [no-options-message @(subscribe [:translation :no-selectable-items])
        props {:disabled    (boolean is-disabled)
               :isLoading   (boolean is-loading)
               :isClearable true
               :noOptionsMessage (constantly no-options-message)
               :options     options
               :placeholder placeholder
               :theme       theme}]
    [:> Select props]))
