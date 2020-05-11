(ns hakukohderyhmapalvelu.authentication.schema
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema Identity
  {:username                 s/Str
   :oid                      s/Str
   :ticket                   s/Str
   :last-name                s/Str
   :first-name               s/Str
   :lang                     s/Str})

(s/defschema Session
  (st/open-schema
    {(s/optional-key :key)          s/Str
     (s/optional-key :client-ip)    s/Str
     (s/optional-key :user-agent)   s/Str
     (s/optional-key :original-url) s/Str
     (s/optional-key :identity)     Identity}))
