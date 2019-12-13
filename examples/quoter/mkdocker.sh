#!/bin/bash

if [[ -z "${DEMO_REGISTRY}" ]]; then
    echo "Env variable DEMO_REGISTRY must be set"
    exit 1
fi

mkdir tmp
cp ../../target/b3inject-1.0-SNAPSHOT-jar-with-dependencies.jar tmp
mvn clean install
docker build -t $DEMO_REGISTRY/quoter .
docker push $DEMO_REGISTRY/quoter
