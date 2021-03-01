/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

import * as hs from '../selectors/hakukohderyhmaPanelSelectors'
import * as hh from '../selectors/hakukohderyhmanHakutoimintoSelectors'
import * as hl from '../selectors/hakukohderyhmanLisaysSelectors'
import { PostHakukohderyhmaRequestFixture } from '../fixtures/hakukohderyhmapalvelu/PostHakukohderyhmaRequestFixture'

describe('Hakukohderyhmäpalvelu', () => {
  const mockHaut = () => {
    cy.login()
    cy.mockBackendRequest({
      method: 'GET',
      path: '/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001',
      service: 'kouta-service',
      responseFixture: 'hakukohderyhmapalvelu/get-haku-response.json',
    })
    cy.login()
  }

  before(() => {
    cy.resetMocks()
    mockHaut()
    cy.visit('/')
  })
  it('Ohjaa käyttäjän polkuun /hakukohderyhmapalvelu/hakukohderyhmien-hallinta', () => {
    cy.location().should(location => {
      expect(location.pathname).to.equal(
        '/hakukohderyhmapalvelu/hakukohderyhmien-hallinta',
      )
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
      cy.get(hh.haunHakutoimintoDivSelector).should('exist')
      mockHaut()
      cy.get(hh.haunHakutoimintoNaytaMyosPaattyneetCheckboxSelector)
        .should('have.attr', 'aria-checked', 'false')
        .should('have.attr', 'aria-disabled', 'false')
        .click({ force: true })
        .should('have.attr', 'aria-checked', 'true')
        .should('have.attr', 'aria-disabled', 'false')
      cy.get(hh.haunHakutoimintoNaytaMyosPaattyneetTextSelector).should(
        'have.text',
        'Näytä myös päättyneet',
      )
    })
  })
  describe('Hakukohderyhmän lisäystoiminto', () => {
    it('Näyttää hakukohderyhmän lisäysnäkymän', () => {
      cy.get(hl.hakukohderyhmanLisaysHeadingSelector).should(
        'have.text',
        'Hakukohderyhmät',
      )
      cy.get(hl.hakukohderyhmanLisaysDropdownSelector).should(
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
          cy.fixture<PostHakukohderyhmaRequestFixture>(
            'hakukohderyhmapalvelu/post-hakukohderyhma-request.json',
          )
            .as('post-hakukohderyhma-request')
            .then(hakukohderyhma =>
              cy
                .get(
                  hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
                )
                .type(hakukohderyhma.nimi.fi, { force: true }),
            )
        })
        it('Hakukohderyhmän tallennuspainiketta voi klikata', () => {
          cy.get(
            hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
          ).should('be.not.disabled')
        })
        describe('Hakukohderyhmän tallentaminen', () => {
          before(() => {
            cy.login()
            cy.fixture(
              'hakukohderyhmapalvelu/post-hakukohderyhma-request.json',
            ).as('post-hakukohderyhma-request')
            cy.fixture(
              'hakukohderyhmapalvelu/post-hakukohderyhma-response.json',
            ).as('post-hakukohderyhma-response')
            cy.mockBackendRequest({
              method: 'POST',
              path: '/organisaatio-service/rest/organisaatio/v4',
              service: 'organisaatio-service',
              requestFixture:
                'hakukohderyhmapalvelu/post-hakukohderyhma-request.json',
              responseFixture:
                'hakukohderyhmapalvelu/post-hakukohderyhma-response.json',
            })
            cy.server()
            cy.route('POST', '/hakukohderyhmapalvelu/api/hakukohderyhma').as(
              'post-hakukohderyhma',
            )
          })
          it('Tallentaa hakukohderyhmän', () => {
            cy.get(
              hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
            ).click({ force: true })
            cy.get(
              hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
            ).should('be.enabled')
            cy.get<PostHakukohderyhmaRequestFixture>(
              '@post-hakukohderyhma-request',
            ).then(hakukohderyhma =>
              cy
                .get(
                  hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
                )
                .should('have.value', hakukohderyhma.nimi.fi),
            )
          })
        })
      })
    })
  })
})
