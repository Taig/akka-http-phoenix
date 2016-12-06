package io.taig.akka.http.phoenix

import io.taig.akka.http.phoenix.message.Response

sealed trait Result
sealed trait Error extends Result

object Result {
    case class Success( response: Response ) extends Result
    case class Failure( response: Response ) extends Error
    case object None extends Error
}