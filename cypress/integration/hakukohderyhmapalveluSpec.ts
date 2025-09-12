/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

import * as hs from '../selectors/hakukohderyhmaPanelSelectors'
import * as hh from '../selectors/hakukohderyhmanHakutoimintoSelectors'
import * as hl from '../selectors/hakukohderyhmanLisaysSelectors'
import * as ha from '../selectors/hakukohderyhmanAsetustenSelectors'

import { PostHakukohderyhmaRequestFixture } from '../fixtures/hakukohderyhmapalvelu/PostHakukohderyhmaRequestFixture'
import { PutHakukohderyhmaRequestFixture } from '../fixtures/hakukohderyhmapalvelu/PutHakukohderyhmaRequestFixture'
import { hakukohderyhmanValintaDropdown } from '../selectors/hakukohderyhmanLisaysSelectors'

describe('Hakukohderyhmäpalvelu', () => {
  const mockLocalizationRoute = (locale: string) => {
    cy.mockBrowserRequest({
      method: 'GET',
      path: `http://localhost/lokalisointi/cxf/rest/v1/localisation?category=hakukohderyhmapalvelu&locale=${locale}`,
      fixturePath: `hakukohderyhmapalvelu/get-translations-${locale}.json`,
      responseAlias: `hakukohderyhmapalvelu-get-${locale}-translations-response`,
    })
  }
  const addUnauthorizedMockHakukohdeToHakukohderyhma = () => {
    cy.task('query', {
      sql: `
                INSERT INTO hakukohderyhma (hakukohde_oid, hakukohderyhma_oid)
                VALUES ($1, $2)
            `,
      values: ['1.2.4.2.1.4', '1.1.2.5.2.9'],
    })
  }
  const tarjoajaParameter = 'tarjoaja=1.2.246.562.10.0439845%2C1.2.246.562.28.1'

  before(() => {
    addUnauthorizedMockHakukohdeToHakukohderyhma()
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
      path: `/kouta-internal/hakukohde/findbyoids?${tarjoajaParameter}`,
      service: 'kouta-service',
      requestFixture:
        'hakukohderyhmapalvelu/post-find-hakukohteet-by-oids-unreversed.json',
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

    cy.mockBrowserRequest({
      method: 'GET',
      path:
        'http://localhost/koodisto-service/rest/json/searchKoodis?koodiUris=koulutustyyppi_26&koodiUris=koulutustyyppi_2&koodiUris=koulutustyyppi_4&koodiUris=koulutustyyppi_5&koodiUris=koulutustyyppi_10&koodiUris=koulutustyyppi_40&koodiUris=koulutustyyppi_41&koodiTilas=HYVAKSYTTY&koodiTilas=LUONNOS&koodiVersioSelection=LATEST',
      fixturePath: 'hakukohderyhmapalvelu/get-koulutustyypit-response.json',
      responseAlias: 'hakukohderyhmapalvelu-get-koulutustyypit-response',
    })

    mockLocalizationRoute('fi')
    mockLocalizationRoute('sv')
    mockLocalizationRoute('en')

    cy.visit('/hakukohderyhmapalvelu/')
    cy.get('div[cypressid=haku-search-cypress] input').type('{ctrl}h', {
      force: true,
    })
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
  describe('Lokalisointi', () => {
    it('Sovellus käyttää lokalisointipalvelusta haettuja tekstejä', () => {
      cy.fixture('hakukohderyhmapalvelu/get-translations-fi.json').then(
        translations => {
          cy.get(hh.haunHakutoimintoTitleSelector).should(
            'have.text',
            translations[0].value,
          )
        },
      )
    })
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

      cy.fixture('hakukohderyhmapalvelu/get-hakukohde-response.json').then(
        hakukohteet => {
          cy.get(hh.haunHakutoimintoDivSelectorChildDivs)
            .eq(1)
            .type('Testihaku 3{enter}')
            .get(hh.hakukohteetContainerSelector)
            .children()
            .should(
              'have.length',
              hakukohteet.filter((h: any) => h.oikeusHakukohteeseen).length,
            )
        },
      )

      cy.get(hh.hakukohteetContainerSelector)
        .children()
        .eq(0)
        .find('span')
        .last()
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
        .find('span')
        .last()
        .should($el => {
          expect($el.text()).to.equal('Testi-perustutkinto')
        })

      cy.get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .type('Perustutkinto')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .eq(0)
        .find('span')
        .last()
        .should($el => {
          expect($el.text()).to.equal('Testi-perustutkinto')
        })
    })

    describe('Suodattaa hakukohteita lisärajaimilla', () => {
      it('Suodattaa kaksoistutkinnolla', () => {
        cy.get(hh.hakukohteidenSuodatusInputSelector).clear()
        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 8)

        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterBooleanSelector('kaksoistutkinto-filter')).should(
          'have.attr',
          'aria-checked',
          'false',
        )
        cy.get(hh.extraFilterBooleanSelector('kaksoistutkinto-filter')).click({
          force: true,
        })
        cy.get(hh.extraFilterBooleanSelector('kaksoistutkinto-filter')).should(
          'have.attr',
          'aria-checked',
          'true',
        )
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 2)

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .eq(0)
          .find('span')
          .last()
          .should($el => {
            expect($el.text()).to.equal('Testi-perustutkinto')
          })
      })

      it('Suodattaa SORA-tiedolla', () => {
        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterBooleanSelector('kaksoistutkinto-filter')).click({
          force: true,
        })
        cy.get(hh.extraFilterBooleanSelector('sora-filter')).click({
          force: true,
        })
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 5)

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .eq(0)
          .find('span')
          .last()
          .should($el => {
            expect($el.text()).to.equal('Testi-jatkotutkinto')
          })

        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterBooleanSelector('sora-filter')).click({
          force: true,
        })
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 8)
      })

      it('Suodattaa harkinnanvaraisuudella', () => {
        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterBooleanSelector('harkinnanvaraiset-filter')).click(
          {
            force: true,
          },
        )
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 2)

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .eq(0)
          .find('span')
          .last()
          .should($el => {
            expect($el.text()).to.equal('TestiZ Ammatillinen harkinnanvarainen')
          })

        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterBooleanSelector('harkinnanvaraiset-filter')).click(
          {
            force: true,
          },
        )
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 8)
      })

      it('Suodattaa koulutustyypillä', () => {
        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterSelectSelector('koulutustyypit-filter')).type(
          'Vapaan sivistystyön koulutus{enter}',
        )
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 2)

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .eq(0)
          .find('span')
          .last()
          .should($el => {
            expect($el.text()).to.equal('Testi-perustutkinto')
          })
        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .eq(1)
          .find('span')
          .last()
          .should($el => {
            expect($el.text()).to.equal('archivearkistoitukohde')
          })

        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterSelectSelector('koulutustyypit-filter'))
          .find('svg')
          .eq(0)
          .click({ force: true })

        cy.get(hh.extraFiltersPopupClose).click({ force: true })
        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 8)
      })

      it('Suodattaa urheilijakoulutuksella', () => {
        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterBooleanSelector('urheilu-filter')).click({
          force: true,
        })
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 2)

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .eq(0)
          .find('span')
          .last()
          .should($el => {
            expect($el.text()).to.equal('TestiZ Ammatillinen harkinnanvarainen')
          })
        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .eq(1)
          .find('span')
          .last()
          .should($el => {
            expect($el.text()).to.equal('xtrakohde-4')
          })

        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterBooleanSelector('urheilu-filter')).click({
          force: true,
        })
        cy.get(hh.extraFiltersPopupClose).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 8)
      })

      it('Suodattaa kaikki hakukohteet pois', () => {
        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterSelectSelector('koulutustyypit-filter')).type(
          'Lukiokoulutus{enter}',
        )
        cy.get(hh.extraFiltersButtonSelector).click({ force: true })

        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 0)

        cy.get(hh.extraFiltersButtonSelector).click({ force: true })
        cy.get(hh.extraFilterSelectSelector('koulutustyypit-filter'))
          .find('svg')
          .eq(0)
          .click({ force: true })

        cy.get(hh.extraFiltersPopupClose).click({ force: true })
        cy.get(hh.hakukohteetContainerSelector)
          .children()
          .should('have.length', 8)
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
            .find('span')
            .last()
            .should('have.text', hakukohteet[0].nimi.fi)
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .children()
            .eq(0)
            .find('span')
            .last()
            .should('have.text', hakukohteet[1].nimi.fi)
        },
      )
    })
    it('Valitaan hakukohderyhmä, jolla on ainoastaan oikeudettomia hakukohteita', () => {
      cy.fixture('hakukohderyhmapalvelu/get-hakukohde-response.json').then(
        hakukohteet => {
          cy.get(hl.hakukohderyhmanValintaDropdown)
            .type('Kinuskiryhmä{enter}')
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .children()
            .should('have.length', 1)
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .contains(hakukohteet[1].nimi.fi)
            .should('not.exist')
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .contains(hakukohteet[3].nimi.fi)
            .should('exist')
        },
      )
    })
    it('Oikeudettomia hakukohteita ei voi poistaa ryhmästä', () => {
      cy.fixture('hakukohderyhmapalvelu/get-hakukohde-response.json').then(
        hakukohteet => {
          cy.get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .children()
            .each(el => {
              cy.wrap(el).click({ force: true })
            })
            .get(hh.poistaRyhmastaButtonSelector)
            .click({ force: true })
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .children()
            .should('have.length', 1)
            .get(hh.hakukohderyhmanHakukohteetContainerSelector)
            .contains(hakukohteet[3].nimi.fi)
            .should('exist')
        },
      )
    })
    it('Poistetaan hakukohteet hakukohderyhmältä', () => {
      cy.login()
      cy.get(hl.hakukohderyhmanValintaDropdown)
        .type('Suklaaryhmä{enter}')
        .get(hh.hakukohderyhmanHakukohteetContainerSelector)
        .children()
        .each(el => {
          cy.wrap(el.children().get(1)).click({ force: true })
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

  describe('Näyttää hakukohteen olevan arkistoitu', () => {
    it('Arkistoidulla hakukohteella on ikoni', () => {
      cy.get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
        .type('arkisto')
        .get(hh.hakukohteetContainerSelector)
        .contains('archivearkistoitukohde')
        .find('i')
        .should('exist')
        .get(hh.hakukohteidenSuodatusInputSelector)
        .clear()
    })

    it('Ryhmään lisätyllä arkistoidulla hakukohteella on myös ikoni', () => {
      cy.get(hl.hakukohderyhmanValintaDropdown)
        .type('Suklaaryhmä{enter}')
        .get(hh.hakukohteetContainerSelector)
        .contains('archivearkistoitukohde')
        .click({ force: true })
        .get(hh.hakukohteetLisaysButtonSelector)
        .click({ force: true })
        .get(hh.hakukohderyhmanHakukohteetContainerSelector)
        .children()
        .contains('archivearkistoitukohde')
        .find('i')
        .should('exist')
        .click({ force: true })
        .get(hh.poistaRyhmastaButtonSelector)
        .click({ force: true })
        .get(hh.hakukohderyhmanHakukohteetContainerSelector)
        .children()
        .should('have.length', 0)
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

    it('Suodatuksen asettaminen poistaa valinnan hakukohteilta jotka ovat suodatuksen ulkopuolella', () => {
      cy.get(hh.hakukohdeSelectAllSelector)
        .click({ force: true })
        .get(hh.hakukohteidenSuodatusInputSelector)
        .type('jatkotutkinto')
        .get(hh.hakukohteetContainerSelector)
        .children()
        .should('have.length', 1)
        .get(hh.hakukohteetContainerOptionSelector('Testi-jatkotutkinto', true))
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
  })

  describe('Kaikkien ryhmässä olevien hakukohteiden valitseminen kerralla', () => {
    it('Liitä kaikki kohteet ryhmään', () => {
      cy.get(hl.hakukohderyhmanValintaDropdown).type('Suklaaryhmä{enter}')
      cy.get(hh.hakukohdeSelectAllSelector)
        .click({ force: true })
        .login()
        .get(hh.hakukohteetLisaysButtonSelector)
        .click({ force: true })
    })
    it('Kohteet eivät ole automaattisesti valittuja kun ne lisätään ryhmään', () => {
      cy.get(
        hh.groupedHakukohteetContainerOptionSelector(
          'Testi-jatkotutkinto',
          false,
        ),
      )
        .should('exist')
        .get(
          hh.groupedHakukohteetContainerOptionSelector(
            'Testi-perustutkinto',
            false,
          ),
        )
        .should('exist')
      for (let i = 1; i < 5; i += 1) {
        cy.get(
          hh.groupedHakukohteetContainerOptionSelector(`xtrakohde-${i}`, false),
        ).should('exist')
      }
    })
    it('Kaikki ryhmän kohteet voi valita kerralla', () => {
      cy.get(hh.groupedHakukohdeSelectAllSelector)
        .should('be.not.disabled')
        .click({ force: true })
        .should('be.disabled')
      for (let i = 1; i < 5; i += 1) {
        cy.get(
          hh.groupedHakukohteetContainerOptionSelector(`xtrakohde-${i}`, true),
        ).should('exist')
      }
      cy.get(hh.groupedHakukohdeDeselectAllSelector)
        .should('be.not.disabled')
        .click({ force: true })
        .should('be.disabled')
      for (let i = 1; i < 5; i += 1) {
        cy.get(
          hh.groupedHakukohteetContainerOptionSelector(`xtrakohde-${i}`, false),
        ).should('exist')
      }
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
        ).should('have.attr', 'placeholder', 'Hakukohderyhmän nimi')
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
    describe('Hakukohderyhmän asetusten muutto', () => {
      it('Max hakukohteet eivät aluksi ole näkyvissä', () => {
        cy.get(ha.maxHakukohteetSelector).should('not.exist')
      })
      it('Max hakukohteet tulee näkyviin kun asettaa rajaavan', () => {
        cy.login()
        cy.get(ha.rajaavaSelector).click({ force: true })
        cy.get(ha.maxHakukohteetSelector).should('exist')
      })
    })
    describe('Hakukohderyhmän nimen muuttaminen', () => {
      it('Näyttää hakukohderyhmän muokkauksen tekstikentän ja muokattavan ryhmän nimen', () => {
        cy.get(hakukohderyhmanValintaDropdown).type(
          'Testihakukohderyhmä{enter}',
        )
        cy.get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector).click({
          force: true,
        })
        cy.get(
          hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector,
        ).should('have.attr', 'placeholder', 'Hakukohderyhmän nimi')
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
            )
              .clear()
              .type(renamedHakukohderyhma.nimi.fi, { force: true })
          })
      })
      it('Uuden nimen tallennuspainiketta voi klikata', () => {
        cy.get(
          hl.hakukohderyhmanLisaysSaveRenameHakukohderyhmaButtonSelector,
        ).should('be.not.disabled')
      })
      describe('Uuden nimen tallentaminen', () => {
        before(() => {
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
          cy.wait(500) // eslint-disable-line
            // Waits for debounce to settle
            .get(hl.hakukohderyhmanLisaysSaveRenameHakukohderyhmaButtonSelector)
            .click({ force: true })
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
            .should('have.value', 'Uudelleennimetty testihakukohderyhmä')
            .should('have.text', '')
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).click({
            force: true,
          })
          cy.get(hl.hakukohderyhmanPoistoCancelDeleteButtton).click({
            force: true,
          })
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).should('exist')
        })
        it('Jos käyttäjä vaihtaa ryhmää kesken varmistuksen, inputit katoaa', () => {
          cy.get(hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector)
            .should('have.value', 'Uudelleennimetty testihakukohderyhmä')
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
          cy.get(
            hl.hakukohderyhmanLisaysRenameHakukohderyhmaTextInputSelector,
          ).should('not.exist')
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).should('not.exist')
          cy.get(hl.hakukohderyhmanPoistoCancelDeleteButtton).should(
            'not.exist',
          )
        })
        it('Hakukohderyhmän voi poistaa', () => {
          cy.mockBackendRequest({
            method: 'GET',
            path: '/lomake-editori/api/forms?hakukohderyhma-oid=1.2.2.5.2.9',
            service: 'ataru-service',
            responseFixture:
              'hakukohderyhmapalvelu/ataru-empty-forms-response.json',
          })
          cy.fixture(
            'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
          ).then(ryhmat => {
            const preExistingRyhmaNimi = ryhmat[1].nimi.fi
            const searchStr = preExistingRyhmaNimi.substr(0, 4)
            cy.get(hakukohderyhmanValintaDropdown).type(
              `${preExistingRyhmaNimi}{enter}`,
            )
            cy.get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector).click({
              force: true,
            })
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
      describe('Hakukohderyhmässä oikeudettomia hakukohteita', () => {
        it('Hakukohderyhmää ei voi poistaa, jos siinä on oikeudettomia hakukohteita', () => {
          cy.fixture(
            'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
          ).then(ryhmat => {
            const preExistingRyhmaNimi = ryhmat[0].nimi.fi
            const searchStr = preExistingRyhmaNimi.substr(0, 4)
            cy.get(hakukohderyhmanValintaDropdown)
              .type(`${searchStr}{enter}`)
              .get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector)
              .click({
                force: true,
              })
              .get(hl.hakukohderyhmanPoistoDeleteButtton)
              .should('not.exist')
              .get(
                hl.hakukohderyhmanLisaysSaveRenameHakukohderyhmaButtonSelector,
              )
              .should('exist')
          })
        })
      })
      describe('Hakukohderyhmän epäonnistunut poisto', () => {
        it('Jos poistettava ryhmä on käytössä, käyttäjälle näytetään alert banner', () => {
          cy.mockBackendRequest({
            method: 'GET',
            path:
              '/lomake-editori/api/forms?hakukohderyhma-oid=1.2.246.562.28.47149607930',
            service: 'ataru-service',
            responseFixture:
              'hakukohderyhmapalvelu/ataru-non-empty-forms-response.json',
          })

          cy.get(hakukohderyhmanValintaDropdown).type(
            'Uudelleennimetty testihakukohderyhmä{enter}',
          )
          cy.get(hl.hakukohderyhmanLisaysMuokkaaRyhmaaLinkSelector).click({
            force: true,
          })
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).click({
            force: true,
          })
          cy.login()
          cy.get(hl.hakukohderyhmanPoistoConfirmDeleteButtton).click({
            force: true,
          })

          cy.get(hl.alertSelector).should('exist')
          cy.get(hl.alertSelector).contains(
            'Hakukohderyhmä on käytössä hakulomakkeella ja sitä ei voi poistaa.',
          )
          cy.get(hl.hakukohderyhmanPoistoDeleteButtton).should('exist')
        })
        it('Käyttäjä voi sulkea alert bannerin', () => {
          cy.get(hl.alertSelector).should('exist')
          cy.get(hl.alertCloseSelector).click({ force: true })
          cy.get(hl.alertSelector).should('not.exist')
        })
      })
    })
  })
  describe('Epäonnistunut HTTP-pyyntö näyttää virheen', () => {
    it('Näytetään virhe, jos HTTP-pyyntö epäonnistuu', () => {
      cy.login()
      cy.get(hl.alertSelector).should('not.exist')
      cy.get(hl.hakukohderyhmanLisaysLisaaUusiRyhmaLinkSelector).click({
        force: true,
      })
      cy.get(
        hl.hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector,
      ).type('Ryhmän nimi jota ei ole mockattu')

      cy.wait(500) // eslint-disable-line
      // Waits for debounce to settle

      cy.get(
        hl.hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector,
      ).click({ force: true })

      cy.get(hl.alertSelector).should('exist')
    })
  })

  after('Clean up db', () => {
    cy.task('query', {
      sql: 'TRUNCATE hakukohderyhma',
    })

    cy.task('query', {
      sql: 'TRUNCATE hakukohderyhma_settings',
    })
  })
})
