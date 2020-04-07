(ns hakukohderyhmapalvelu.cas.mock.mock-dispatcher-protocol)

(defprotocol MockDispatcherProtocol
  (dispatch-mock [this spec])

  (reset-mocks [this]))
