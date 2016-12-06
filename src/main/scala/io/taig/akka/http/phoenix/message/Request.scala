package io.taig.akka.http.phoenix.message

import io.circe.{ Encoder, Json }
import io.circe.generic.semiauto._
import io.taig.akka.http.phoenix.{ Event, Ref, Topic }

case class Request(
    topic:   Topic,
    event:   Event,
    payload: Json  = Json.Null,
    ref:     Ref   = Ref.unique()
)

object Request {
    implicit val encoder: Encoder[Request] = deriveEncoder[Request]
}