#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

echo "./loadBalancer.sh"
echo ""
echo ""

serverNomName=$(head -n 1 ./ServiceDeNom/service_de_nom.txt)

java -cp "$basepath"/loadBalancer.jar:"$basepath"/shared.jar \
  -Djava.rmi.loadBalancer.codebase=file:"$basepath"/shared.jar \
  -Djava.security.policy="$basepath"/policy \
  -Djava.rmi.loadBalancer.hostname="$HOSTNAME" \
  ca.polymtl.inf8480.tp2.loadBalancer.LoadBalancer $HOSTNAME $serverNomName
