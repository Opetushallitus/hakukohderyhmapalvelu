export const hakukohderyhmanLisaysHeadingSelector =
  'label[cypressid=hakukohderyhma-select-label]:visible'

export const hakukohderyhmanLisaysDropdownSelectorUndropped =
  'span[cypressid=hakukohderyhma-select-dropdown-label--undropped]:visible'

export const hakukohderyhmanLisaysDropdownSelectorDropped =
  'span[cypressid=hakukohderyhma-select-dropdown-label--dropped]:visible'

export const hakukohderyhmanLisaysDropdownSelectorItem = (
  itemName: string,
): string => `div[cypressid=dropdown-selector--${itemName}]:visible`

export const hakukohderyhmanLisaysLisaaUusiRyhmaLinkSelector =
  'button[cypressid=hakukohderyhma-select-control--add-new-hakukohderyhma]:visible'

export const hakukohderyhmanLisaysNewHakukohderyhmaNameTextInputSelector =
  'input[cypressid=hakukohderyhma-create-input]:visible'

export const hakukohderyhmanLisaysSaveNewHakukohderyhmaButtonSelector =
  'button[cypressid=hakukohderyhma-create-button]:visible'
