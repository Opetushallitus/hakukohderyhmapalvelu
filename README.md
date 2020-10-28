# Hakukohderyhmäpalvelu

[![Build Status](https://travis-ci.org/Opetushallitus/hakukohderyhmapalvelu.svg?branch=master)](https://travis-ci.org/Opetushallitus/hakukohderyhmapalvelu)
![NPM Dependencies Status](https://david-dm.org/opetushallitus/hakukohderyhmapalvelu.svg)

* [Palvelun ajaminen paikallisesti](#palvelun-ajaminen-paikallisesti)
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

### Palvelun ajaminen paikallisesti

Tämä on suositeltu tapa ajaa palvelua paikallisesti.

1. Valmistele palvelun konfiguraatio
   * Mene aiemmin kloonaamaasi [local-environment](https://github.com/Opetushallitus/local-environment) -repositoryyn.
   * Mikäli et ole vielä kertaakaan valmistellut local-environment -ympäristöä, tee se repositoryn ohjeiden mukaan.
   * Generoi konfiguraatiotiedosto palvelua varten. Generointi lataa S3:sta untuva-, hahtuva- ja pallero -ympäristöjen salaisuudet ja generoi jokaista ympäristöä vastaavan hakukohderyhmäpalvelun konfiguraation.
   ```bash
   rm -f .opintopolku-local/.templates_compiled # Aja tämä komento, mikäli haluat pakottaa konfiguraation generoinnin
   make compile-templates
   ```
   * Konfiguraatiotiedostot löytyvät tämän repositoryn alta hakemistosta `oph-configurations/{hahtuva,pallero,untuva}/oph-configuration/hakukohderyhmapalvelu.config.edn`
2. Valmistele nginx -containerin konfiguraatio
   * Mikäli käytät Linuxia, etkä Mac OS -käyttöjärjestelmää, editoi tämän repositoryn `nginx/nginx.conf` -tiedostoa: korvaa kaikki `host.docker.internal` -osoitteet sillä IP-osoitteella, joka koneesi `docker0` -sovittimessa on käytössä. Tämän IP:n saat esimerkiksi komennolla `/sbin/ifconfig docker0` selville.
3. Asenna NPM-riippuvuudet
   * Käytä Node.js v14:ää. Jos sinulla on NVM, voit kirjoittaa tämän repositoryn juuressa
   ```bash
   nvm use
   npm install
   ```
4. Käynnistä nginx
   * Tämän repositoryn juuressa
   ```bash
   docker-compose up
   ```
5. Käynnistä taustajärjestelmä
   * Tämän repositoryn juuressa
   ```bash
   TIMBRE_NS_BLACKLIST='["clj-timbre-auditlog.audit-log"]' CONFIG=/polku/local-environment-repositoryn-juureen/oph-configurations/pallero/oph-configuration/hakukohderyhmapalvelu.config.edn lein server:dev
   ```
6. Käynnistä selainohjelman kehityspalvelin
   * Tämän repositoryn juuressa
   ```bash
   lein frontend:dev
   ```
7. Palvelu on käytettävissä osoitteessa `http://localhost:9030/hakukohderyhmapalvelu`

### Palvelun ajaminen paikallisesti local-environment -ympäristön avulla

Palvelun ajaminen on helppoa, mutta valitettavasti erityisesti Mac OS -käyttöjärjestelmällä tämän ratkaisun vuoksi hidasta. *Tämä vaihtoehto ei ole suositeltava*

Käynnistä Hakukohderyhmäpalvelu sanomalla `local-environment` -repositoryn juuressa:
 
```sh
make start-hakukohderyhmapalvelu
```

Muita tuettuja komentoja: 

```sh
make {restart,kill}-hakukohderyhmapalvelu
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

### E2E-testit

Käynnistä `local-environment` -repositoryssä E2E:tä varten dedikoidut instanssit palvelusta:

```sh
make start-hakukohderyhmapalvelu-e2e
```

Jos haluat samalla komennolla käynnistää rinnakain toimivat instanssit palvelusta normaalia käyttöä ja E2E-testejä varten, tee se seuraavalla komennolla:

```sh
make start-hakukohderyhmapalvelu-all
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
