/// <reference types="cypress" />

type Method = 'GET' | 'POST'

type Service = 'organisaatio-service' | 'kouta-service'

interface MockBackendRequestOpts {
  method: Method
  path: string
  service: Service
  requestFixture?: string
  responseFixture: string
}

interface MockBrowserRequestOpts {
  method: Method
  path: string
  fixturePath: string
  responseAlias: string
}

declare namespace Cypress {
  interface Chainable {
    resetMocks(): Chainable<Response>

    mockBackendRequest(opts: MockBackendRequestOpts): Chainable<Response>

    login(): Chainable<Cypress.Cookie | null>

    mockBrowserRequest<T>(opts: MockBrowserReqestOpts): Chainable<null>
  }
}
