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
    resetMocks(): Chainable<Response>

    mockBackendRequest(opts: MockBackendRequestOpts): Chainable<Response>

    login(): Chainable<Cypress.Cookie | null>
  }
}
