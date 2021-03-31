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
              hakuOid: '1.2.4.1.1.1',
              organisaatio: {
                oid: '1.2.10.1.2.1',
                nimi: { fi: 'Organisaatio 1' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_01'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
            },
            {
              oid: '1.2.4.2.1.2',
              nimi: { fi: 'Testi-jatkotutkinto' },
              hakuOid: '1.2.4.1.1.1',
              organisaatio: {
                oid: '1.2.10.1.2.2',
                nimi: { fi: 'Organisaatio 2' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_02'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
            },
          ]),
      )
    })
  })
  describe('Hakukohderyhmän hakeminen hakukohteille', () => {
    beforeEach(() => {
      cy.login()
      cy.mockBackendRequest({
        method: 'GET',
        path:
          '/organisaatio-service/rest/organisaatio/v3/ryhmat?ryhmatyyppi=ryhmatyypit_6%231',
        service: 'organisaatio-service',
        responseFixture:
          'hakukohderyhmapalvelu/get-organisaatio-ryhmat-response.json',
      })
      cy.mockBackendRequest({
        method: 'POST',
        path: '/kouta-internal/hakukohde/findbyoids',
        service: 'kouta-service',
        requestFixture:
          'hakukohderyhmapalvelu/post-find-hakukohteet-by-oids.json',
        responseFixture: 'hakukohderyhmapalvelu/get-hakukohde-response.json',
      })
    })
    it('Palauttaa tyhjät hakukohderyhmät, kun request bodyssa on hakukohteen oideja', () => {
      cy.request(
        'POST',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/find-by-hakukohde-oids',
        { oids: ['1.2.4.2.1.1', '1.2.4.2.1.2'] },
      ).then(({ body }) => {
        expect(body).to.deep.equal([
          {
            oid: '1.1.2.5.2.9',
            nimi: {
              fi: 'Kinuskiryhmä',
              en: 'Caramel group',
              sv: 'Kolagrupp',
            },
            version: 0,
            parentOid: '1.2.2.5.1.0',
            ryhmatyypit: ['ryhmatyypit_6#1'],
            kayttoryhmat: ['kayttoryhmat_1#1'],
            tyypit: ['Ryhma'],
            hakukohteet: [],
          },
          {
            oid: '1.2.2.5.2.9',
            nimi: {
              fi: 'Suklaaryhmä',
              en: 'Chocolate group',
              sv: 'Chokladgrupp',
            },
            version: 0,
            parentOid: '1.2.2.5.1.0',
            ryhmatyypit: ['ryhmatyypit_6#1'],
            kayttoryhmat: ['kayttoryhmat_1#1'],
            tyypit: ['Ryhma'],
            hakukohteet: [],
          },
        ])
      })
    })
  })
  describe('Hakukohderyhmän liitoksien tallentaminen', () => {
    beforeEach(() => {
      cy.login()
      cy.mockBackendRequest({
        method: 'GET',
        path: '/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.4',
        service: 'organisaatio-service',
        responseFixture:
          'hakukohderyhmapalvelu/get-hakukohderyhma-response.json',
      })
      cy.login()
      cy.mockBackendRequest({
        method: 'POST',
        path: '/kouta-internal/hakukohde/findbyoids',
        service: 'kouta-service',
        responseFixture: 'hakukohderyhmapalvelu/empty-array.json',
        requestFixture: 'hakukohderyhmapalvelu/empty-array.json',
      })
      cy.login()
      cy.mockBackendRequest({
        method: 'POST',
        path: '/kouta-internal/hakukohde/findbyoids',
        service: 'kouta-service',
        responseFixture: 'hakukohderyhmapalvelu/empty-array.json',
        requestFixture: 'hakukohderyhmapalvelu/empty-array.json',
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
    })

    it('Päivittää hakukohderyhmän hakukohteet', () => {
      cy.request(
        'PUT',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/1.2.246.562.28.4/hakukohteet',
        [
          {
            oid: '1.2.4.2.1.1',
            nimi: { fi: 'Testi-perustutkinto' },
            hakuOid: '1.2.4.1.1.1',
            organisaatio: {
              oid: '1.2.10.1.2.1',
              nimi: { fi: 'Organisaatio 1' },
              version: 0,
              parentOid: '1.1.4.2.1.1',
              tyypit: [],
              ryhmatyypit: [],
              kayttoryhmat: [],
            },
          },
          {
            oid: '1.2.4.2.1.2',
            nimi: { fi: 'Testi-jatkotutkinto' },
            hakuOid: '1.2.4.1.1.1',
            organisaatio: {
              oid: '1.2.10.1.2.2',
              nimi: { fi: 'Organisaatio 2' },
              version: 0,
              parentOid: '1.1.4.2.1.1',
              tyypit: [],
              ryhmatyypit: [],
              kayttoryhmat: [],
            },
          },
        ],
      ).then(({ body }) =>
        expect(body).to.deep.equal({
          oid: '1.2.246.562.28.4',
          nimi: { fi: 'Hakukohderyhmä 1' },
          version: 0,
          parentOid: '1.2.246.562.28.01',
          tyypit: [],
          ryhmatyypit: [],
          kayttoryhmat: [],
          hakukohteet: [
            {
              oid: '1.2.4.2.1.1',
              nimi: { fi: 'Testi-perustutkinto' },
              hakuOid: '1.2.4.1.1.1',
              organisaatio: {
                oid: '1.2.10.1.2.1',
                nimi: { fi: 'Organisaatio 1' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_01'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
            },
            {
              oid: '1.2.4.2.1.2',
              nimi: { fi: 'Testi-jatkotutkinto' },
              hakuOid: '1.2.4.1.1.1',
              organisaatio: {
                oid: '1.2.10.1.2.2',
                nimi: { fi: 'Organisaatio 2' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_02'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
            },
          ],
        }),
      )
    })
  })
})
