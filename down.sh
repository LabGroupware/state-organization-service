#!/bin/sh

docker compose -f init-docker-compose.yml -f docker-compose.yml down -v && \
docker network rm connect_network