(ns hakukohderyhmapalvelu.hakukohderyhma.fixtures)

(def fake-session
  {:identity
                                                 {:oid        "1.2.246.562.24.1"
                                                  :lang       "fi"
                                                  :ticket     "ticket-1"
                                                  :username   "testuser"
                                                  :last-name  "User"
                                                  :first-name "Test"}
   :client-ip                                    "127.0.0.1"
   :logged-in                                    true
   :user-agent                                   "Mozilla Firefox Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:53.0) Gecko/20100101 Firefox/53.0"
   :ring.middleware.session-timeout/idle-timeout 10000000
   :key                                          "c99b1ad2-5af0-441c-985d-dda7737df41e"})
