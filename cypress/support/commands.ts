/// <reference types="cypress" />
/// <reference types="./commands"/>

Cypress.Commands.add(
  'resetMocks',
  (): Cypress.Chainable<Cypress.Response> =>
    cy.request('POST', '/hakukohderyhmapalvelu/api/mock/reset'),
)

Cypress.Commands.add(
  'mockBackendRequest',
  (opts: MockBackendRequestOpts): Cypress.Chainable<Cypress.Response> =>
    cy
      .fixture(opts.requestFixture)
      .then(request =>
        cy
          .fixture(opts.responseFixture)
          .then(response => ({ request, response })),
      )
      .then(({ request, response }) =>
        cy.request(
          'POST',
          '/hakukohderyhmapalvelu/api/mock/authenticating-client',
          {
            method: opts.method.toLowerCase(),
            path: opts.path,
            service: opts.service,
            request,
            response,
          },
        ),
      ),
)

Cypress.Commands.add(
  'login',
  (): Cypress.Chainable<Cypress.Cookie | null> =>
    cy
      .request(
        'get',
        `/hakukohderyhmapalvelu/auth/cas?ticket=any_unique_ticket_is_good_for_fake_authentication-${Math.random()}`,
      )
      .then(() => cy.getCookie('ring-session').should('exist')),
)
