/// <reference types="cypress" />
/// <reference types="./commands"/>

Cypress.Commands.add(
  'resetMocks',
  (): Cypress.Chainable<Cypress.Response> =>
    cy.request({
      method: 'POST',
      url: '/hakukohderyhmapalvelu/api/mock/reset',
      headers: { 'caller-id': 'cypres' },
    }),
)

Cypress.Commands.add(
  'mockBackendRequest',
  (opts: MockBackendRequestOpts): Cypress.Chainable<Cypress.Response> => {
    const req = opts.requestFixture
      ? cy.fixture(opts.requestFixture)
      : cy.wrap(null)
    return req
      .then(request =>
        cy
          .fixture(opts.responseFixture)
          .then(response => ({ request, response })),
      )
      .then(({ request, response }) =>
        cy.request({
          method: 'POST',
          url: '/hakukohderyhmapalvelu/api/mock/authenticating-client',
          headers: { 'caller-id': 'cypres' },
          body: {
            method: opts.method.toLowerCase(),
            path: opts.path,
            service: opts.service,
            request,
            response,
          },
        }),
      )
  },
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

Cypress.Commands.add(
  'mockBrowserRequest',
  (opts: MockBrowserRequestOpts): Cypress.Chainable<null> =>
    cy
      .fixture(opts.fixturePath)
      .then(response => cy.route2(opts.method, opts.path, response))
      .as(opts.responseAlias),
)
