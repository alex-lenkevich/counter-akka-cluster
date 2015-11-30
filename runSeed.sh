#!/usr/bin/env bash
docker stop $1
docker rm $1
docker run --name $1 akka-docker-counter:2.4.1 --seed -Dakka.cluster.node.number=$2 -Dakka.cluster.node.count=$3 -Dakka.cluster.node.secret=$4 -Dakka.cluster.min-nr-of-members=$3


