import * as ha from '../selectors/haunAsetuksetPanelSelectors'
import * as checkbox from '../checkbox'
import * as input from '../input'

describe('Haun asetukset', () => {
  const hakuOid = '1.2.246.562.29.00000000000000000084'

  before(() => {
    cy.resetMocks()
      .then(() => cy.login())
      .then(() =>
        cy.mockBrowserRequest({
          method: 'GET',
          path: 'http://localhost/lomake-editori/api/forms',
          fixturePath: 'lomake-editori/get-forms-response.json',
          responseAlias: 'lomake-editori-get-forms-response',
        }),
      )
      .then(() =>
        cy.mockBrowserRequest({
          method: 'GET',
          path: `http://localhost/kouta-internal/haku/${hakuOid}`,
          fixturePath: 'kouta-internal/get-haku-response.json',
          responseAlias: 'kouta-internal-get-haku-response',
        }),
      )
      .then(() =>
        cy.mockBrowserRequest({
          method: 'GET',
          path: `http://localhost/ohjausparametrit-service/api/v1/rest/parametri/${hakuOid}`,
          fixturePath: 'ohjausparametrit-service/get-parametri-response.json',
          responseAlias: 'ohjausparametrit-service-get-parametri-response',
        }),
      )
      .then(() =>
        cy.visit(`/hakukohderyhmapalvelu/haun-asetukset?hakuOid=${hakuOid}`),
      )
  })

  it('Näyttää haun asetukset', () => {
    cy.get(ha.hakuajatSelector(hakuOid))
      .find(`:contains('14.09.2020 klo 00.00.00 - 30.09.2020 klo 00.00.00')`)
      .should('exist')
    cy.get(ha.hakuajatSelector(hakuOid))
      .find(`:contains('14.10.2020 klo 00.00.00 - 15.10.2020 klo 00.00.00')`)
      .should('exist')
    checkbox
      .getCheckbox('haun-asetukset-hakukohteiden-maara-rajoitettu-checkbox')
      .should('exist')
      .should(checkbox.beChecked)
    input
      .getInput('haun-asetukset-hakukohteiden-maara-rajoitettu-input')
      .should('exist')
      .should('have.value', '1')
    checkbox
      .getCheckbox('haun-asetukset-jarjestetyt-hakutoiveet-checkbox')
      .should('exist')
      .should(checkbox.notBeChecked)
    checkbox
      .getCheckbox('haun-asetukset-useita-hakemuksia-checkbox')
      .should('exist')
      .should(checkbox.beChecked)
    input
      .getInput('haun-asetukset-hakijakohtainen-paikan-vastaanottoaika-input')
      .should('exist')
      .should('have.value', '5')
  })
})
