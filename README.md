# Hakukohderyhmäpalvelu

[![Build Status](https://travis-ci.org/Opetushallitus/hakukohderyhmapalvelu.svg?branch=master)](https://travis-ci.org/Opetushallitus/hakukohderyhmapalvelu)
[![Dependencies Status](https://jarkeeper.com/Opetushallitus/hakukohderyhmapalvelu/status.svg)](https://jarkeeper.com/Opetushallitus/hakukohderyhmapalvelu)


* [Palvelun ajaminen paikallisesti](#palvelun-ajaminen-paikallisesti)
* [Testien ajaminen](#testien-ajaminen)
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

Käynnistä Hakukohderyhmäpalvelu sanomalla `local-environment` -repositoryn juuressa:
 
```sh
make start-hakukohderyhmapalvelu
```

Muita tuettuja komentoja: 

```sh
make {restart,kill}-hakukohderyhmapalvelu
```

## Testien ajaminen

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
