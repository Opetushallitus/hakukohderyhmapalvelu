(ns hakukohderyhmapalvelu.i18n.translations)

(def local-translations
  {:yleiset        {:ei-valittavia-kohteita {:fi "Valittavia kohteita ei löytynyt"
                                             :sv "Inga valbara ansökningsmål hittades"}
                    :vahvista-poisto        {:fi "Vahvista poisto"
                                             :sv "Bekräfta borttagning"}
                    :peruuta                {:fi "Peruuta"
                                             :sv "Avbryt"}
                    :hakulomake             {:fi "Hakulomake"
                                             :sv "Ansökningsblanketten"}
                    :tallenna               {:fi "Tallenna"
                                             :sv "Spara"}
                    :hakuajat               {:fi "Hakuajat"
                                             :sv "Ansökningstider"}
                    :muokkaa-lomaketta      {:fi "Muokkaa lomaketta"
                                             :sv "Redigera ansökningsblanketten"}
                    :muokkaa-hakua          {:fi "Muokkaa hakua"
                                             :sv "Redigera ansökan"}
                    :pakolliset-kentat      {:fi "* merkityt kentät ovat pakollisia"
                                             :sv "* markerade fält är obligatoriska"}
                    :http-virhe             {:fi "Tietojen haku epäonnistui"
                                             :sv "Hämtning av uppgifter misslyckades"}
                    :arkistoitu             {:fi "Arkistoitu"
                                             :sv "Arkiverad"}
                    :http-403               {:fi "Ei oikeuksia"
                                             :sv "Inga rättigheter"}}
   :haun-asetukset {:valintatulokset-valmiina-viimeistaan                        {:fi "Valintatulokset valmiina viimeistään"
                                                                                  :sv "Antagningsresultaten klara senast"}
                    :useita-hakemuksia                                           {:fi "Vain yksi hakemus -rajoitus"
                                                                                  :sv "Endast en ansökan tillåten"}
                    :haun-asetukset                                              {:fi "Haun asetukset"
                                                                                  :sv "Ansökningsinställningar"}
                    :synteettiset-hakemukset                                     {:fi "Haussa käytetään synteettisiä hakemuksia"
                                                                                  :sv "Syntetiska ansökningar används"}
                    :synteettisen-hakemuksen-lomakeavain                         {:fi "Synteettisten hakemusten lomakeavain"
                                                                                  :sv "Ansökningsnyckel för syntetisk ansökan"}
                    :hakukohteiden-enimmaismaara                                 {:fi "Hakutoiveiden enimmäismäärä"
                                                                                  :sv "Maximalt antal ansökningsönskemål"}
                    :hakukierros-paattyy                                         {:fi "Hakukierros päättyy"
                                                                                  :sv "Ansökningsomgången avslutas"}
                    :varasijataytto-paattyy                                      {:fi "Varasijatäyttö päättyy"
                                                                                  :sv "Reservantagning avslutas"}
                    :hakukohteiden-maara-rajoitettu                              {:fi "Hakutoiveiden määrä rajoitettu"
                                                                                  :sv "Antalet ansökningsönskemål är begränsat"}
                    :jarjestetyt-hakutoiveet                                     {:fi "Hakutoiveiden priorisointi"
                                                                                  :sv "Prioritering av ansökningsönskemål"}
                    :varasijasaannot-astuvat-voimaan                             {:fi "Varasijasäännöt astuvat voimaan"
                                                                                  :sv "Reservplatsregler träder i kraft"}
                    :paikan-vastaanotto-paattyy                                  {:fi "Hakijan paikan vastaanotto päättyy"
                                                                                  :sv "Mottagande av studieplats avslutas"}
                    :hakijakohtainen-paikan-vastaanottoaika                      {:fi "Hakijakohtainen paikan vastaanottoaika (vrk)"
                                                                                  :sv "Sökandespecifik tid för att ta emot studieplatsen (dygn)"}
                    :syota-kellonaika                                            {:fi "Syötä tähän kenttään päivämäärästä ja kellonajasta muodostuvan arvon kellonaika"
                                                                                  :sv "Ange klockslag som del av datum- och tidsvärdet"}
                    :syota-paivamaara                                            {:fi "Syötä tähän kenttään päivämäärästä ja kellonajasta muodostuvan arvon päivämäärä"
                                                                                  :sv "Ange datum som del av datum- och tidsvärdet"}
                    :aikavali                                                    {:fi "Aikaväli"
                                                                                  :sv "Tidsintervall"}
                    :aikavali-alku                                               {:fi "Aikavälin alkuhetki"
                                                                                  :sv "Start av tidsintervall"}
                    :aikavali-loppu                                              {:fi "Aikavälin loppuhetki"
                                                                                  :sv "Slut av tidsintervall"}
                    :sijoittelu                                                  {:fi "Haussa käytetään sijoittelua"
                                                                                  :sv "Placering används"}
                    :valintatulosten-julkaiseminen-hakijoille                    {:fi "Valintatulosten julkaiseminen hakijoille (aikaväli)"
                                                                                  :sv "Publicering av antagningsresultat för sökande (tidsintervall)"}
                    :liitteiden-muokkauksen-takaraja                             {:fi "Liitteiden muokkauksen takaraja on"
                                                                                  :sv "Sista tidpunkt för redigering av bilagor är"}
                    :liitteiden-muokkauksen-takaraja-hakukohtainen               {:fi "Hakukohtainen"
                                                                                  :sv "Ansökningsspecifik"}
                    :liitteiden-muokkauksen-takaraja-hakemuskohtainen            {:fi "Hakemuskohtainen"
                                                                                  :sv "Ansökningsspecifik"}
                    :liitteiden-muokkauksen-takaraja-vuorokausina                {:fi "Takaraja vuorokausina ja kellonaika"
                                                                                  :sv "Gräns i dygn och klockslag"}
                    :ilmoittautuminen-paattyy                                    {:fi "Ilmoittautuminen päättyy"
                                                                                  :sv "Anmälan avslutas"}
                    :automaattinen-hakukelpoisuus-paattyy                        {:fi "Automaattinen hakukelpoisuus päättyy"
                                                                                  :sv "Automatisk behörighet upphör"}
                    :harkinnanvaraisen-valinnan-paatosten-tallennus-paattyy      {:fi "Harkinnanvaraisen valinnan päätösten tallennus päättyy"
                                                                                  :sv "Registrering av beslut för antagning enligt prövning avslutas"}
                    :oppilaitosten-virkailijoiden-valintapalvelun-kaytto-estetty {:fi "Oppilaitosten virkailijoiden valintapalvelun käyttö estetty"
                                                                                  :sv "Användning av antagningstjänsten spärrad för läroanstalters personal"}
                    :valintaesityksen-hyvaksyminen                               {:fi "Valintaesityksen hyväksyminen"
                                                                                  :sv "Godkännande av antagningsförslag"}
                    :koetulosten-tallentaminen                                   {:fi "Koetulosten tallentaminen"
                                                                                  :sv "Registrering av provresultat"}
                    :suoritusten-vahvistuspaiva                                  {:fi "Suoritusten vahvistuspäivä"
                                                                                  :sv "Bekräftelsedatum för prestationer"}
                    :valintalaskentapaiva                                        {:fi "Valintalaskentapäivä"
                                                                                  :sv "Datum för antagningsräkning"}
                    :muutoksia-ei-viela-tallennettu                              {:fi "Muutoksia ei vielä tallennettu"
                                                                                  :sv "Ändringar är ännu inte sparade"}
                    :tayta-pakolliset                                            {:fi "Täytä kaikki pakolliset kohdat"
                                                                                  :sv "Fyll i alla obligatoriska fält"}
                    :tallenna                                                    {:fi "TALLENNA"
                                                                                  :sv "SPARA"}}
   :hakukohderyhma {:haku                         {:fi "Haku"
                                                   :sv "Sök"}
                    :lisarajain-harkinnanvaraiset {:fi "Hakukohteella harkinnanvarainen valinta"
                                                   :sv "Ansökningsmålet använder antagning enligt prövning"}
                    :lisarajain-kaksoistutkinto   {:fi "Hakukohteella mahdollisuus kaksoistutkintoon"
                                                   :sv "Möjlighet till dubbelexamen"}
                    :lisarajain-koulutustyypit    {:fi "Koulutustyypit"
                                                   :sv "Utbildningstyper"}
                    :lisarajain-urheilu           {:fi "Urheiluoppilaitoksien hakukohteet"
                                                   :sv "Idrottsläroanstalternas ansökningsmål"}
                    :nimi-tai-organisaatio        {:fi "Nimi tai organisaatio"
                                                   :sv "Namn eller organisation"}
                    :poista-valinnat              {:fi "Poista valinnat"
                                                   :sv "Radera val"}
                    :valitse-kaikki               {:fi "Valitse kaikki"
                                                   :sv "Välj alla"}
                    :luo-uusi-ryhma               {:fi "Luo uusi ryhmä"
                                                   :sv "Skapa ny grupp"}
                    :hakukohteet                  {:fi "Hakukohteet"
                                                   :sv "Ansökningsmål"}
                    :nayta-myos-paattyneet        {:fi "Näytä myös päättyneet"
                                                   :sv "Visa även avslutade"}
                    :lisarajaimet                 {:fi "Lisäsuodattimet"
                                                   :sv "Ytterligare filter"}
                    :hakukohderyhma-nimi          {:fi "Hakukohderyhmän nimi"
                                                   :sv "Ansökningsmålsgruppens namn"}
                    :haun-nimi                    {:fi "Haun nimi"
                                                   :sv "Ansökningens namn"}
                    :hakukohderyhma-kaytossa      {:fi "Hakukohderyhmä on käytössä hakulomakkeella ja sitä ei voi poistaa."
                                                   :sv "Ansökningsmålsgruppen används i ansökningsblanketten och kan inte tas bort"}
                    :liita-ryhmaan                {:fi "Liitä ryhmään"
                                                   :sv "Lägg till i grupp"}
                    :sora-hakukohteet             {:fi "SORA -hakukohteet"
                                                   :sv "SORA-ansökningsmål"}
                    :muokkaa-ryhmaa               {:fi "Muokkaa ryhmää"
                                                   :sv "Redigera grupp"}
                    :poista-ryhmasta              {:fi "Poista ryhmästä"
                                                   :sv "Ta bort från grupp"}
                    :max-hakukohteet              {:fi "Ryhmän hakukohteita valittavissa enintään"
                                                   :sv "Max antal valbara ansökningsmål i gruppen"}
                    :rajaava                      {:fi "Rajaava"
                                                   :sv "Begränsande"}
                    :priorisoiva                  {:fi "Priorisoiva"
                                                   :sv "Prioriterande"}
                    :tallenna-jarjestys           {:fi "Tallenna järjestys"
                                                   :sv "Spara ordning"}
                    :jyemp                        {:fi "Jos Ylioppilastutkinto tai kansainvälinen ylioppilastutkinto, ei muiden tutkintojen liitepyyntöjä"
                                                   :sv "Om studentexamen eller internationell motsvarighet, inga bilagekrav för andra examina"}
                    :yo-amm-autom-hakukelpoisuus  {:fi "YO tai Ammatillinen tutkinto antaa automaattisen hakukelpoisuuden"
                                                   :sv "Studentexamen eller yrkesexamen ger automatisk behörighet"}}
   })
