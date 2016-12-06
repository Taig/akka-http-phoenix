#!/bin/bash

# Docker entrypoint with open sbt shell and phoenix_echo running in background
#
# docker run --entrypoint="./develop.sh" -v "$PWD:/akka-http-phoenix/" --rm -it taig/akka-http-phoenix:latest

set -e # halt on errors

cd ~/phoenix_echo/
elixir --detached -S mix do phoenix.server
cd -

sbt