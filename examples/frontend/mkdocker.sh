#!/bin/bash

if [[ -z "${DEMO_REGISTRY}" ]]; then
    echo "Env variable DEMO_REGISTRY must be set"
    exit 1
fi

mkdir tmp
cp ../../target/b3inject-1.0.1-jar-with-dependencies.jar tmp
mvn clean install
docker build -t $DEMO_REGISTRY/frontend .
docker push $DEMO_REGISTRY/frontend
