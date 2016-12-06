#!/bin/bash

# Docker entrypoint for test suite execution
#
# docker run -e CODECOV_TOKEN=$CODECOV_TOKEN --entrypoint="./test.sh" -v "$PWD:/akka-http-phoenix/" taig/akka-http-phoenix:latest

set -e # halt on errors

cd ~/phoenix_echo/
elixir --detached -S mix do phoenix.server

cd /akka-http-phoenix/
sbt ";coverage;+test;+tut;coverageReport;coverageAggregate"

if [ -n "$CODECOV_TOKEN" ]; then
    codecov
fi