/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

describe('Hakukohderyhmäpalvelun health check', () => {
  before(() => {
    cy.visit('/hakukohderyhmapalvelu/api/health', {
      headers: { 'caller-id': 'cypress' },
    })
  })
  it('Näyttää health check -tarkistuksen läpimenosta kertovan viestin', () => {
    cy.get('body').should(
      'have.text',
      'Hakukohderyhmäpalvelu vaikuttaa olevan OK.',
    )
  })
})
