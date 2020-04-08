(ns hakukohderyhmapalvelu.audit-log
  (:require [clj-timbre-auditlog.audit-log :as cta-audit-log]
            [com.stuartsierra.component :as component]
            [hakukohderyhmapalvelu.config :as c]
            [schema.core :as s])
  (:import [fi.vm.sade.auditlog ApplicationType Audit]))

(defn- ^Audit create-audit-log [base-path]
  (cta-audit-log/create-audit-logger "hakukohderyhmapalvelu" base-path ApplicationType/VIRKAILIJA))

(defprotocol AuditLogger
  (log [this user operation target changes]))

(defrecord OpintopolkuAuditLogger [config]
  component/Lifecycle

  (start [this]
    (s/validate c/HakukohderyhmaConfig config)
    (assoc this :audit-log (create-audit-log (-> config :log :base-path))))

  (stop [this]
    (assoc this :audit-log nil))

  AuditLogger
  (log [this user operation target changes]
    (.log (:audit-log this) user operation target changes)))
