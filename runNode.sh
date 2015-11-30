#!/usr/bin/env bash
docker stop node-$2
docker rm node-$2
docker run --name node-$2 akka-docker-counter:2.4.1 $1 -Dakka.cluster.node.number=$2 -Dakka.cluster.node.count=$3 -Dakka.cluster.node.secret=$4
