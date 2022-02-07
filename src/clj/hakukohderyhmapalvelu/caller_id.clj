(ns hakukohderyhmapalvelu.caller-id
  (:require [schema.core :as s]))

(s/defn make-caller-id :- s/Str
  [organisaatio-oid :- s/Str]
  (str organisaatio-oid ".hakukohderyhmapalvelu.backend.local"))
