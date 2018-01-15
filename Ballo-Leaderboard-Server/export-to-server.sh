#!/bin/bash
export GOOS=linux
go build -o ballo -v
docker build -t fru1tstand/ballo .
go clean
docker push fru1tstand/ballo
