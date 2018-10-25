#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

echo "./serverCalcul.sh capacity maliciousness"
echo "---- UTILISATION ----"
echo "1er argument: capacity: Capacite du serveur (int)"
echo "2e  argument: maliciousness: Entier entre 0 et 100"
echo ""
echo ""

serverNomName=$(head -n 1 ./ServiceDeNom/service_de_nom.txt)

java -cp "$basepath"/serverCalcul.jar:"$basepath"/shared.jar \
  -Djava.rmi.serverCalcul.codebase=file:"$basepath"/shared.jar \
  -Djava.security.policy="$basepath"/policy \
  -Djava.rmi.serverCalcul.hostname="$HOSTNAME" \
  ca.polymtl.inf8480.tp2.serverCalcul.ServerCalcul $HOSTNAME $1 $serverNomName $2
