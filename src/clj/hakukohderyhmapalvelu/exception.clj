(ns hakukohderyhmapalvelu.exception
  (:require [reitit.ring.middleware.exception :as ring-exception]
            [ring.util.http-response :as response]
            [taoensso.timbre :as log]))

(def exception-middleware
  (ring-exception/create-exception-middleware
    (merge
      ring-exception/default-handlers
      {Throwable (fn [error _]
                   (log/error error)
                   (response/internal-server-error "Internal Server Error"))})))
