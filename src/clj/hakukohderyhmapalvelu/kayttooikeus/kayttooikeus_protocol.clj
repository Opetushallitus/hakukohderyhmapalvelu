(ns hakukohderyhmapalvelu.kayttooikeus.kayttooikeus-protocol
  (:require [schema.core :as s]))

(s/defschema Kayttooikeus
  {:palvelu s/Str
   :oikeus  s/Str})

(s/defschema KayttooikeudetOrganisaatiossa
  {:organisaatioOid s/Str
   :kayttooikeudet  [Kayttooikeus]})

(s/defschema Virkailija
  {:oidHenkilo     s/Str
   :username       s/Str
   :kayttajaTyyppi s/Str
   :organisaatiot  [KayttooikeudetOrganisaatiossa]})

(defprotocol KayttooikeusService
  (virkailija-by-username [this username]))
