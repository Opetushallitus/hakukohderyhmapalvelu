/// <reference types="cypress"/>
// eslint-disable-next-line @typescript-eslint/triple-slash-reference
/// <reference path="../support/commands.d.ts"/>

describe('Hakukohderyhmäpalvelu - haun tiedot', () => {
  before(() => {
    cy.resetMocks()
    cy.login()
    cy.visit('/')
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
})
