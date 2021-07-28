# Hakukohderyhmäpalvelu

[![Build Status](https://travis-ci.org/Opetushallitus/hakukohderyhmapalvelu.svg?branch=master)](https://travis-ci.org/Opetushallitus/hakukohderyhmapalvelu)
![NPM Dependencies Status](https://david-dm.org/opetushallitus/hakukohderyhmapalvelu.svg)

* [Palvelun ajaminen paikallisesti](#palvelun-ajaminen-paikallisesti)
  * [Vain kerran tehtävät työvaiheet](#vain-kerran-tehtävät-työvaiheet)
  * [Käyttö](#käyttö)
    * [Palvelun käynnistäminen](#palvelun-käynnistäminen)
    * [Palvelun pysäyttäminen](#palvelun-pysäyttäminen)
    * [Palvelun logien tarkastelu](#palvelun-logien-tarkastelu)
    * [Palvelun komponenttien tilan tarkastelu](#palvelun-komponenttien-tilan-tarkastelu)
* [Testien ajaminen](#testien-ajaminen)
  * [Lint](#lint)
    * [Clojure(Script) -tiedostojen lint](#clojurescript--tiedostojen-lint)
    * [JavaScript -tiedostojen lint](#javascript--tiedostojen-lint)
  * [E2E-testit](#e2e-testit)
    * [Testien ajaminen Cypress-käyttöliittymän kautta](#testien-ajaminen-cypress-käyttöliittymän-kautta)
    * [Testien ajaminen headless -moodissa](#testien-ajaminen-headless--moodissa)
* [REPL-yhteys palvelimeen ja selaimeen](#repl-yhteys-palvelimeen-ja-selaimeen)
* [Palvelun paikalliset osoitteet](#palvelun-paikalliset-osoitteet)
* [Tuotantokäyttö](#tuotantokäyttö)
  * [Palvelun uberjar -tiedoston luonti tuotantokäyttöä varten](#palvelun-uberjar--tiedoston-luonti-tuotantok%C3%A4ytt%C3%B6%C3%A4-varten)
  * [Palvelun ajaminen uberjar -tiedostosta](#palvelun-ajaminen-uberjar--tiedostosta)

## Palvelun ajaminen paikallisesti

Kloonaa ja valmistele omien ohjeiden mukaan käyttökuntoon [local-environment](https://github.com/Opetushallitus/local-environment) -ympäristö.

### Vain kerran tehtävät työvaiheet

1. Valmistele palvelun konfiguraatio
   * Mene aiemmin kloonaamaasi [local-environment](https://github.com/Opetushallitus/local-environment) -repositoryyn.
   * Mikäli et ole vielä kertaakaan valmistellut local-environment -ympäristöä, tee se repositoryn ohjeiden mukaan.
   * Generoi konfiguraatiotiedosto palvelua varten. Generointi lataa S3:sta untuva-, hahtuva- ja pallero -ympäristöjen salaisuudet ja generoi jokaista ympäristöä vastaavan hakukohderyhmäpalvelun konfiguraation. Tee siis tämä local-environment -repoistoryssä.
   ```bash
   rm -f .opintopolku-local/.templates_compiled # Aja tämä komento, mikäli haluat pakottaa konfiguraation generoinnin
   make compile-templates
   ```
   * Konfiguraatiotiedostot löytyvät nyt local-environment -repositoryn alta hakemistosta `oph-configurations/{hahtuva,pallero,untuva}/oph-configuration/hakukohderyhmapalvelu.config.edn`
2. Valmistele nginx -containerin konfiguraatio
   * Mikäli käytät Mac OS -käyttöjärjestelmää, sinun ei tarvitse tehdä mitään.
   * Mikäli käytät Linuxia, etkä Mac OS -käyttöjärjestelmää, editoi tämän repositoryn `nginx/nginx.conf` -tiedostoa: korvaa kaikki `host.docker.internal` -osoitteet sillä IP-osoitteella, joka koneesi `docker0` -sovittimessa on käytössä. Tämän IP:n saat esimerkiksi komennolla `/sbin/ifconfig docker0` selville.
3. Konfiguroi SSH-client
   * Tarvitset SSH-tunnelia varten SSH-konfiguraatioosi tiedon useista porttiohjauksista.
   * Kun olet ensin alustanut local-environment -ympäristön kohdan 1 mukaan, voit yksinkertaisimmillaan lisätä seuraavan rivin `~./ssh/config` -tiedostosi ensimmäiseksi riviksi:
   ```
   Include /polku/local-environment-repositoryysi/docker/ssh/config
   ```
   * Mikäli et halua määrittää kyseistä `Include` -direktiiviä, voit tarjota kyseiset porttiohjauskonfiguraatiot SSH-clientillesi jotenkin toisin.
   

### Käyttö

Tämä on suositeltu tapa ajaa palvelua paikallisesti. Tässä ohjeessa oletetaan, että local-environment -repository löytyy hakukohderyhmäpalvelu -hakemiston vierestä, samasta hakemistosta.

Käynnistetty palvelu on käytettävissä osoitteessa (http://localhost:9030/hakukohderyhmapalvelu).

Kun ajat palvelua, käynnistä aina ensin SSH-yhteys käyttämääsi ympäristöön. Oletuksena se on `pallero`:

```
ssh bastion.pallero
```

#### Palvelun käynnistäminen

```bash
make start
```

#### Palvelun pysäyttäminen

```bash
make kill
```

#### Palvelun logien tarkastelu

```bash
make logs
```


#### Palvelun komponenttien tilan tarkastelu

```bash
make status
```

#### Palvelun ajaminen lokaalisti lokaalia kantaa käyttäen
Esimerkiksi hyödyllinen tehtäessä tietokantaan kohdistuvaa kehitystä. Ei vaadi SSH-yhteyttä. 

```bash
export CONFIG='../local-environment/oph-configurations/local/hakukohderyhmapalvelu.config.edn'
make start-local    # Palvelun käynnistäminen
make reload         # Palvelun uudelleenlataaminen, ei uudellenkäynnistä docker-kontteja
make restart-local  # Palvelun uudelleenkäynnistys, uudelleenkäynnistää docker-kontit
```

## Testien ajaminen

### Lint

#### Clojure(Script) -tiedostojen lint

```sh
npm run lint:clj
```

#### JavaScript -tiedostojen lint

```
npm run lint:js
```

#### JavaScript -tiedostojen formatointi
```
npm run format:js
```

### Integraatiotestit

Käynnistä ensin bäkkäri komennolla

```
make start-cypress
```

Aja sen jälkeen lein testit käyttäen lokaalia konfiguraatiotiedostoa:

```
CONFIG=oph-configuration/config.cypress.local-environment.edn lein test
```

### E2E-testit

1. Mikäli et vielä ole kertaakaan valmistellut local-environment -ympäristöä, suorita ensin kohdan [vain kerran tehtävät työvaiheet](#vain-kerran-tehtävät-työvaiheet) mukaiset toimenpiteet.

Jotta voit ajaa testejä, käynnistä Cypress-testejä varten dedikoitu instanssi palvelusta. Instanssi tarvitsee käynnistää vain kerran, vaikka ajat testejä monta kertaa. HUOM: tämä komento uudelleenkäynnistää frontend-käännöksetn.

```bash
make start-cypress
```

Voit sammuttaa palvelun komennoilla:

```
make kill # Sammuttaa sekä Cypress -instanssit että normaalia kehitystä varten tarkoitetut instanssit palvelusta.
make kill-cypress # Sammuttaa ainoastaan Cypress -instanssit palvelusta
```

#### Testien ajaminen Cypress-käyttöliittymän kautta

Avaa Cypress-käyttöliittymän josta voi käynnistää testit ja jättää taustalle. Testit ajetaan automaattisesti uudestaan koodimuutosten yhteydessä.

```sh
npm run cypress:open
```

#### Testien ajaminen headless -moodissa

```sh
npm run cypress:run:local-environment
```

## REPL-yhteys palvelimeen ja selaimeen

REPL-yhteys palvelimelle avautuu sanomalla komentorivillä

```sh
lein repl :connect localhost:9031
```

REPL-yhteys selaimeen avautuu sanomalla em. REPL-yhteyden sisällä. Muistathan ensin avata selaimellasi palvelun (ks. osoite alta).

```clj
(shadow.cljs.devtools.api/nrepl-select :hakukohderyhmapalvelu)
```

## Palvelun paikalliset osoitteet

* Palvelun osoite: (http://localhost:9032/hakukohderyhmapalvelu)
* Palvelun Shadow CLJS -palvelimen osoite (http://localhost:9630)

## Tuotantokäyttö

### Palvelun uberjar -tiedoston luonti tuotantokäyttöä varten

Seuraava komento luo tämän repositoryn `target` -hakemistoon tiedoston `hakukohderyhmapalvelu.jar`.

```sh
lein with-profile prod uberjar
```

### Palvelun ajaminen uberjar -tiedostosta

```sh
CONFIG=/polku/palvelun/config-tiedostoon java -jar hakukohderyhmapalvelu.jar
```
