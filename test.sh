#!/bin/bash

# Docker entrypoint for test suite execution
#
# docker run -e CODECOV_TOKEN=$CODECOV_TOKEN --entrypoint="./test.sh" -v "$PWD:/akka-http-phoenix/" --rm taig/akka-http-phoenix:latest

cd ~/phoenix_echo/
elixir --detached -S mix do phoenix.server
cd -

sbt ";coverage;+test;+tut;coverageReport;coverageAggregate"

if [ -n "$CODECOV_TOKEN" ]; then
    codecov
fi