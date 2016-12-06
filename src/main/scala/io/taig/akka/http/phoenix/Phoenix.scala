package io.taig.akka.http.phoenix

import akka.NotUsed
import akka.actor.{ ActorSystem, Cancellable }
import akka.http.scaladsl._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws._
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, MergeHub, Source }
import akka.stream.{ KillSwitches, Materializer, UniqueKillSwitch }
import cats.syntax.either._
import io.circe.Json
import io.circe.parser._
import io.circe.syntax._
import io.taig.akka.http.phoenix.message.{ Inbound, Push, Request, Response }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

case class Phoenix(
        flow:       Flow[Request, Inbound, NotUsed],
        killswitch: UniqueKillSwitch
) {
    def join(
        topic:   Topic,
        payload: Json     = Json.Null,
        timeout: Duration = Default.timeout
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Either[Error, Channel]] = {
        val flow = Flow[( Event, Json, Ref )].map {
            case ( event, payload, ref ) ⇒
                Request( topic, event, payload, ref )
        }.via( this.flow ).filter( topic isSubscribedTo _.topic )

        Channel.join( topic, payload, timeout )( flow )
    }

    def close(): Unit = killswitch.shutdown()
}

object Phoenix {
    def fromFlow(
        websocket: Flow[Message, Message, Future[WebSocketUpgradeResponse]],
        heartbeat: Option[FiniteDuration]                                   = Default.heartbeat
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Phoenix] = {
        import as.dispatcher

        val source = heartbeat match {
            case Some( heartbeat ) ⇒
                MergeHub
                    .source[Request]
                    .merge( Phoenix.heartbeat( heartbeat ) )
                    .map { request ⇒
                        TextMessage( request.asJson.noSpaces )
                    }
            case None ⇒
                MergeHub
                    .source[Request]
                    .map { request ⇒
                        TextMessage( request.asJson.noSpaces )
                    }
        }

        val sink = BroadcastHub.sink[Inbound].contramap[Message] {
            case TextMessage.Strict( message ) ⇒
                ( decode[Response]( message ): Either[io.circe.Error, Inbound] )
                    .orElse( decode[Push]( message ) )
                    .valueOr( throw _ )
            case _ ⇒ throw new RuntimeException( "ohne moos nix los" )
        }

        val ( ( ( in, killswitch ), upgrade ), out ) = source
            .viaMat( KillSwitches.single )( Keep.both )
            .viaMat( websocket )( Keep.both )
            .toMat( sink )( Keep.both )
            .run()

        upgrade.map( _.response.status ).map {
            case StatusCodes.SwitchingProtocols ⇒
                Phoenix( Flow.fromSinkAndSource( in, out ), killswitch )
            case _ ⇒ throw new RuntimeException( "ohne moos nix los" )
        }
    }

    def apply(
        request:   WebSocketRequest,
        heartbeat: Option[FiniteDuration] = Some( 7 seconds )
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Phoenix] = {
        val websocket = Http().webSocketClientFlow( request )
        fromFlow( websocket, heartbeat )
    }

    def heartbeat( delay: FiniteDuration ): Source[Request, Cancellable] = {
        def request = Request(
            Topic( "phoenix" ),
            Event( "heartbeat" ),
            Json.Null,
            _: Ref
        )

        Source.tick( delay, delay, request ).map( _.apply( Ref.unique() ) )
    }
}