#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./serviceDeNom.sh

EndOfMessage

echo "ServiceDeNom server is ready !"
rm -rf ServiceDeNom
mkdir ServiceDeNom
echo $HOSTNAME > ServiceDeNom/service_de_nom.txt

java -cp "$basepath"/serviceDeNom.jar:"$basepath"/shared.jar \
-Djava.rmi.serviceDeNom.codebase=file:"$basepath"/shared.jar \
-Djava.security.policy="$basepath"/policy \
-Djava.rmi.serviceDeNom.hostname="$HOSTNAME" \
ca.polymtl.inf8480.tp2.serviceDeNom.ServiceDeNom
