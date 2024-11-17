#!/bin/sh

docker network create connect_network && \
docker compose -f init-docker-compose.yml -f docker-compose.yml up -d