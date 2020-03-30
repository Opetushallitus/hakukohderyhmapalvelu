/// <reference types="cypress"/>

describe('Hakukohderyhmäpalvelu', () => {
  before(() => {
    cy.visit('/')
  })
  it('Ohjaa käyttäjän polkuun /hakukohderyhmapalvelu', () => {
    cy.location().should(location => {
      expect(location.pathname).to.equal('/hakukohderyhmapalvelu')
    })
  })
  it('Näyttää päätason otsikon', () => {
    cy.get('[cypressid=hakukohderyhmapalvelu-panel-heading]:visible').should(
      'have.text',
      'Hakukohderyhmien hallinta',
    )
  })
  describe('Haun hakutoiminto', () => {
    it('Näyttää haun hakutoiminnon otsikon', () => {
      cy.get('[cypressid=haku-search-label]:visible').should(
        'have.text',
        'Haku',
      )
    })
    describe('Haun hakutoiminnon "näytä myös päättyneet" -checkbox', () => {
      it('Näyttää haun hakutoiminnon "näytä myös päättyneet" -checkboxin', () => {
        cy.get('input[cypressid=haku-search-checkbox-input]:visible').should(
          'have.attr',
          'type',
          'checkbox',
        )
      })
      it('Näyttää haun hakutoiminnon "näytä myös päättyneet" -tekstin', () => {
        cy.get('label[cypressid=haku-search-checkbox-label]:visible').should(
          'have.text',
          'Näytä myös päättyneet',
        )
      })
    })
    describe('Haun hakutoiminnon hakukenttä', () => {
      it('Näyttää haun hakutoiminnon hakukentän', () => {
        cy.get('input[cypressid=haku-search-input]:visible').should(
          'have.attr',
          'type',
          'text',
        )
      })
      it('Näyttää haun hakutoiminnon hakukentän ohjetekstin', () => {
        cy.get('input[cypressid=haku-search-input]:visible').should(
          'have.attr',
          'placeholder',
          'Haun nimi',
        )
      })
    })
  })
  describe('Hakukohderyhmän lisäys', () => {
    it('Näyttää hakukohderyhmän lisäyksen otsikon', () => {
      cy.get('label[cypressid=hakukohderyhma-select-label]:visible').should(
        'have.text',
        'Hakukohderyhmät',
      )
    })
    it('Näyttää pudotusvalikon, jossa ei valittuna hakukohderyhmää', () => {
      cy.get(
        'span[cypressid=hakukohderyhma-select-dropdown-unselected-label]:visible',
      ).should('have.text', 'Hakukohderyhmä')
    })
    it('Näyttää "lisää uusi ryhmä" -linkin', () => {
      cy.get(
        'a[cypressid=hakukohderyhma-select-add-new-hakukohderyhma]:visible',
      ).should('have.text', 'Luo uusi ryhmä')
    })
  })
})
