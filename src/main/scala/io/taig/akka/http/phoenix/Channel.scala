package io.taig.akka.http.phoenix

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{ Flow, Keep, Sink, Source }
import io.circe.Json
import io.taig.akka.http.phoenix.Channel.Result
import io.taig.akka.http.phoenix.message.{ Request, Response }

import scala.concurrent.Future

case class Channel(
        topic: Topic,
        flow:  Flow[( Event, Json, Ref ), Response, NotUsed]
)(
        implicit
        as: ActorSystem,
        m:  Materializer
) {
    def send( event: Event, payload: Json ): Future[Result] = {
        import as.dispatcher

        val ref = Ref.unique()

        Source.single( event, payload, ref )
            .via( flow )
            .filter( topic isSubscribedTo _.topic )
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

    def leave(): Future[Result] = send( Event.Leave, Json.Null )
}

object Channel {
    def join(
        topic:   Topic,
        payload: Json  = Json.Null
    )(
        flow: Flow[Request, Response, _]
    )(
        implicit
        as: ActorSystem,
        m:  Materializer
    ): Future[Either[Error, Channel]] = {
        import as.dispatcher

        val channel = Flow[( Event, Json, Ref )].map {
            case ( event, payload, ref ) ⇒
                Request( topic, event, payload, ref )
        }.via( flow ).filter( topic isSubscribedTo _.topic )

        val ref = Ref.unique()

        Source.single( Event.Join, payload, ref )
            .via( channel )
            .filter( topic isSubscribedTo _.topic )
            .filter( ref == _.ref )
            .toMat( Sink.headOption[Response] )( Keep.right )
            .run()
            .map {
                case Some( response ) if response.isOk ⇒
                    Right( Channel( topic, channel ) )
                case Some( response ) ⇒ Left( Result.Failure( response ) )
                case None             ⇒ Left( Result.None )
            }
    }

    sealed trait Result
    sealed trait Error extends Result

    object Result {
        case class Success( response: Response ) extends Result
        case class Failure( response: Response ) extends Error
        case object None extends Error
    }
}