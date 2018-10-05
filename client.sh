#!/bin/bash

pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

cat << EndOfMessage
HELP: 
./client.sh ip_address
	- ip_address: (OPTIONAL) L'addresse ip du serveur.
	  Si l'arguement est non fourni, on conisdère que le serveur est local (ip_address = 127.0.0.1)

EndOfMessage

java -cp "$basepath"/client.jar:"$basepath"/shared.jar \
  -Djava.rmi.client.codebase=file:"$basepath"/shared.jar \
  -Djava.security.policy="$basepath"/policy \
  -Djava.rmi.client.hostname="$IPADDR" \
  client.Client $*
