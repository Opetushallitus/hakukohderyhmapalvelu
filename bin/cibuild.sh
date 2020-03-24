#!/usr/bin/env bash

LEIN=$(command -v lein)
if [[ "${LEIN}" == "" ]]; then
  LEIN="./bin/lein"
fi

lint() {
  $LEIN lint
}

run-all-tests() {
  lint
}

COMMAND=$1

case $COMMAND in
  "run-all-tests" )
    run-all-tests
    ;;
  "lint" )
    lint
    ;;
esac
