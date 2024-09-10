#!/usr/bin/env bash

LEIN=$(command -v lein)
if [[ "${LEIN}" == "" ]]; then
  LEIN="./bin/lein"
fi

tsc() {
  npm run tsc:type-check
}

lint() {
  npm run lint:clj \
    && npm run lint:js
}

test-e2e() {
  npm run cypress:run:travis
}

test-lein() {
  CONFIG=oph-configuration/config.cypress.travis.edn \
    lein test
}

run-all-tests() {
  lint \
    && test-e2e
}

create-uberjar() {
  lein clean \
    && lein with-profile prod:ovara uberjar
}

run-mocked-hakukohderyhmapalvelu() {
  docker kill hakukohderyhmapalvelu-e2e-db || true && docker rm -f hakukohderyhmapalvelu-e2e-db || true 2>&1 > /dev/null
  docker run --name hakukohderyhmapalvelu-e2e-db -d -e POSTGRES_PASSWORD=postgres_password -e POSTGRES_USER=postgres_user -e POSTGRES_DB=hakukohderyhmapalvelu -p 5432:5432 postgres:12-alpine
  CONFIG=oph-configuration/config.cypress.travis.edn java -jar target/hakukohderyhmapalvelu.jar &
  ./bin/wait-for.sh localhost:19033 -t 30
}

run-all-tests-and-create-uberjar() {
  tsc \
    && lint \
    && create-uberjar \
    && run-mocked-hakukohderyhmapalvelu \
    && test-e2e \
    && test-lein
}

COMMAND=$1

case $COMMAND in
  "run-all-tests" )
    run-all-tests
    ;;
  "run-all-tests-and-create-uberjar" )
    run-all-tests-and-create-uberjar
    ;;
  "lint" )
    lint
    ;;
  "test-e2e" )
    test-e2e
    ;;
  "run-mocked-hakukohderyhmapalvelu" )
    run-mocked-hakukohderyhmapalvelu
    ;;
esac
