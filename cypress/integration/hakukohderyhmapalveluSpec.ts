/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

import * as hs from '../selectors/hakukohderyhmaPanelSelectors'
import * as hh from '../selectors/hakukohderyhmanHakutoimintoSelectors'
import * as hl from '../selectors/hakukohderyhmanLisaysSelectors'
import { PostHakukohderyhmaRequestFixture } from '../fixtures/hakukohderyhmapalvelu/PostHakukohderyhmaRequestFixture'
import { PutHakukohderyhmaRequestFixture } from '../fixtures/hakukohderyhmapalvelu/PutHakukohderyhmaRequestFixture'
import { hakukohderyhmanValintaDropdown } from '../selectors/hakukohderyhmanLisaysSelectors'

describe('Hakukohderyhmäpalvelu', () => {
  const hideReframeDebuggerWindow = () => {
    cy.visit('/')
    cy.get('body').type('{ctrl}h')
  }
  const tarjoajaParameter = 'tarjoaja=1.2.246.562.10.0439845%2C1.2.246.562.28.1'

  before(() => {
    cy.login()
    cy.mockBackendRequest({
      method: 'GET',
      path:
        '/organisaatio-service/rest/organisaatio/v3/ryhmat?ryhmatyyppi=ryhmatyypit_6%231',
      service: 'organisaatio-service',
      responseFixture:
        'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
    })
    cy.login()
    cy.mockBackendRequest({
      method: 'GET',
      path: `/kouta-internal/haku/search?${tarjoajaParameter}`,
      service: 'kouta-service',
      responseFixture: 'hakukohderyhmapalvelu/get-haku-response.json',
    })
    cy.login()
    cy.mockBackendRequest({
      method: 'GET',
      path: `/kouta-internal/hakukohde/search?haku=1.2.3.4.5.3&${tarjoajaParameter}&all=true`,
      service: 'kouta-service',
      responseFixture: 'hakukohderyhmapalvelu/get-hakukohde-response.json',
    })

    cy.login()
    cy.mockBackendRequest({
      method: 'POST',
      path: `/kouta-internal/hakukohde/findbyoids?${tarjoajaParameter}`,
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
    cy.mockBackendRequest({
      method: 'GET',
      path: '/organisaatio-service/rest/organisaatio/v4/1.2.2.5.2.9',
      service: 'organisaatio-service',
      responseFixture: 'hakukohderyhmapalvelu/get-hakukohderyhma-response.json',
    })
    cy.login()
    cy.mockBackendRequest({
      method: 'POST',
      path: `/kouta-internal/hakukohde/findbyoids?${tarjoajaParameter}`,
      service: 'kouta-service',
      requestFixture:
        'hakukohderyhmapalvelu/post-find-hakukohteet-by-oids.json',
      responseFixture: 'hakukohderyhmapalvelu/get-hakukohde-response.json',
    })
    cy.login()
    cy.mockBackendRequest({
      method: 'POST',
      path: `/kouta-internal/hakukohde/findbyoids?${tarjoajaParameter}`,
      service: 'kouta-service',
      requestFixture:
        'hakukohderyhmapalvelu/post-find-hakukohteet-by-oids-reversed.json',
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

    hideReframeDebuggerWindow()
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

    it('Näyttää hakukohteet, joihin käyttäjällä on oikeus', () => {
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
          expect($el.text()).to.equal('Testi-jatkotutkinto')
        })

      cy.get(hh.hakukohteetContainerSelector).contains('Testi-perustutkinto')

      cy.get(hh.hakukohteetContainerSelector)
        .contains('Testi-ei-oikeuksia')
        .should('not.exist')

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
  describe('Hakukohteen lisääminen ja poistaminen hakukohderyhmäään', () => {
    it('Lisää hakukohde hakukohderyhmään', () => {
      cy.fixture('hakukohderyhmapalvelu/get-hakukohde-response.json').then(
        hakukohteet => {
          cy.get(hl.hakukohderyhmanValintaDropdown)
            .type('Suklaaryhmä{enter}')
            .get(hh.hakukohteidenSuodatusInputSelector)
            .clear()
            .type('tutkinto')
            .get(hh.hakukohteetContainerSelector)
            .contains(hakukohteet[0].nimi.fi)
            .click({ force: true })
            .get(hh.hakukohteetContainerSelector)
            .contains(hakukohteet[1].nimi.fi)
            .click({ force: true })
            .login()
            .get(hh.hakukohteetLisaysButtonSelector)
            .click({ force: true })
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .children()
            .eq(1)
            .should('have.text', hakukohteet[0].nimi.fi)
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .children()
            .eq(0)
            .should('have.text', hakukohteet[1].nimi.fi)
        },
      )
    })
    it('Valitaan hakukohderyhmä, jolla ei ole hakukohteita', () => {
      cy.get(hl.hakukohderyhmanValintaDropdown)
        .type('Kinuskiryhmä{enter}')
        .get(hh.hakukohderyhmanHakukohteetContainerSelector)
        .children()
        .should('have.length', 0)
    })
    it('Poistetaan hakukohteet hakukohderyhmältä', () => {
      cy.login()
      cy.get(hl.hakukohderyhmanValintaDropdown)
        .type('Suklaaryhmä{enter}')
        .get(hh.hakukohderyhmanHakukohteetContainerSelector)
        .children()
        .each(el => {
          cy.wrap(el).click({ force: true })
        })
        .get(hh.poistaRyhmastaButtonSelector)
        .click({ force: true })
        .get(hh.hakukohteetContainerSelector)
        .children()
        .should('have.length', 2)
        .get(hh.hakukohderyhmanHakukohteetContainerSelector)
        .children()
        .should('have.length', 0)
    })

    it('Kaikki hakukohteet voi poistaa valinnasta kerralla, jonka jälkeen nappi disabloituu', () => {
      cy.get(hh.hakukohdeDeselectAllSelector)
        .click({ force: true })
        .should('be.disabled')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', false),
        )
        .should('exist')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', false),
        )
        .should('exist')
    })
  })

  describe('Kaikkien hakukohteiden valitseminen kerralla', () => {
    it('Kaikki hakukohteet voi valita yhdellä kertaa', () => {
      cy.get(hh.hakukohdeSelectAllSelector)
        .click({ force: true })
        .should('be.disabled')
        .get(hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', true))
        .should('exist')
        .get(hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', true))
        .should('exist')
        .get(hh.hakukohdeDeselectAllSelector)
        .should('be.not.disabled')
        .click({ force: true })
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', false),
        )
        .should('exist')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', false),
        )
        .should('exist')
    })

    it('Kaikkien valitseminen kohdistuu vain suodatettuihin hakukohteisiin', () => {
      cy.get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .type('perustutkinto')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .should('have.length', 1)
        .get(hh.hakukohdeSelectAllSelector)
        .click({ force: true })
        .should('be.disabled')
        .get(hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', true))
        .should('exist')
        .get(hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', true))
        .should('not.exist')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', false),
        )
        .should('not.exist')
        .get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .get(hh.hakukohdeSelectAllSelector)
        .should('be.not.disabled')
        .get(hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', true))
        .should('exist')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', false),
        )
        .should('exist')
    })

    it('Kaikkien valinnan poistaminen kohdistuu myös suodatuksen ulkopuolelle', () => {
      cy.get(hh.hakukohdeSelectAllSelector)
        .click({ force: true })
        .get(hh.hakukohteidenSuodatusInputSelector)
        .type('jatkotutkinto')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .should('have.length', 1)
        .get(hh.hakukohdeDeselectAllSelector)
        .should('be.not.disabled')
        .click({ force: true })
        .should('be.disabled')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', false),
        )
        .should('exist')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', false),
        )
        .should('not.exist')
        .get(hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', true))
        .should('not.exist')
        .get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .get(hh.hakukohdeDeselectAllSelector)
        .should('be.disabled')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-perustutkinto', false),
        )
        .should('exist')
        .get(
          hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', false),
        )
        .should('exist')
    })
  })

  describe('Hakukohderyhmän hallinnointi', () => {
    it('Näyttää hakukohderyhmän lisäysnäkymän', () => {
      cy.get(hl.hakukohderyhmanLisaysHeadingSelector).should(
        'have.text',
        'Hakukohderyhmät',
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
    it('Hakukohderyhmän voi valita', () => {
      cy.fixture(
        'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
      ).then(ryhmat => {
        const preExistingRyhmaNimi = ryhmat[0].nimi.fi
        cy.get(hakukohderyhmanValintaDropdown).type(
          `${preExistingRyhmaNimi}{enter}`,
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
              cy.get(hl.hakukohderyhmanValintaDropdown).should(
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
            path:
              '/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.47149607930',
            service: 'organisaatio-service',
            responseFixture:
              'hakukohderyhmapalvelu/get-hakukohderyhma-to-rename-response.json',
          })
          cy.server()
          cy.route(
            'POST',
            '/hakukohderyhmapalvelu/api/hakukohderyhma/1.2.246.562.28.47149607930/rename',
          )
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
            cy.get(hl.hakukohderyhmanValintaDropdown).should(
              'have.text',
              renamedHakukohderyhma.nimi.fi,
            )
          })
        })
      })
      describe('Hakukohderyhmän poisto', () => {
        before(() => {
          cy.resetMocks()
          cy.login()
          cy.mockBackendRequest({
            method: 'DELETE',
            path: '/organisaatio-service/rest/organisaatio/v4/1.2.2.5.2.9',
            service: 'organisaatio-service',
            responseFixture:
              'hakukohderyhmapalvelu/delete-organisaatio-response.json',
          })
          cy.server()
          cy.route(
            'DELETE',
            '/hakukohderyhmapalvelu/api/hakukohderyhma/1.2.2.5.2.9',
          )
        })
        it('Poistonapin painamisen jälkeen tehdään varmistus, jossa käyttäjä voi vielä peruuttaa poiston', () => {
          cy.get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector).click({
            force: true,
          })
          cy.get(hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector)
            .should(
              'have.attr',
              'placeholder',
              'Uudelleennimetty testihakukohderyhmä',
            )
            .should('have.text', '')
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).click({
            force: true,
          })
          cy.get(hl.hakukohderyhmanPoistoCancelDeleteButtton).click({
            force: true,
          })
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).should('exist')
        })
        it('Jos käyttäjä vaihtaa ryhmää kesken varmistuksen, varmistusdialogi katoaa', () => {
          cy.get(hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector)
            .should(
              'have.attr',
              'placeholder',
              'Uudelleennimetty testihakukohderyhmä',
            )
            .should('have.text', '')
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).click({
            force: true,
          })

          cy.get(hl.hakukohderyhmanPoistoCancelDeleteButtton).should('exist')
          cy.fixture(
            'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
          ).then(ryhmat => {
            const preExistingRyhmaNimi = ryhmat[1].nimi.fi
            cy.get(hakukohderyhmanValintaDropdown).type(
              `${preExistingRyhmaNimi}{enter}`,
            )
          })
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).should('exist')
          cy.get(hl.hakukohderyhmanPoistoCancelDeleteButtton).should(
            'not.exist',
          )
        })
        it('Hakukohderyhmän voi poistaa', () => {
          cy.fixture(
            'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
          ).then(ryhmat => {
            const preExistingRyhmaNimi = ryhmat[1].nimi.fi
            const searchStr = preExistingRyhmaNimi.substr(0, 4)
            cy.get(hakukohderyhmanValintaDropdown)
              .type(searchStr)
              .contains(preExistingRyhmaNimi)

            cy.get(hl.hakukohderyhmanPoistoDeleteButtton).click({
              force: true,
            })
            cy.login()
            cy.get(hl.hakukohderyhmanPoistoConfirmDeleteButtton).click({
              force: true,
            })

            cy.get(hakukohderyhmanValintaDropdown)
              .type(searchStr)
              .contains('Valittavia kohteita ei löytynyt')
          })
        })
      })
      describe('Hakukohderyhmän epäonnistunut poisto', () => {
        before(() => {
          //TODO when ataru check has been implemented
          //mock routes to return `in-use` status
        })
        it('Jos poistettava ryhmä on käytössä, käyttäjälle näytetään alert banner', () => {
          //valitse ryhmä
          //muokkaa ryhmää
          //paina roskakori-nappia
          //paina vahvista poisto- nappia
          //assert, että roskakori on näkyvissä
          //assert, että alert banner näkyy ja siinä oikea teksti
        })
        it('Käyttäjä voi sulkea alert bannerin', () => {
          //paina alert bannerin sulkuruksia
          //assert, että alert banner katoaa
        })
      })
    })
  })
})
