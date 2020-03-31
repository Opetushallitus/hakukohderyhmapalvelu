/// <reference types="cypress"/>

import * as hs from '../selectors/hakukohderyhmaPanelSelectors'
import * as hh from '../selectors/hakukohderyhmanHakutoimintoSelectors'
import * as hl from '../selectors/hakukohderyhmanLisaysSelectors'

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
    cy.get(hs.hakukohderyhmaPanelHeadingSelector).should(
      'have.text',
      'Hakukohderyhmien hallinta',
    )
  })
  describe('Haun hakutoiminto', () => {
    it('Näyttää haun hakutoiminnon otsikon', () => {
      cy.get(hh.haunHakutoimintoHeadingSelector).should('have.text', 'Haku')
    })
    describe('Haun hakutoiminnon "näytä myös päättyneet" -checkbox', () => {
      it('Näyttää haun hakutoiminnon "näytä myös päättyneet" -checkboxin', () => {
        cy.get(hh.haunHakutoimintoNaytaMyosPaattyneetCheckboxSelector).should(
          'have.attr',
          'type',
          'checkbox',
        )
      })
      it('Näyttää haun hakutoiminnon "näytä myös päättyneet" -tekstin', () => {
        cy.get(hh.haunHakutoimintoNaytaMyosPaattyneetTextSelector).should(
          'have.text',
          'Näytä myös päättyneet',
        )
      })
    })
    describe('Haun hakutoiminnon hakukenttä', () => {
      it('Näyttää haun hakutoiminnon hakukentän', () => {
        cy.get(hh.haunHakutoimintoTextInputSelector).should(
          'have.attr',
          'type',
          'text',
        )
      })
      it('Näyttää haun hakutoiminnon hakukentän ohjetekstin', () => {
        cy.get(hh.haunHakutoimintoTextInputPlaceholderSelector).should(
          'have.attr',
          'placeholder',
          'Haun nimi',
        )
      })
    })
  })
  describe('Hakukohderyhmän lisäystoiminto', () => {
    it('Näyttää hakukohderyhmän lisäyksen otsikon', () => {
      cy.get(hl.hakukohderyhmanLisaysHeadingSelector).should(
        'have.text',
        'Hakukohderyhmät',
      )
    })
    it('Näyttää pudotusvalikon, jossa ei valittuna hakukohderyhmää', () => {
      cy.get(hl.hakukohderyhmanLisaysClosedDropdownSelector).should(
        'have.text',
        'Hakukohderyhmä',
      )
    })
    it('Näyttää "lisää uusi ryhmä" -linkin', () => {
      cy.get(hl.hakukohderyhmanLisaysLisaaUusiRyhmaLinkSelector).should(
        'have.text',
        'Luo uusi ryhmä',
      )
    })
    it('Ei näytä hakukohderyhmän luonnin tekstikenttää', () => {
      cy.get(
        hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
      ).should('not.exist')
    })
    it('Ei näytä hakukohderyhmän luonnin tallennuspainiketta', () => {
      cy.get(
        hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
      ).should('not.exist')
    })
  })
})
