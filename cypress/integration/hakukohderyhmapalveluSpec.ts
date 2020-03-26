/// <reference types="cypress"/>

describe('Hakukohderyhmäpalvelu', () => {
  before(() => {
    cy.visit('/')
  })
  it('Redirects to /hakukohderyhmapalvelu context path', () => {
    cy.location().should(location => {
      expect(location.pathname).to.equal('/hakukohderyhmapalvelu')
    })
  })
  it('Shows the main heading', () => {
    cy.get('#hakukohderyhmapalvelu-panel-heading').should(heading => {
      expect(heading).to.have.text('Hakukohderyhmien hallinta')
    })
  })
})
