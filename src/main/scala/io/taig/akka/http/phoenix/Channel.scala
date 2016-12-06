package io.taig.akka.http.phoenix

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import io.circe.Json
import io.taig.akka.http.phoenix.message.Response

import scala.concurrent.{ Future, TimeoutException }
import scala.concurrent.duration._
import scala.language.postfixOps

case class Channel(
        topic: Topic,
        flow:  Flow[( Event, Json, Ref ), Response, _]
)(
        implicit
        as: ActorSystem,
        m:  Materializer
) {
    def send(
        event:   Event,
        payload: Json,
        timeout: FiniteDuration = 3 second
    ): Future[Result] = Channel.send( event, payload, timeout )( flow )

    def leave(): Future[Result] = send( Event.Leave, Json.Null )
}

object Channel {
    def join(
        topic:   Topic,
        payload: Json           = Json.Null,
        timeout: FiniteDuration = 3 second
    )(
        flow: Flow[( Event, Json, Ref ), Response, _]
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

    def send( event: Event, payload: Json, timeout: FiniteDuration = 1 second )(
        flow: Flow[( Event, Json, Ref ), Response, _]
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Result] = {
        import as.dispatcher

        val ref = Ref.unique()

        Source.single( event, payload, ref )
            .via( flow )
            .filter( ref == _.ref )
            .completionTimeout( timeout )
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