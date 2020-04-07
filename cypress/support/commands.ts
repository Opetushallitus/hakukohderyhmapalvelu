/// <reference types="cypress" />
/// <reference types="./commands"/>

Cypress.Commands.add('resetMocks', () => {
  cy.request('POST', '/hakukohderyhmapalvelu/api/mock/reset')
})

Cypress.Commands.add('mockBackendRequest', (opts: MockBackendRequestOpts) => {
  cy.fixture(opts.requestFixture)
    .then(request =>
      cy
        .fixture(opts.responseFixture)
        .then(response => ({ request, response })),
    )
    .then(({ request, response }) => {
      cy.request('POST', '/hakukohderyhmapalvelu/api/mock/cas-client', {
        method: opts.method.toLowerCase(),
        path: opts.path,
        service: opts.service,
        request,
        response,
      })
    })
})
