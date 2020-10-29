import GetFormsResponse from '../fixtures/lomake-editori/GetFormsResponse'
import GetKoutaInternalHakuResponse from '../fixtures/kouta-internal/GetKoutaInternalHaku'
import GetParametriResponse from '../fixtures/ohjausparametrit-service/GetParametriResponse'

import * as ha from '../selectors/haunAsetuksetPanelSelectors'

describe('Haun asetukset', () => {
  const hakuOid = '1.2.246.562.29.00000000000000000084'

  before(() => {
    cy.resetMocks()
      .then(() => cy.login())
      .then(() =>
        cy
          .fixture<GetFormsResponse>('lomake-editori/get-forms-response.json')
          .then(getFormsResponse =>
            cy
              .route2(
                'http://localhost/lomake-editori/api/forms',
                getFormsResponse,
              )
              .as('lomake-editori-get-forms-response'),
          ),
      )
      .then(() =>
        cy
          .fixture<GetKoutaInternalHakuResponse>(
            'kouta-internal/get-haku-response',
          )
          .then(getHakuResponse =>
            cy
              .route2(
                `http://localhost/kouta-internal/haku/${hakuOid}`,
                getHakuResponse,
              )
              .as('kouta-internal-get-haku-response'),
          ),
      )
      .then(() =>
        cy
          .fixture<GetParametriResponse>(
            'ohjausparametrit-service/get-parametri-response.json',
          )
          .then(getParametriResponse =>
            cy
              .route2(
                `http://localhost/ohjausparametrit-service/api/v1/rest/parametri/${hakuOid}`,
                getParametriResponse,
              )
              .as('ohjausparametrit-service-get-parametri-response'),
          ),
      )
      .then(() =>
        cy.visit(`/hakukohderyhmapalvelu/haun-asetukset?hakuOid=${hakuOid}`),
      )
  })

  // eslint-disable-next-line @typescript-eslint/no-empty-function
  it('Näyttää haun asetukset', () => {
    cy.get(ha.hakuajatSelector(hakuOid))
      .find(`:contains('14.09.2020 klo 00.00.00 - 30.09.2020 klo 00.00.00')`)
      .should('exist')
    cy.get(ha.hakuajatSelector(hakuOid))
      .find(`:contains('14.10.2020 klo 00.00.00 - 15.10.2020 klo 00.00.00')`)
      .should('exist')
  })
})
