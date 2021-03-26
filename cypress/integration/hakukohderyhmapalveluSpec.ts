/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

import * as hs from '../selectors/hakukohderyhmaPanelSelectors'
import * as hh from '../selectors/hakukohderyhmanHakutoimintoSelectors'
import * as hl from '../selectors/hakukohderyhmanLisaysSelectors'
import { PostHakukohderyhmaRequestFixture } from '../fixtures/hakukohderyhmapalvelu/PostHakukohderyhmaRequestFixture'
import { PutHakukohderyhmaRequestFixture } from '../fixtures/hakukohderyhmapalvelu/PutHakukohderyhmaRequestFixture'

describe('Hakukohderyhmäpalvelu', () => {
  before(() => {
    cy.login()
    cy.mockBackendRequest({
      method: 'GET',
      path: '/organisaatio-service/rest/organisaatio/v3/ryhmat',
      service: 'organisaatio-service',
      responseFixture:
        'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
    })
    cy.login()
    cy.mockBackendRequest({
      method: 'GET',
      path: '/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001',
      service: 'kouta-service',
      responseFixture: 'hakukohderyhmapalvelu/get-haku-response.json',
    })
    cy.login()
    cy.mockBackendRequest({
      method: 'GET',
      path: '/kouta-internal/hakukohde/search?haku=1.2.3.4.5.3',
      service: 'kouta-service',
      responseFixture: 'hakukohderyhmapalvelu/get-hakukohde-response.json',
    })

    cy.login()
    cy.mockBackendRequest({
      method: 'POST',
      path: '/kouta-internal/hakukohde/findbyoids',
      service: 'kouta-service',
      requestFixture:
        'hakukohderyhmapalvelu/post-find-hakukohteet-by-oids.json',
      responseFixture: 'hakukohderyhmapalvelu/get-hakukohde-response.json',
    })

    cy.login()
    cy.mockBackendRequest({
      method: 'POST',
      path: '/organisaatio-service/rest/organisaatio/v4/findbyoids',
      service: 'organisaatio-service',
      requestFixture:
        'hakukohderyhmapalvelu/post-find-organisaatiot-request.json',
      responseFixture:
        'hakukohderyhmapalvelu/post-find-organisaatiot-response.json',
    })
    cy.login()

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
      cy.login()
      cy.get(hh.haunHakutoimintoDivSelector).should('exist')
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

    it('Haun valinta - näyttää hakukohteen', () => {
      cy.login()
      cy.get(hh.hakukohteetContainerSelector)
        .children()
        .should('have.length', 0)
      cy.get(hh.haunHakutoimintoDivSelectorChildDivs)
        .eq(1)
        .type('Testihaku 3{enter}')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .should('have.length', 2)

      cy.get(hh.hakukohteetContainerSelector)
        .children()
        .eq(0)
        .should($el => {
          expect($el.text()).to.equal('Testi-perustutkinto')
        })

      cy.get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .type('Ei pitäisi löytyä mitään')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .should('have.length', 0)

      cy.get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .type('Organisaatio 1')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .eq(0)
        .should($el => {
          expect($el.text()).to.equal('Testi-perustutkinto')
        })

      cy.get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .type('Perustutkinto')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .eq(0)
        .should($el => {
          expect($el.text()).to.equal('Testi-perustutkinto')
        })
    })
  })
  describe('Hakukohderyhmän hallinnointi', () => {
    it('Näyttää hakukohderyhmän lisäysnäkymän', () => {
      cy.get(hl.hakukohderyhmanLisaysHeadingSelector).should(
        'have.text',
        'Hakukohderyhmät',
      )
      cy.get(hl.hakukohderyhmanLisaysDropdownSelectorUndropped).should(
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
      cy.get(hl.hakukohderyhmanLisaysDropdownSelectorDropped).should(
        'not.exist',
      )
    })
    it('Hakukohderyhmä-dropdownissa näkyy valmiiksi olemassa olevat ryhmät', () => {
      cy.fixture(
        'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
      ).then(ryhmat => {
        const preExistingRyhmaNimi = ryhmat[0].nimi.fi
        cy.get(hl.hakukohderyhmanLisaysDropdownSelectorUndropped).click({
          force: true,
        })
        cy.get(hl.hakukohderyhmanLisaysDropdownSelectorDropped).should('exist')
          cy.get(hl.hakukohderyhmanLisaysDropdownSelectorItem(preExistingRyhmaNimi)) // eslint-disable-line prettier/prettier
          .should('exist')
      })
    })
    it('Hakukohderyhmän voi valita', () => {
      cy.fixture(
        'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
      ).then(ryhmat => {
        const preExistingRyhmaNimi = ryhmat[0].nimi.fi
            cy.get(hl.hakukohderyhmanLisaysDropdownSelectorItem(preExistingRyhmaNimi)) // eslint-disable-line prettier/prettier
          .click({ force: true })
        cy.get(hl.hakukohderyhmanLisaysDropdownSelectorUndropped).should(
          'have.text',
          preExistingRyhmaNimi,
        )
      })
    })
    it('Muokkauslinkki tulee näkyviin, kun ryhmä on valittu', () => {
      cy.get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector).should(
        'have.text',
        'Muokkaa ryhmää',
      )
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
        ).should('have.attr', 'placeholder', 'Uuden ryhmän nimi')
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
          it('Ryhmän voi tallentaa, tallennus-input katoaa näkymästä ja uusi ryhmä on valittu automaattisesti', () => {
            cy.get(
              hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
            ).click({ force: true })
            cy.get(
              hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
            ).should('not.exist')
            cy.get<PostHakukohderyhmaRequestFixture>(
              '@post-hakukohderyhma-request',
            ).then(hakukohderyhma => {
              cy.get(
                hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
              ).should('not.exist')
              cy.get(
                hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
              ).should('not.exist')
              cy.get(hl.hakukohderyhmanLisaysDropdownSelectorUndropped).should(
                'have.text',
                hakukohderyhma.nimi.fi,
              )
            })
          })
        })
      })
    })
    describe('Hakukohderyhmän nimen muuttaminen', () => {
      it('Näyttää hakukohderyhmän muokkauksen tekstikentän ja muokattavan ryhmän nimen placeholderissa', () => {
        cy.get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector).click({
          force: true,
        })
        cy.get(hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector)
          .should('have.attr', 'placeholder', 'Testihakukohderyhmä')
          .should('have.text', '')
        cy.get(
          hl.hakukohderyhmanLisaysSaveRenameHakukohderyhmaButtonSelector,
        ).should('be.disabled')
      })
      it('Muokkauslinkki on pois näkymästä muokkauksen aikana', () => {
        cy.get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector).should(
          'not.exist',
        )
      })
      it('Uuden nimen kirjoittaminen', () => {
        cy.fixture<PutHakukohderyhmaRequestFixture>(
          'hakukohderyhmapalvelu/put-hakukohderyhma-request.json',
        )
          .as('put-hakukohderyhma-request')
          .then(renamedHakukohderyhma => {
            cy.get(
              hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector,
            ).type(renamedHakukohderyhma.nimi.fi, { force: true })
          })
      })
      it('Uuden nimen tallennuspainiketta voi klikata', () => {
        cy.get(
          hl.hakukohderyhmanLisaysSaveRenameHakukohderyhmaButtonSelector,
        ).should('be.not.disabled')
      })
      describe('Uuden nimen tallentaminen', () => {
        before(() => {
          cy.resetMocks()
          cy.login()
          cy.fixture(
            'hakukohderyhmapalvelu/put-hakukohderyhma-request.json',
          ).as('put-hakukohderyhma-request')
          cy.fixture(
            'hakukohderyhmapalvelu/put-hakukohderyhma-response.json',
          ).as('put-hakukohderyhma-response')
          cy.mockBackendRequest({
            method: 'PUT',
            path:
              '/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.47149607930',
            service: 'organisaatio-service',
            requestFixture:
              'hakukohderyhmapalvelu/put-hakukohderyhma-request.json',
            responseFixture:
              'hakukohderyhmapalvelu/put-hakukohderyhma-response.json',
          })
            cy.login()
            cy.mockBackendRequest({
                method: 'GET',
                path: '/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.47149607930',
                service: 'organisaatio-service',
                responseFixture:
                    'hakukohderyhmapalvelu/get-hakukohderyhma-to-rename-response.json',
            })
          cy.server()
          cy.route(
            'POST',
            '/hakukohderyhmapalvelu/api/hakukohderyhma/1.2.246.562.28.47149607930/rename',
          ).as('post-hakukohderyhma-rename')
        })
        it('Uuden nimen voi tallentaa, tallennus-input katoaa näkymästä ja uusi ryhmä on valittu automaattisesti', () => {
          cy.get(
            hl.hakukohderyhmanLisaysSaveRenameHakukohderyhmaButtonSelector,
          ).click({ force: true })
          cy.get<PutHakukohderyhmaRequestFixture>(
            '@put-hakukohderyhma-request',
          ).then(renamedHakukohderyhma => {
            cy.get(
              hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector,
            ).should('not.exist')
            cy.get(
              hl.hakukohderyhmanLisaysSaveRenameHakukohderyhmaButtonSelector,
            ).should('not.exist')
            cy.get(hl.hakukohderyhmanLisaysDropdownSelectorUndropped).should(
              'have.text',
              renamedHakukohderyhma.nimi.fi,
            )
          })
        })
      })
    })
  })
})
