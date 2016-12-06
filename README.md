# akka HTTP Phoenix

[![CircleCI](https://circleci.com/gh/Taig/akka-http-phoenix/tree/master.svg?style=shield)](https://circleci.com/gh/Taig/akka-http-phoenix/tree/master)
[![codecov](https://codecov.io/github/Taig/akka-http-phoenix/coverage.svg?branch=master)](https://codecov.io/github/Taig/akka-http-phoenix?branch=master)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/taig/akka-http-phoenix/master/LICENSE)

> A simple implementation of (most of) the [Phoenix Channels][1] protocol with [akka HTTP][2] for WebSocket communication and [circe][3] for JSON

## Installation

_akka HTTP Phoenix_ is available for Scala `2.11` and `2.12`

```scala
libraryDependencies += "io.taig" %% "akka-http-phoenix" % "1.0.0-SNAPSHOT"
```

## Usage

```scala
import akka.actor.ActorSystem
// import akka.actor.ActorSystem

import akka.http.scaladsl.model.ws.WebSocketRequest
// import akka.http.scaladsl.model.ws.WebSocketRequest

import akka.stream.ActorMaterializer
// import akka.stream.ActorMaterializer

import io.circe.syntax._
// import io.circe.syntax._

import io.taig.akka.http.phoenix._
// import io.taig.akka.http.phoenix._

import scala.concurrent._
// import scala.concurrent._

import scala.concurrent.duration._
// import scala.concurrent.duration._

import scala.language.postfixOps
// import scala.language.postfixOps

implicit val system = ActorSystem()
// system: akka.actor.ActorSystem = akka://default

implicit val materializer = ActorMaterializer()
// materializer: akka.stream.ActorMaterializer = ActorMaterializerImpl(akka://default,ActorMaterializerSettings(4,16,,<function1>,StreamSubscriptionTimeoutSettings(CancelTermination,5000 milliseconds),false,1000,1000,false,true),akka.dispatch.Dispatchers@40e48965,Actor[akka://default/user/StreamSupervisor-0#1336150488],false,akka.stream.impl.SeqActorNameImpl@d399921)

val request = WebSocketRequest( "ws://localhost:4000/socket/websocket" )
// request: akka.http.scaladsl.model.ws.WebSocketRequest = WebSocketRequest(ws://localhost:4000/socket/websocket,List(),None)

val topic = Topic( "echo", "foobar" )
// topic: io.taig.akka.http.phoenix.Topic = Topic(echo:foobar)

import system.dispatcher
// import system.dispatcher

val future = for {
    // Open socket connection to the Phoenix server
    phoenix ← Phoenix( request )
    // Join a channel
    Right( channel ) ← phoenix.join( topic )
    // Send a message and wait for response
    Result.Success( response ) ← channel.send( Event( "echo" ), "foobar".asJson )
    // Shutdown socket connection from the client side
    _ = phoenix.close()
} yield response
// future: scala.concurrent.Future[io.taig.akka.http.phoenix.message.Response] = Future(<not completed>)

try {
    Await.result( future, 3 seconds )
} finally {
    Await.result( system.terminate(), 3 seconds )
}
// res0: io.taig.akka.http.phoenix.message.Response =
// Response(Topic(echo:foobar),Event(phx_reply),Some(Payload(Status(ok),{
//   "payload" : "foobar"
// })),Ref(1))
```

## Test-Suite & Documentation

Code executed by the unit tests or documentation generation via [tut][4] required the [phoenix_echo][5] app to be running in the background. Use the provided `Dockerfile` to create an image with preinstalled requirements.

[1]: http://www.phoenixframework.org/docs/channels
[2]: http://doc.akka.io/docs/akka-http/current/scala.html
[3]: https://github.com/circe/circe
[4]: https://github.com/tpolecat/tut
[5]: https://github.com/PragTob/phoenix_echo
