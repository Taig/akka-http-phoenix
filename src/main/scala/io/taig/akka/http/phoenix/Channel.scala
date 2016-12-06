package io.taig.akka.http.phoenix

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import io.circe.Json
import io.taig.akka.http.phoenix.message.Response

import scala.concurrent.Future

case class Channel(
        topic: Topic,
        flow:  Flow[( Event, Json, Ref ), Response, _]
)(
        implicit
        as: ActorSystem,
        m:  Materializer
) {
    def send( event: Event, payload: Json ): Future[Result] = {
        Channel.send( event, payload )( flow )
    }

    def leave(): Future[Result] = send( Event.Leave, Json.Null )
}

object Channel {
    def join(
        topic:   Topic,
        payload: Json  = Json.Null
    )(
        flow: Flow[( Event, Json, Ref ), Response, _]
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Either[Error, Channel]] = {
        import as.dispatcher

        send( Event.Join, payload )( flow ).map {
            case Result.Success( _ ) ⇒ Right( Channel( topic, flow ) )
            case error: Error        ⇒ Left( error )
        }
    }

    def send( event: Event, payload: Json )(
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
            .toMat( Sink.headOption[Response] )( Keep.right )
            .run()
            .map {
                case Some( response ) if response.isOk ⇒
                    Result.Success( response )
                case Some( response ) ⇒ Result.Failure( response )
                case None             ⇒ Result.None
            }
    }
}