/// <reference types="cypress" />

type Method = 'POST'

type Service = 'organisaatio-service'

type MockBackendRequestOpts = {
  method: Method
  path: string
  service: Service
  requestFixture: string
  responseFixture: string
}

declare namespace Cypress {
  interface Chainable {
    resetMocks(): Chainable<Element>

    mockBackendRequest(opts: MockBackendRequestOpts): Chainable<Element>
  }
}
