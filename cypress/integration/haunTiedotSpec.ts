/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

import { v4 as uuidv4 } from 'uuid'

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
          expect(body.slice(0, 4)).to.deep.equal([
            {
              oid: '1.2.4.2.1.1',
              nimi: { fi: 'Testi-perustutkinto' },
              hakuOid: '1.2.4.1.1.1',
              tarjoaja: {
                oid: '1.2.9.1.2.1',
                nimi: { fi: 'Organisaatio 1' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_01'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
              tila: 'julkaistu',
              oikeusHakukohteeseen: true,
              hasPaasyJaSoveltuvuuskoe: false,
              salliikoHakukohdeHarkinnanvaraisuudenKysymisen: true,
              toinenAsteOnkoKaksoistutkinto: true,
              sora: { tila: 'arkistoitu' },
              koulutustyyppikoodi: 'koulutustyyppi_10',
            },
            {
              oid: '1.2.4.2.1.2',
              nimi: { fi: 'Testi-jatkotutkinto' },
              hakuOid: '1.2.4.1.1.1',
              tarjoaja: {
                oid: '1.2.9.1.2.2',
                nimi: { fi: 'Organisaatio 2' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_02'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
              tila: 'julkaistu',
              oikeusHakukohteeseen: true,
              hasPaasyJaSoveltuvuuskoe: false,
              salliikoHakukohdeHarkinnanvaraisuudenKysymisen: true,
              toinenAsteOnkoKaksoistutkinto: false,
              sora: { tila: 'julkaistu' },
              koulutustyyppikoodi: null,
            },
            {
              oid: '1.2.4.2.1.3',
              nimi: { fi: 'Testi-ei-oikeuksia' },
              hakuOid: '1.2.4.1.1.1',
              tarjoaja: {
                oid: '1.2.9.1.2.3',
                nimi: { fi: 'Organisaatio, johon käyttäjällä ei ole asiaa' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_03'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
              tila: 'julkaistu',
              oikeusHakukohteeseen: false,
              hasPaasyJaSoveltuvuuskoe: false,
              salliikoHakukohdeHarkinnanvaraisuudenKysymisen: true,
              toinenAsteOnkoKaksoistutkinto: false,
              sora: { tila: 'arkistoitu' },
              koulutustyyppikoodi: 'koulutustyyppi_4',
            },
            {
              oid: '1.2.4.2.1.4',
              nimi: { fi: 'Testi-ei-oikeuksia-ryhmitelty' },
              hakuOid: '1.2.4.1.1.1',
              tarjoaja: {
                oid: '1.2.9.1.2.3',
                nimi: { fi: 'Organisaatio, johon käyttäjällä ei ole asiaa' },
                version: 0,
                parentOid: '1.2.0.0.0.0.1',
                tyypit: ['organisaatiotyyppi_03'],
                ryhmatyypit: [],
                kayttoryhmat: [],
              },
              tila: 'julkaistu',
              oikeusHakukohteeseen: false,
              hasPaasyJaSoveltuvuuskoe: false,
              salliikoHakukohdeHarkinnanvaraisuudenKysymisen: true,
              toinenAsteOnkoKaksoistutkinto: false,
              sora: { tila: 'julkaistu' },
              koulutustyyppikoodi: 'koulutustyyppi_26',
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
          oids: [
            '1.2.4.2.1.3',
            '1.2.4.2.1.4',
            '1.2.4.2.1.2',
            '1.2.4.2.1.1',
            '1.2.4.2.2.1',
            '1.2.4.2.1.9',
            '1.2.4.2.1.5',
            '1.2.4.2.1.6',
            '1.2.4.2.1.7',
            '1.2.4.2.1.8',
          ],
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
            settings: {
              rajaava: false,
              'max-hakukohteet': null,
              priorisoiva: false,
              prioriteettijarjestys: [],
              'jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja': false,
              'yo-amm-autom-hakukelpoisuus': false,
            },
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
            settings: {
              rajaava: false,
              'max-hakukohteet': null,
              priorisoiva: false,
              prioriteettijarjestys: [],
              'jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja': false,
              'yo-amm-autom-hakukelpoisuus': false,
            },
          },
        ])
      })
    })
    it('Ei palauta tyhjiä hakukohderyhmiä, kun includeEmpty parametri on false', () => {
      cy.request(
        'POST',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/search/find-by-hakukohde-oids',
        {
          oids: [
            '1.2.4.2.1.3',
            '1.2.4.2.1.4',
            '1.2.4.2.1.2',
            '1.2.4.2.1.1',
            '1.2.4.2.2.1',
            '1.2.4.2.1.9',
            '1.2.4.2.1.5',
            '1.2.4.2.1.6',
            '1.2.4.2.1.7',
            '1.2.4.2.1.8',
          ],
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
        expect(body).to.deep.equal([])
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
            sora: { tila: 'julkaistu' },
            hakuOid: '1.2.4.1.1.1',
            toinenAsteOnkoKaksoistutkinto: false,
            tila: 'julkaistu',
            tarjoaja: {
              oid: '1.2.9.1.2.2',
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
            sora: { tila: 'arkistoitu' },
            hakuOid: '1.2.4.1.1.1',
            toinenAsteOnkoKaksoistutkinto: false,
            tila: 'julkaistu',
            tarjoaja: {
              oid: '1.2.9.1.2.1',
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
          settings: {
            rajaava: false,
            'max-hakukohteet': null,
            priorisoiva: false,
            prioriteettijarjestys: [],
            'jos-ylioppilastutkinto-ei-muita-pohjakoulutusliitepyyntoja': false,
            'yo-amm-autom-hakukelpoisuus': false,
          },
          hakukohteet: [
            {
              oid: '1.2.4.2.1.1',
              nimi: { fi: 'Testi-perustutkinto' },
              hakuOid: '1.2.4.1.1.1',
              oikeusHakukohteeseen: true,
              hasPaasyJaSoveltuvuuskoe: false,
              toinenAsteOnkoKaksoistutkinto: false,
              sora: { tila: 'arkistoitu' },
              tila: 'julkaistu',
              tarjoaja: {
                oid: '1.2.9.1.2.1',
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
              hasPaasyJaSoveltuvuuskoe: false,
              toinenAsteOnkoKaksoistutkinto: false,
              sora: { tila: 'julkaistu' },
              tila: 'julkaistu',
              tarjoaja: {
                oid: '1.2.9.1.2.2',
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
      cy.mockBackendRequest({
        method: 'GET',
        path: '/lomake-editori/api/forms?hakukohderyhma-oid=1.2.246.562.28.001',
        service: 'ataru-service',
        responseFixture: 'hakukohderyhmapalvelu/get-forms-empty-response.json',
      })
      cy.mockBackendRequest({
        method: 'GET',
        path:
          '/lomake-editori/api/forms?hakukohderyhma-oid=1.2.246.562.28.001001',
        service: 'ataru-service',
        responseFixture:
          'hakukohderyhmapalvelu/get-forms-non-empty-response.json',
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
    it('Poisto epäonnistuu, hakukohderyhmää käytetään atarussa', () => {
      cy.request({
        method: 'DELETE',
        url: '/hakukohderyhmapalvelu/api/hakukohderyhma/1.2.246.562.28.001001',
        failOnStatusCode: false,
      }).then(({ status, body }) => {
        expect(status).to.equal(409)
        expect(body).to.deep.equal({ status: 'in-use' })
      })
    })
  })

  describe('Hakukohderyhmien ryhmittely hakukohteittain', () => {
    it('Palauttaa hakukohderyhmät ryhmiteltyinä', () => {
      interface HakukohdeResponse {
        oid: string
        hakukohderyhmat: string[]
      }

      const insertRow = (hakukohdeOid: string, hakukohderyhmaOid: string) => {
        cy.task('query', {
          sql: `
                INSERT INTO hakukohderyhma (hakukohde_oid, hakukohderyhma_oid)
                VALUES ($1, $2) ON CONFLICT DO NOTHING 
            `,
          values: [hakukohdeOid, hakukohderyhmaOid],
        })
      }

      insertRow('1.2.246.562.20.001', '1.2.246.562.28.00001')
      insertRow('1.2.246.562.20.001', '1.2.246.562.28.00002')
      insertRow('1.2.246.562.20.002', '1.2.246.562.28.00003')
      cy.login()
      cy.request(
        'POST',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/search/by-hakukohteet',
        ['1.2.246.562.20.001', '1.2.246.562.20.002'],
      ).then(({ status, body }) => {
        expect(status).to.equal(200)

        const sortedInner = body
          .map((hk: HakukohdeResponse) => ({
            ...hk,
            hakukohderyhmat: hk.hakukohderyhmat.sort(),
          }))
          .sort((a: HakukohdeResponse, b: HakukohdeResponse) =>
            a.oid > b.oid ? 1 : -1,
          )

        const expected = [
          {
            oid: '1.2.246.562.20.001',
            hakukohderyhmat: ['1.2.246.562.28.00001', '1.2.246.562.28.00002'],
          },
          {
            oid: '1.2.246.562.20.002',
            hakukohderyhmat: ['1.2.246.562.28.00003'],
          },
        ]

        expect(sortedInner).to.deep.eq(expected)
      })
    })
    it('Palauttaa hakukohderyhmät ryhmiteltyinä, ei hakukohteita', () => {
      cy.login()
      cy.request(
        'POST',
        '/hakukohderyhmapalvelu/api/hakukohderyhma/search/by-hakukohteet',
        [],
      ).then(({ status, body }) => {
        expect(status).to.equal(200)
        expect(body).to.deep.eq([])
      })
    })
  })

  describe('CAS-logout', () => {
    const id = uuidv4()
    const data = {
      identity: {
        oid: '1.2.246.562.24.1',
        lang: 'fi',
        ticket: 'TICKET-123',
        username: 'user1',
        'last-name': 'Last',
        'first-name': 'First',
        organizations: ['1.2.246.562.10.00000000001'],
      },
      'client-ip': '127.0.0.1',
      'logged-in': true,
      'user-agent': 'None',
      'ring.middleware.session-timeout/idle-timeout': 12345567,
    }
    const logoutRequest = `
    <samlp:LogoutRequest xmlns:samlp="urn:oasis:names:tc:SAML:2.0:protocol" ID="id" Version="2.0" IssueInstant="2021-05-05T10:00:00Z">
        <saml:NameID xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion">user1</saml:NameID>
        <samlp:SessionIndex>TICKET-123</samlp:SessionIndex>
    </samlp:LogoutRequest>
    `

    it('Uloskirjaus onnistuu', () => {
      cy.task('query', {
        sql: 'INSERT INTO sessions(key, data) VALUES ($1, $2)',
        values: [id, JSON.stringify(data)],
      })
        .request({
          method: 'POST',
          url: '/hakukohderyhmapalvelu/auth/cas',
          form: true,
          body: { logoutRequest },
        })
        .task('query', {
          sql:
            'SELECT data->\'logged-in\' AS "loggedIn" FROM sessions WHERE key = $1',
          values: [id],
        })
        .then((res: any) => { // eslint-disable-line
          const { loggedIn } = res.rows[0]
          expect(loggedIn).to.equal(false)
        })
    })
  })

  after('Clean up db', () => {
    cy.task('query', {
      sql: 'TRUNCATE hakukohderyhma',
    })
  })
})
