#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

echo "./client.sh pathToOperations"
echo "---- UTILISATION ----"
echo "1er argument: pathToOperations (Ex: ./operations-588)"
echo "2e  argument: mode (0 (non securise) ou 1 (securise))"
echo ""
echo ""

serverNomName=$(head -n 1 ./ServiceDeNom/service_de_nom.txt)

java -cp "$basepath"/client.jar:"$basepath"/shared.jar \
-Djava.rmi.client.codebase=file:"$basepath"/shared.jar \
-Djava.security.policy="$basepath"/policy \
-Djava.rmi.client.hostname="$IPADDR" \
ca.polymtl.inf8480.tp2.client.Client $serverNomName $1 $2
