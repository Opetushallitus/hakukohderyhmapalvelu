/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

describe('Hakukohderyhmäpalvelu - haun tiedot', () => {
  const tarjoajaParameter = 'tarjoaja=1.2.246.562.10.0439845%2C1.2.246.562.28.1'

  before(() => {
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
        path: `/kouta-internal/haku/search?${tarjoajaParameter}`,
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
        path: `/kouta-internal/hakukohde/search?haku=1.2.4.1.1.1&${tarjoajaParameter}&all=true`,
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
              oikeusHakukohteeseen: true,
              toinenAsteOnkoKaksoistutkinto: true,
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
              oikeusHakukohteeseen: true,
              toinenAsteOnkoKaksoistutkinto: false,
            },
            {
              oid: '1.2.4.2.1.3',
              nimi: { fi: 'Testi-ei-oikeuksia' },
              hakuOid: '1.2.4.1.1.1',
              organisaatio: {
                oid: '1.2.10.1.2.3',
                nimi: { fi: 'Organisaatio, johon käyttäjällä ei ole asiaa' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_03'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
              oikeusHakukohteeseen: false,
              toinenAsteOnkoKaksoistutkinto: false,
            },
            {
              oid: '1.2.4.2.1.4',
              nimi: { fi: 'Testi-ei-oikeuksia-ryhmitelty' },
              hakuOid: '1.2.4.1.1.1',
              organisaatio: {
                oid: '1.2.10.1.2.3',
                nimi: { fi: 'Organisaatio, johon käyttäjällä ei ole asiaa' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_03'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
              oikeusHakukohteeseen: false,
            },
          ]),
      )
    })
  })
  describe('Hakukohderyhmien kyseleminen hakukohteille', () => {
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
        path: `/kouta-internal/hakukohde/findbyoids?${tarjoajaParameter}`,
        service: 'kouta-service',
        requestFixture:
          'hakukohderyhmapalvelu/post-find-hakukohteet-by-oids.json',
        responseFixture: 'hakukohderyhmapalvelu/get-hakukohde-response.json',
      })
    })
    it('Palauttaa tyhjät hakukohderyhmät, kun request bodyssa on hakukohteen oideja ja includeEmpty parametri on true', () => {
      cy.request(
        'POST',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/search/find-by-hakukohde-oids',
        {
          oids: ['1.2.4.2.1.3', '1.2.4.2.1.4', '1.2.4.2.1.2', '1.2.4.2.1.1'],
          includeEmpty: true,
        },
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
    it('Ei palauta tyhjiä hakukohderyhmiä, kun includeEmpty parametri on false', () => {
      cy.request(
        'POST',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/search/find-by-hakukohde-oids',
        {
          oids: ['1.2.4.2.1.3', '1.2.4.2.1.4', '1.2.4.2.1.2', '1.2.4.2.1.1'],
          includeEmpty: false,
        },
      ).then(({ body }) => {
        expect(body).to.deep.equal([])
      })
    })
  })
  describe('Hahkukohteen hakukohderyhmien oidien hakeminen (rajapinta ulkoiseen käyttöön)', () => {
    beforeEach(() => {
      cy.login()
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
        path: `/kouta-internal/hakukohde/findbyoids?${tarjoajaParameter}`,
        service: 'kouta-service',
        requestFixture:
          'hakukohderyhmapalvelu/post-find-authorized-hakukohteet-by-oids.json',
        responseFixture:
          'hakukohderyhmapalvelu/find-authorized-hakukohde-response.json',
      })
      cy.login()
      cy.mockBackendRequest({
        method: 'POST',
        path: '/organisaatio-service/rest/organisaatio/v4/findbyoids',
        service: 'organisaatio-service',
        requestFixture:
          'hakukohderyhmapalvelu/post-find-authorized-organisaatiot-request.json',
        responseFixture:
          'hakukohderyhmapalvelu/post-find-authorized-organisaatiot-response.json',
      })
    })

    it('Hahkukohteen hakukohderyhmien GET-reitti palauttaa tyhjän listan, jos hakukohde ei kuulu mihinkään ryhmään', () => {
      cy.request(
        '/hakukohderyhmapalvelu/api/hakukohde/1.2.4.2.1.1/hakukohderyhmat',
      ).then(({ body }) => {
        expect(body).to.deep.equal([]) //FIXME: this fails after first run because of no proper db cleanup
      })
    })

    it('Päivittää hakukohderyhmän hakukohteet', () => {
      cy.request(
        'PUT',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/1.2.246.562.28.4/hakukohteet',
        [
          {
            oid: '1.2.4.2.1.2',
            nimi: { fi: 'Testi-jatkotutkinto' },
            hakuOid: '1.2.4.1.1.1',
            toinenAsteOnkoKaksoistutkinto: false,
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
          {
            oid: '1.2.4.2.1.1',
            nimi: { fi: 'Testi-perustutkinto' },
            hakuOid: '1.2.4.1.1.1',
            toinenAsteOnkoKaksoistutkinto: false,
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
              oikeusHakukohteeseen: true,
              toinenAsteOnkoKaksoistutkinto: false,
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
              oikeusHakukohteeseen: true,
              toinenAsteOnkoKaksoistutkinto: false,
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
    it('Hahkukohteen hakukohderyhmien GET-reitti palauttaa hakukohderyhmien oidit, joihin annettu hakukohde kuuluu', () => {
      cy.request(
        '/hakukohderyhmapalvelu/api/hakukohde/1.2.4.2.1.1/hakukohderyhmat',
      ).then(({ body }) => {
        expect(body).to.deep.equal(['1.2.246.562.28.4'])
      })
    })
  })
  describe('Hakukohderyhmän poistaminen', () => {
    beforeEach(() => {
      cy.login()
      cy.mockBackendRequest({
        method: 'DELETE',
        path: '/organisaatio-service/rest/organisaatio/v4/1.2.246.562.28.001',
        service: 'organisaatio-service',
        responseFixture:
          'hakukohderyhmapalvelu/delete-organisaatio-response.json',
      })
    })
    it('Poisto onnistuu', () => {
      cy.request(
        'DELETE',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/1.2.246.562.28.001',
      ).then(({ status, body }) => {
        expect(status).to.equal(200)
        expect(body).to.deep.equal({ status: 'deleted' })
      })
    })
  })

  after('Clean up db', () => {
    cy.task('query', {
      sql: 'TRUNCATE hakukohderyhma',
    })
  })
})
