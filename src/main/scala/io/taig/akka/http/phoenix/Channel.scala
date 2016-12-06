package io.taig.akka.http.phoenix

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import io.circe.Json
import io.taig.akka.http.phoenix.message.{ Inbound, Response }

import scala.concurrent.duration.Duration.Infinite
import scala.concurrent.{ Future, TimeoutException }
import scala.concurrent.duration._
import scala.language.postfixOps

case class Channel(
        topic: Topic,
        flow:  Flow[( Event, Json, Ref ), Inbound, _]
)(
        implicit
        as: ActorSystem,
        m:  Materializer
) {
    def send(
        event:   Event,
        payload: Json,
        timeout: Duration = Default.timeout
    ): Future[Result] = Channel.send( event, payload, timeout )( flow )

    def leave(): Future[Result] = send( Event.Leave, Json.Null )
}

object Channel {
    def join(
        topic:   Topic,
        payload: Json     = Json.Null,
        timeout: Duration = Default.timeout
    )(
        flow: Flow[( Event, Json, Ref ), Inbound, _]
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Either[Error, Channel]] = {
        import as.dispatcher

        send( Event.Join, payload, timeout )( flow ).map {
            case Result.Success( _ ) ⇒ Right( Channel( topic, flow ) )
            case error: Error        ⇒ Left( error )
        }
    }

    def send( event: Event, payload: Json, timeout: Duration = Default.timeout )(
        flow: Flow[( Event, Json, Ref ), Inbound, _]
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Result] = {
        import as.dispatcher

        val ref = Ref.unique()

        val source = Source.single( event, payload, ref )
            .via( flow )
            .collect { case response: Response ⇒ response }
            .filter( ref == _.ref )

        val withTimeout = timeout match {
            case timeout: FiniteDuration ⇒ source.completionTimeout( timeout )
            case _: Infinite             ⇒ source
        }

        withTimeout
            .toMat( Sink.headOption[Response] )( Keep.right )
            .run()
            .map {
                case Some( response ) if response.isOk ⇒
                    Result.Success( response )
                case Some( response ) ⇒ Result.Failure( response )
                case None             ⇒ Result.None
            }
            .recover {
                case _: TimeoutException ⇒ Result.None
            }
    }
}