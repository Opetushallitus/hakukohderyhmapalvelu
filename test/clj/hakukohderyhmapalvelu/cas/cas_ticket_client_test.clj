(ns hakukohderyhmapalvelu.cas.cas-ticket-client-test
  (:require [clojure.set :refer [union]]
            [clojure.test :refer [deftest is testing]]
            [hakukohderyhmapalvelu.cas.cas-ticket-client :as client]
            [hakukohderyhmapalvelu.cas.cas-ticket-client-protocol :as cas-ticket-client-protocol])
  (:import (fi.vm.sade.javautils.nio.cas UserDetails)))

(def test-henkilo-oid "1.2.246.562.98.12345678901")
(def test-org-oid "1.2.246.562.99.12345678901")
(def other-org-oid "1.2.246.562.99.00045678902")

(defn roles-for-org [service org]
  #{(str "ROLE_APP_" service "_CRUD")
    (str "ROLE_APP_" service "_CRUD_" org)})

(defn create-test-user-details [roles]
  (new UserDetails
       "virkahemmo"
       test-henkilo-oid
       "VIRKAILIJA"
       "usernamePassword"
       roles))

(def authorized-user-details
  (create-test-user-details
    (union (roles-for-org "HAKUKOHDERYHMAPALVELU" test-org-oid)
           (roles-for-org "ATARU" other-org-oid))))

(def super-user-details
  (create-test-user-details
    (union (roles-for-org "HAKUKOHDERYHMAPALVELU" client/oph-organisaatio-oid)
           (roles-for-org "HAKUKOHDERYHMAPALVELU" test-org-oid))))

(def unauthorized-user-details
  (create-test-user-details
    (conj (roles-for-org "ATARU" test-org-oid)
          "ROLE_APP_HAKUKOHDERYHMAPALVELU_CRUD")))

(deftest validate-service-ticket-test
  (testing "Palauttaa käyttäjän, kun CAS-vastaus on kelvollinen"
    (with-redefs [client/get-user-details (fn [_ _ _] authorized-user-details)]
      (let [impl (client/map->CasTicketClient {})
            expected {:oidHenkilo     test-henkilo-oid
                      :username       "virkahemmo"
                      :organisaatiot  (seq [test-org-oid])
                      :superuser      false}]
        (is (= expected (cas-ticket-client-protocol/validate-service-ticket impl "ticket"))))))
  (testing "Palauttaa käyttäjän super-userina kun hänellä on OPH-oikeus"
    (with-redefs [client/get-user-details (fn [_ _ _] super-user-details)]
      (let [impl (client/map->CasTicketClient {})
            expected {:oidHenkilo     test-henkilo-oid
                      :username       "virkahemmo"
                      :organisaatiot  (seq [client/oph-organisaatio-oid test-org-oid])
                      :superuser      true}]
        (is (= expected (cas-ticket-client-protocol/validate-service-ticket impl "ticket"))))))
  (testing "Palauttaa nil, kun käyttäjältä puuttuu oikea rooli"
    (with-redefs [client/get-user-details (fn [_ _ _] unauthorized-user-details)]
      (let [impl (client/map->CasTicketClient {})]
        (is (= nil (cas-ticket-client-protocol/validate-service-ticket impl "ticket")))))))
