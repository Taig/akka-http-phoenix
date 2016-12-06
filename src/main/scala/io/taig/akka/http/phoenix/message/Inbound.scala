package io.taig.akka.http.phoenix.message

import io.circe.{ Decoder, Json }
import io.circe.generic.semiauto._
import io.taig.akka.http.phoenix.{ Event, Ref, Topic }
import cats.syntax.either._

sealed trait Inbound extends Product with Serializable {
    def topic: Topic

    def event: Event
}

object Inbound {
    def unapply( inbound: Inbound ): Option[( Topic, Event )] = {
        Some( inbound.topic, inbound.event )
    }
}

case class Response(
        topic:   Topic,
        event:   Event,
        payload: Option[Response.Payload],
        ref:     Ref
) extends Inbound {
    def isOk: Boolean = payload.exists( _.status == Response.Status.Ok )

    def isError: Boolean = payload.exists( _.status == Response.Status.Error )

    def error: Option[String] = {
        payload.flatMap( _.response.cursor.get[String]( "reason" ).toOption )
    }
}

object Response {
    implicit val decoder: Decoder[Response] = deriveDecoder

    case class Payload( status: Status, response: Json )

    object Payload {
        implicit val decoderPayload: Decoder[Option[Payload]] = {
            Decoder.instance { cursor ⇒
                val patched = cursor.withFocus {
                    case json if json.asObject.exists( _.size == 0 ) ⇒
                        Json.Null
                    case json ⇒ json
                }

                Decoder.decodeOption( deriveDecoder[Payload] )
                    .decodeJson( patched.focus )
            }
        }
    }

    sealed case class Status( value: String )

    object Status {
        object Error extends Status( "error" )
        object Ok extends Status( "ok" )

        implicit val decoderStatus: Decoder[Status] = {
            Decoder[String].map {
                case Error.value ⇒ Error
                case Ok.value    ⇒ Ok
                case value       ⇒ Status( value )
            }
        }
    }
}

case class Push(
    topic:   Topic,
    event:   Event,
    payload: Json
) extends Inbound