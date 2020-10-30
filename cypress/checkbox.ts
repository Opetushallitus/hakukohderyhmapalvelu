import Chainable = Cypress.Chainable

export const getCheckbox = (
  cypressid: string,
): Chainable<JQuery<HTMLDivElement>> =>
  cy.get(`div[cypressid=${cypressid}][role=checkbox]`)

export const beChecked = (checkbox: JQuery<HTMLDivElement>): void => {
  expect(checkbox.attr('aria-checked')).to.equal('true')
}

export const notBeChecked = (checkbox: JQuery<HTMLDivElement>): void => {
  expect(checkbox.attr('aria-checked')).to.equal('false')
}
