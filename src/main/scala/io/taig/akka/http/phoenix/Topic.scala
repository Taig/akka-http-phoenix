package io.taig.akka.http.phoenix

import io.circe.{ Decoder, Encoder }
import cats.implicits._

case class Topic( name: String, identifier: Option[String] ) {
    def isSubscribedTo( topic: Topic ): Boolean = topic match {
        case Topic( `name`, `identifier` ) ⇒ true
        case Topic( `name`, None )         ⇒ true
        case _                             ⇒ false
    }

    def serialize = name + identifier.map( ":" + _ ).getOrElse( "" )

    override def toString = s"Topic($serialize)"
}

object Topic {
    implicit val encoderTopic: Encoder[Topic] = {
        Encoder[String].contramap( _.serialize )
    }

    implicit val decoderTopic: Decoder[Topic] = {
        Decoder[String].emap { topic ⇒
            Either.fromOption( parse( topic ), "Invalid format" )
        }
    }

    val Phoenix = Topic( "phoenix" )

    def apply( name: String, identifier: String ): Topic = {
        Topic( name, Some( identifier ) )
    }

    def apply( name: String ): Topic = Topic( name, None )

    def parse( topic: String ): Option[Topic] = topic.split( ":" ) match {
        case Array( name )             ⇒ Some( Topic( name ) )
        case Array( name, identifier ) ⇒ Some( Topic( name, identifier ) )
        case _                         ⇒ None
    }
}