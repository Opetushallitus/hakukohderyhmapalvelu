import Chainable = Cypress.Chainable

export const getInput = (
  cypressid: string,
): Chainable<JQuery<HTMLInputElement>> =>
  cy.get(`input[cypressid=${cypressid}]`)
