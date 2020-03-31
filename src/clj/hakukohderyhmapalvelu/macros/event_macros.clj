(ns hakukohderyhmapalvelu.macros.event-macros)

(defmacro reg-event-db-validating [event-kwd handler-fn]
  `(re-frame.core/reg-event-db
     ~event-kwd
     [re-frame.core/trim-v hakukohderyhmapalvelu.interceptors.app-db-validating-interceptor/validate-interceptor]
     ~handler-fn))

(defmacro reg-event-fx-validating [event-kwd handler-fn]
  `(re-frame.core/reg-event-fx
     ~event-kwd
     [re-frame.core/trim-v hakukohderyhmapalvelu.interceptors.app-db-validating-interceptor/validate-interceptor]
     ~handler-fn))
