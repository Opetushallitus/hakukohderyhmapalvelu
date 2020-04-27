(ns hakukohderyhmapalvelu.audit-logger-protocol)

(defprotocol AuditLoggerProtocol
  (log [this user operation target changes]))
