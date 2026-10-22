import * as input from '../input'

const parseBody = (body: unknown): Record<string, unknown> =>
  typeof body === 'string'
    ? JSON.parse(body)
    : (body as Record<string, unknown>)

describe('Liitteiden muokkauksen takaraja (OY-5822)', () => {
  const hakuOid = '1.2.246.562.29.00000000000000000084'
  const parametriUrl = `http://localhost/ohjausparametrit-service/api/v1/rest/parametri/${hakuOid}`
  const asetuksetUrl = `/hakukohderyhmapalvelu/haun-asetukset?hakuOid=${hakuOid}`
  const eiTakarajaa =
    '[cypressid="haun-asetukset-liitteiden-muokkauksen-takaraja-radio-ei-takarajaa-input"]'
  const save = '[cypressid="save-ohjausparametrit"]'
  const priorisointi =
    '[cypressid="haun-asetukset-jarjestetyt-hakutoiveet-checkbox"][role="checkbox"]'
  const takarajaInput = 'haun-asetukset-liitteiden-muokkauksen-takaraja-input'
  const takarajaKeys = [
    'PH_LMT',
    'liitteidenMuokkauksenHakukohtainenTakarajaKellonaika',
    'liitteidenMuokkauksenHakemuskohtainenTakarajaKaytossa',
    'liitteidenMuokkauksenHakemuskohtainenTakarajaPaivaa',
    'liitteidenMuokkauksenHakemuskohtainenTakarajaKellonaika',
  ]
  const hakutavat = [
    { nimi: 'jatkuva haku', koodi: 'hakutapa_03#1', sallittu: true },
    { nimi: 'joustava haku', koodi: 'hakutapa_04', sallittu: true },
    { nimi: 'yhteishaku', koodi: 'hakutapa_01#1', sallittu: false },
    { nimi: 'erillishaku', koodi: 'hakutapa_02', sallittu: false },
  ]
  const takarajat = [
    {
      nimi: 'hakukohtainen',
      parametrit: {
        PH_LMT: { value: 14 },
        liitteidenMuokkauksenHakukohtainenTakarajaKellonaika: '16:00',
        liitteidenMuokkauksenHakemuskohtainenTakarajaKaytossa: false,
      },
    },
    {
      nimi: 'hakemuskohtainen',
      parametrit: {
        liitteidenMuokkauksenHakemuskohtainenTakarajaKaytossa: true,
        liitteidenMuokkauksenHakemuskohtainenTakarajaPaivaa: 10,
        liitteidenMuokkauksenHakemuskohtainenTakarajaKellonaika: '12:00',
      },
    },
  ]

  const avaaAsetukset = (
    hakutapa: string,
    takaraja: Record<string, unknown> = {},
  ) => {
    cy.mockBrowserRequest({
      method: 'GET',
      path: 'http://localhost/lomake-editori/api/forms',
      fixturePath: 'lomake-editori/get-forms-response.json',
      responseAlias: 'forms',
    })
    cy.mockBrowserRequest({
      method: 'GET',
      path: 'http://localhost/kayttooikeus-service/cas/me',
      fixturePath: 'kayttooikeus-service/get-me-response.json',
      responseAlias: 'me',
    })
    cy.fixture('kouta-internal/get-haku-response.json').then(haku => {
      cy.route2('GET', `http://localhost/kouta-internal/haku/${hakuOid}`, {
        ...haku,
        hakutapaKoodiUri: hakutapa,
      }).as('haku')
    })
    return cy
      .fixture('ohjausparametrit-service/get-parametri-response.json')
      .then(fixture => {
        const muutParametrit = {
          ...fixture,
          PH_HKP: { date: 1800089340000 },
          sijoittelu: false,
        }
        let parametrit: Record<string, unknown> = {
          ...muutParametrit,
          ...takaraja,
        }
        cy.route2('GET', parametriUrl, request => {
          request.reply({ body: parametrit })
        }).as('getParametrit')
        cy.route2('POST', parametriUrl, request => {
          parametrit = parseBody(request.body)
          request.reply({ statusCode: 200, body: '' })
        }).as('saveParametrit')
        cy.visit(asetuksetUrl)
        cy.wait(['@haku', '@getParametrit'])
        return cy.wrap(muutParametrit)
      })
  }

  beforeEach(() => {
    cy.resetMocks().then(() => cy.login())
  })

  hakutavat.forEach(({ nimi, koodi, sallittu }) => {
    it(`Näyttää takarajattoman vaihtoehdon vain sallituille hakutavoille: ${nimi}`, () => {
      avaaAsetukset(koodi)
      cy.get(eiTakarajaa).should(sallittu ? 'exist' : 'not.exist')
      if (sallittu) {
        cy.get(eiTakarajaa).should('have.attr', 'aria-checked', 'true')
      } else {
        cy.get(priorisointi).click()
        cy.get(save).should('be.disabled')
      }
    })

    if (sallittu) {
      it(`Tallentaa muut asetukset ilman liitteiden takarajaa: ${nimi}`, () => {
        avaaAsetukset(koodi).then(muutParametrit => {
          const expected = { ...muutParametrit, jarjestetytHakutoiveet: true }
          cy.get(priorisointi).click()
          cy.get(save).should('not.be.disabled').click()
          cy.wait('@saveParametrit')
            .its('request.body')
            .then(parseBody)
            .should('deep.equal', expected)
          cy.wait('@getParametrit')
            .its('response.body')
            .then(parseBody)
            .should('deep.equal', expected)
          cy.reload()
          cy.wait('@getParametrit')
          cy.get(eiTakarajaa).should('have.attr', 'aria-checked', 'true')
          cy.get(priorisointi).should('have.attr', 'aria-checked', 'true')
        })
      })

      takarajat.forEach(({ nimi: takarajaNimi, parametrit }) => {
        it(`Poistaa tallennetun takarajan ja säilyttää muut asetukset: ${nimi}, ${takarajaNimi}`, () => {
          avaaAsetukset(koodi, parametrit).then(muutParametrit => {
            cy.get(eiTakarajaa).should('have.attr', 'aria-checked', 'false')
            input.getInput(takarajaInput).should('be.visible')
            cy.get(eiTakarajaa).click()
            cy.get(eiTakarajaa).should('have.attr', 'aria-checked', 'true')
            input.getInput(takarajaInput).should('not.exist')
            cy.get(save).should('not.be.disabled').click()
            cy.wait('@saveParametrit')
              .its('request.body')
              .then(parseBody)
              .should('deep.equal', muutParametrit)
            cy.wait('@getParametrit')
              .its('response.body')
              .then(parseBody)
              .should('deep.equal', muutParametrit)
            cy.reload()
            cy.wait('@getParametrit')
              .its('response.body')
              .then(parseBody)
              .then(body => {
                takarajaKeys.forEach(key => {
                  expect(body).not.to.have.property(key)
                })
              })
            cy.get(eiTakarajaa).should('have.attr', 'aria-checked', 'true')
            input.getInput(takarajaInput).should('not.exist')
          })
        })
      })
    }
  })
})
