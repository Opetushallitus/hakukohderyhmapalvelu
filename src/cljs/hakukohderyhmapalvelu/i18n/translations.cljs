(ns hakukohderyhmapalvelu.i18n.translations)

(def haun-asetukset-translations
  {:haun-asetukset/title
   {:fi "Haun asetukset"}
   :haun-asetukset/hakukohteiden-maara-rajoitettu
   {:fi "Hakutoiveiden määrä rajoitettu"}
   :haun-asetukset/hakukohteiden-maara
   {:fi "Hakutoiveiden enimmäismäärä"}
   :haun-asetukset/jarjestetyt-hakutoiveet
   {:fi "Hakutoiveiden priorisointi"}
   :haun-asetukset/useita-hakemuksia
   {:fi "Vain yksi hakemus -rajoitus"}
   :haun-asetukset/hakijakohtainen-paikan-vastaanottoaika
   {:fi "Hakijakohtainen paikan vastaanottoaika (vrk)"}
   :haun-asetukset/paikan-vastaanotto-paattyy
   {:fi "Hakijan paikan vastaanotto päättyy"}
   :haun-asetukset/hakukierros-paattyy
   {:fi "Hakukierros päättyy"}
   :haun-asetukset/sijoittelu
   {:fi "Haussa käytetään sijoittelua"}
   :haun-asetukset/valintatulokset-valmiina-viimeistaan
   {:fi "Valintatulokset valmiina viimeistään"}
   :haun-asetukset/varasijasaannot-astuvat-voimaan
   {:fi "Varasijasäännöt astuvat voimaan"}
   :haun-asetukset/varasijataytto-paattyy
   {:fi "Varasijatäyttö päättyy"}
   :haun-asetukset/input-date-describedby
   {:fi "Syötä tähän kenttään päivämäärästä ja kellonajasta muodostuvan arvon päivämäärä"}
   :haun-asetukset/input-time-describedby
   {:fi "Syötä tähän kenttään päivämäärästä ja kellonajasta muodostuvan arvon kellonaika"}})

(def common-translations
  {:application-periods
   {:fi "Hakuajat"}
   :application-form
   {:fi "Hakulomake"}
   :modify-form
   {:fi "muokkaa lomaketta"}
   :modify-haku
   {:fi "muokkaa hakua"}
   :required-legend
   {:fi "* merkityt kentät ovat pakollisia"}
   :no-selectable-items
   {:fi "Valittavia kohteita ei löytynyt"}})

(def hakukohderyhma-translations
  {:haku/haku
   {:fi "Haku"}
   :haku/haku-search-placeholder
   {:fi "Haun nimi"}
   :haku/show-all-haut
   {:fi "Näytä myös päättyneet"}
   :haku/hakukohteet
   {:fi "Hakukohteet"}})

(def translations
  (merge
    hakukohderyhma-translations
    haun-asetukset-translations
    common-translations))
