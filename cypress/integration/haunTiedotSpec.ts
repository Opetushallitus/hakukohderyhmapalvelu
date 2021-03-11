/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

describe('Hakukohderyhmäpalvelu - haun tiedot', () => {
  before(() => {
    cy.resetMocks()
    cy.login()
  })
  describe('Haun tietojen hakeminen', () => {
    beforeEach(() => {
      cy.fixture('hakukohderyhmapalvelu/get-haku-response.json').as(
        'get-haku-response',
      )
      cy.login()
      cy.mockBackendRequest({
        method: 'GET',
        path: '/kouta-internal/haku/search?tarjoaja=1.2.246.562.10.00000000001',
        service: 'kouta-service',
        responseFixture: 'hakukohderyhmapalvelu/get-haku-response.json',
      })
    })
    it('Hakee listauksen hauista, myös päättyneet haut', () => {
      cy.request('/hakukohderyhmapalvelu/api/haku?all=true').then(({ body }) =>
        expect(body).to.deep.equal([
          {
            oid: '1.2.3.4.5.1',
            nimi: {
              fi: 'Testihaku',
              sv: 'Testansökan',
              en: 'Test application',
            },
          },
          { oid: '1.2.3.4.5.2', nimi: { fi: 'Testihaku vain suomeksi' } },
          { oid: '1.2.3.4.5.3', nimi: { fi: 'Testihaku 3' } },
        ]),
      )
    })
    it('Hakee listauksen hauista, vain voimassaolevat haut. Testaa, että all=false oletuksena', () => {
      cy.request('/hakukohderyhmapalvelu/api/haku').then(({ body }) =>
        expect(body).to.deep.equal([
          { oid: '1.2.3.4.5.2', nimi: { fi: 'Testihaku vain suomeksi' } },
          { oid: '1.2.3.4.5.3', nimi: { fi: 'Testihaku 3' } },
        ]),
      )
    })
    it('Hakee listauksen hauista, vain voimassaolevat haut.', () => {
      cy.request('/hakukohderyhmapalvelu/api/haku?all=false').then(({ body }) =>
        expect(body).to.deep.equal([
          { oid: '1.2.3.4.5.2', nimi: { fi: 'Testihaku vain suomeksi' } },
          { oid: '1.2.3.4.5.3', nimi: { fi: 'Testihaku 3' } },
        ]),
      )
    })
  })
  describe('Hakukohteiden hakeminen haulle', () => {
    beforeEach(() => {
      cy.login()
      cy.mockBackendRequest({
        method: 'GET',
        path: '/kouta-internal/hakukohde/search?haku=1.2.4.1.1.1',
        service: 'kouta-service',
        responseFixture: 'hakukohderyhmapalvelu/get-hakukohde-response.json',
      })
      cy.mockBackendRequest({
        method: 'POST',
        path: '/organisaatio-service/rest/organisaatio/v4/findbyoids',
        service: 'organisaatio-service',
        requestFixture:
          'hakukohderyhmapalvelu/post-find-organisaatiot-request.json',
        responseFixture:
          'hakukohderyhmapalvelu/post-find-organisaatiot-response.json',
      })
    })
    it('Hakee listauksen haun hakukohteista', () => {
      cy.request('/hakukohderyhmapalvelu/api/haku/1.2.4.1.1.1/hakukohde').then(
        ({ body }) =>
          expect(body).to.deep.equal([
            {
              oid: '1.2.4.2.1.1',
              nimi: { fi: 'Testi-perustutkinto' },
              organisaatio: {
                oid: '1.2.10.1.2.1',
                nimi: { fi: 'Organisaatio 1' },
              },
            },
            {
              oid: '1.2.4.2.1.2',
              nimi: { fi: 'Testi-jatkotutkinto' },
              organisaatio: {
                oid: '1.2.10.1.2.2',
                nimi: { fi: 'Organisaatio 2' },
              },
            },
          ]),
      )
    })
  })
})
