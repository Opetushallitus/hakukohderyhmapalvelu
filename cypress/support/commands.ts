/// <reference types="cypress" />
/// <reference types="./commands"/>

Cypress.Commands.add(
  'resetMocks',
  (): Cypress.Chainable<Cypress.Response> =>
    cy.request('POST', '/hakukohderyhmapalvelu/api/mock/reset'),
)

Cypress.Commands.add(
  'mockBackendRequest',
  (opts: MockBackendRequestOpts): Cypress.Chainable<Cypress.Response> => {
    return cy
      .fixture(opts.requestFixture)
      .then(request =>
        cy
          .fixture(opts.responseFixture)
          .then(response => ({ request, response })),
      )
      .then(({ request, response }) => {
        return cy.request(
          'POST',
          '/hakukohderyhmapalvelu/api/mock/authenticating-client',
          {
            method: opts.method.toLowerCase(),
            path: opts.path,
            service: opts.service,
            request,
            response,
          },
        )
      })
  },
)
