#!/bin/bash

# Docker entrypoint for tut documentation generation
#
# docker run --entrypoint="./tut.sh" -v "$PWD:/akka-http-phoenix/" --rm taig/akka-http-phoenix:latest

cd ~/phoenix_echo/
elixir --detached -S mix do phoenix.server
cd -

sbt "set skip in update := true" tut