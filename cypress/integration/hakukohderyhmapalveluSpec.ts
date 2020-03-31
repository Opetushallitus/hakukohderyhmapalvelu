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
    it('Näyttää haun hakutoiminnon', () => {
      cy.get(hh.haunHakutoimintoHeadingSelector).should('have.text', 'Haku')
      cy.get(hh.haunHakutoimintoNaytaMyosPaattyneetCheckboxSelector).should(
        'have.attr',
        'type',
        'checkbox',
      )
      cy.get(hh.haunHakutoimintoNaytaMyosPaattyneetTextSelector).should(
        'have.text',
        'Näytä myös päättyneet',
      )
      cy.get(hh.haunHakutoimintoTextInputSelector).should(
        'have.attr',
        'type',
        'text',
      )
      cy.get(hh.haunHakutoimintoTextInputPlaceholderSelector).should(
        'have.attr',
        'placeholder',
        'Haun nimi',
      )
    })
  })
  describe('Hakukohderyhmän lisäystoiminto', () => {
    it('Näyttää hakukohderyhmän lisäysnäkymän', () => {
      cy.get(hl.hakukohderyhmanLisaysHeadingSelector).should(
        'have.text',
        'Hakukohderyhmät',
      )
      cy.get(hl.hakukohderyhmanLisaysClosedDropdownSelector).should(
        'have.text',
        'Hakukohderyhmä',
      )
      cy.get(hl.hakukohderyhmanLisaysLisaaUusiRyhmaLinkSelector).should(
        'have.text',
        'Luo uusi ryhmä',
      )
      cy.get(
        hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
      ).should('not.exist')
      cy.get(
        hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
      ).should('not.exist')
    })
    describe('Uuden hakukohderyhmän lisäys', () => {
      before('"Lisää hakukohderyhmä" -linkin klikkaus', () => {
        cy.get(hl.hakukohderyhmanLisaysLisaaUusiRyhmaLinkSelector).click({
          force: true,
        })
      })
      it('Näyttää hakukohderyhmän luonnin tekstikentän ja sen ohjetekstin', () => {
        cy.get(
          hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
        ).should('have.attr', 'placeholder', 'Ryhmän nimi')
        cy.get(
          hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
        ).should('be.disabled')
      })
      describe('Uuden hakukohderyhmän nimen kirjoittaminen', () => {
        before(() => {
          cy.fixture('new-hakukohderyhma.json').then(newHakukohderyhma =>
            cy
              .get(
                hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
              )
              .type(newHakukohderyhma.hakukohderyhmanNimi, { force: true }),
          )
        })
        it('Hakukohderyhmän tallennuspainiketta voi klikata', () => {
          cy.get(
            hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
          ).should('be.not.disabled')
        })
      })
    })
  })
})
