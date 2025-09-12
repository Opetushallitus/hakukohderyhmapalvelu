(ns hakukohderyhmapalvelu.i18n.translations)

(def local-translations
  {:yleiset        {:ei-valittavia-kohteita {:fi "Valittavia kohteita ei löytynyt"}
                    :vahvista-poisto        {:fi "Vahvista poisto"}
                    :peruuta                {:fi "Peruuta"}
                    :hakulomake             {:fi "Hakulomake"}
                    :tallenna               {:fi "Tallenna"}
                    :hakuajat               {:fi "Hakuajat"}
                    :muokkaa-lomaketta      {:fi "Muokkaa lomaketta"}
                    :muokkaa-hakua          {:fi "Muokkaa hakua"}
                    :pakolliset-kentat      {:fi "* merkityt kentät ovat pakollisia"}
                    :http-virhe             {:fi "Tietojen haku epäonnistui"}
                    :arkistoitu             {:fi "Arkistoitu"}
                    :http-403               {:fi "Ei oikeuksia"}}
   :haun-asetukset {:valintatulokset-valmiina-viimeistaan                        {:fi "Valintatulokset valmiina viimeistään"}
                    :useita-hakemuksia                                           {:fi "Vain yksi hakemus -rajoitus"}
                    :haun-asetukset                                              {:fi "Haun asetukset"}
                    :synteettiset-hakemukset                                     {:fi "Haussa käytetään synteettisiä hakemuksia"}
                    :synteettisen-hakemuksen-lomakeavain                         {:fi "Synteettisten hakemusten lomakeavain"}
                    :hakukohteiden-enimmaismaara                                 {:fi "Hakutoiveiden enimmäismäärä"}
                    :hakukierros-paattyy                                         {:fi "Hakukierros päättyy"}
                    :varasijataytto-paattyy                                      {:fi "Varasijatäyttö päättyy"}
                    :hakukohteiden-maara-rajoitettu                              {:fi "Hakutoiveiden määrä rajoitettu"}
                    :jarjestetyt-hakutoiveet                                     {:fi "Hakutoiveiden priorisointi"}
                    :varasijasaannot-astuvat-voimaan                             {:fi "Varasijasäännöt astuvat voimaan"}
                    :paikan-vastaanotto-paattyy                                  {:fi "Hakijan paikan vastaanotto päättyy"}
                    :hakijakohtainen-paikan-vastaanottoaika                      {:fi "Hakijakohtainen paikan vastaanottoaika (vrk)"}
                    :syota-kellonaika                                            {:fi "Syötä tähän kenttään päivämäärästä ja kellonajasta muodostuvan arvon kellonaika"}
                    :syota-paivamaara                                            {:fi "Syötä tähän kenttään päivämäärästä ja kellonajasta muodostuvan arvon päivämäärä"}
                    :aikavali                                                    {:fi "Aikaväli"}
                    :aikavali-alku                                               {:fi "Aikavälin alkuhetki"}
                    :aikavali-loppu                                              {:fi "Aikavälin loppuhetki"}
                    :sijoittelu                                                  {:fi "Haussa käytetään sijoittelua"}
                    :valintatulosten-julkaiseminen-hakijoille                    {:fi "Valintatulosten julkaiseminen hakijoille (aikaväli)"}
                    :liitteiden-muokkauksen-takaraja                             {:fi "Liitteiden muokkauksen takaraja on"}
                    :liitteiden-muokkauksen-takaraja-hakukohtainen               {:fi "Hakukohtainen"}
                    :liitteiden-muokkauksen-takaraja-hakemuskohtainen            {:fi "Hakemuskohtainen"}
                    :liitteiden-muokkauksen-takaraja-vuorokausina                {:fi "Takaraja vuorokausina ja kellonaika"}
                    :ilmoittautuminen-paattyy                                    {:fi "Ilmoittautuminen päättyy"}
                    :automaattinen-hakukelpoisuus-paattyy                        {:fi "Automaattinen hakukelpoisuus päättyy"}
                    :harkinnanvaraisen-valinnan-paatosten-tallennus-paattyy      {:fi "Harkinnanvaraisen valinnan päätösten tallennus päättyy"}
                    :oppilaitosten-virkailijoiden-valintapalvelun-kaytto-estetty {:fi "Oppilaitosten virkailijoiden valintapalvelun käyttö estetty"}
                    :valintaesityksen-hyvaksyminen                               {:fi "Valintaesityksen hyväksyminen"}
                    :koetulosten-tallentaminen                                   {:fi "Koetulosten tallentaminen"}
                    :muutoksia-ei-viela-tallennettu                              {:fi "Muutoksia ei vielä tallennettu"}
                    :tallenna                                                    {:fi "TALLENNA"}}
   :hakukohderyhma {:haku                         {:fi "Haku"}
                    :lisarajain-harkinnanvaraiset {:fi "Hakukohteella harkinnanvarainen valinta"}
                    :lisarajain-kaksoistutkinto   {:fi "Hakukohteella mahdollisuus kaksoistutkintoon"}
                    :lisarajain-koulutustyypit    {:fi "Koulutustyypit"}
                    :lisarajain-urheilu           {:fi "Urheiluoppilaitoksien hakukohteet"}
                    :nimi-tai-organisaatio        {:fi "Nimi tai organisaatio"}
                    :poista-valinnat              {:fi "Poista valinnat"}
                    :valitse-kaikki               {:fi "Valitse kaikki"}
                    :luo-uusi-ryhma               {:fi "Luo uusi ryhmä"}
                    :hakukohteet                  {:fi "Hakukohteet"}
                    :nayta-myos-paattyneet        {:fi "Näytä myös päättyneet"}
                    :lisarajaimet                 {:fi "Lisäsuodattimet"}
                    :hakukohderyhma-nimi          {:fi "Hakukohderyhmän nimi"}
                    :haun-nimi                    {:fi "Haun nimi"}
                    :hakukohderyhma-kaytossa      {:fi "Hakukohderyhmä on käytössä hakulomakkeella ja sitä ei voi poistaa."}
                    :liita-ryhmaan                {:fi "Liitä ryhmään"}
                    :sora-hakukohteet             {:fi "SORA -hakukohteet"}
                    :muokkaa-ryhmaa               {:fi "Muokkaa ryhmää"}
                    :poista-ryhmasta              {:fi "Poista ryhmästä"}
                    :max-hakukohteet              {:fi "Ryhmän hakukohteita valittavissa enintään"}
                    :rajaava                      {:fi "Rajaava"}
                    :priorisoiva                  {:fi "Priorisoiva"}
                    :tallenna-jarjestys           {:fi "Tallenna järjestys"}
                    :jyemp                        {:fi "Jos Ylioppilastutkinto tai kansainvälinen ylioppilastutkinto, ei muiden tutkintojen liitepyyntöjä"}
                    :yo-amm-autom-hakukelpoisuus  {:fi "YO tai Ammatillinen tutkinto antaa automaattisen hakukelpoisuuden"}}
   })
