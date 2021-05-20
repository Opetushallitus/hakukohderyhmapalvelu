export const haunHakutoimintoTitleSelector =
  'span[cypressid=haku-search-title]:visible'

export const haunHakutoimintoDivSelector =
  'div[cypressid=haku-search-cypress]:visible'

export const haunHakutoimintoDivSelectorChildDivs =
  'div[cypressid=haku-search-cypress]:visible > div'

export const hakukohteidenSuodatusInputSelector =
  'input[cypressid=hakukohteet-text-filter]'

export const haunHakutoimintoNaytaMyosPaattyneetCheckboxSelector =
  'div[cypressid=haku-search-checkbox-cypress-input]:visible'

export const haunHakutoimintoNaytaMyosPaattyneetTextSelector =
  'label[cypressid=haku-search-checkbox-cypress-label]:visible'

export const hakukohteetContainerSelector =
  'div[cypressid=hakukohteet-container]'

export const hakukohteetContainerOptionSelector = (
  label: string,
  isSelected: boolean,
): string => {
  return `div[cypressid=hakukohteet-container__${label}${
    isSelected ? '--selected' : ''
  }]`
}

export const hakukohderyhmanHakukohteetContainerSelector =
  'div[cypressid=hakukohderyhma-hakukohteet]'

export const poistaRyhmastaButtonSelector =
  'button[cypressid=remove-from-group-btn]'

export const hakukohteetLisaysButtonSelector =
  'button[cypressid=add-to-group-btn]'

export const hakukohdeSelectAllSelector = 'button[cypressid=select-all-btn]'

export const hakukohdeDeselectAllSelector = 'button[cypressid=deselect-all-btn]'

export const extraFiltersButtonSelector = 'button[cypressid=extra-filters-btn]'

export const extraFiltersPopupClose = 'div[cypressid=extra-filters-popup-close]'

export const extraFilterBooleanSelector = (name: string): string =>
  `div[cypressid=${name}-extra-filter-input]`

export const extraFilterSelectSelector = (name: string): string =>
  `div[cypressid=${name}-extra-filter]`
