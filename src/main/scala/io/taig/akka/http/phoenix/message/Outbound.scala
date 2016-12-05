package io.taig.akka.http.phoenix.message

import io.circe.{ Encoder, Json }
import io.circe.generic.semiauto._
import io.taig.akka.http.phoenix.{ Event, Ref, Topic }

sealed trait Outbound extends Product with Serializable {
    def topic: Topic

    def event: Event

    def payload: Json

    def ref: Ref
}

case class Request(
    topic:   Topic,
    event:   Event,
    payload: Json  = Json.Null,
    ref:     Ref   = Ref.unique()
) extends Outbound

object Request {
    implicit val encoder: Encoder[Request] = deriveEncoder[Request]
}