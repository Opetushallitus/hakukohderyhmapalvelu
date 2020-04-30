(ns hakukohderyhmapalvelu.authentication.schema
  (:require [schema.core :as s]
            [schema-tools.core :as st]))

(s/defschema Identity
  {:username                 s/Str
   :oid                      s/Str
   :ticket                   s/Str
   :last-name                s/Str
   :first-name               s/Str
   :lang                     s/Str
   :organizations            s/Any
   :user-right-organizations [s/Any]
   :superuser                s/Bool})

(s/defschema Session
  (st/open-schema
    {(s/optional-key :key)          s/Str
     :client-ip                     s/Str
     :user-agent                    s/Str
     (s/optional-key :original-url) s/Str
     (s/optional-key :identity)     Identity}))
