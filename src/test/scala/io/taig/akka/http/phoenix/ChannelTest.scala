package io.taig.akka.http.phoenix

import io.circe.Json
import io.circe.syntax._

import scala.language.postfixOps

class ChannelTest extends Suite {
    it should "allow to leave the Channel" in {
        val topic = Topic( "echo", "foobar" )

        for {
            phoenix ← Phoenix( request )
            Right( channel ) ← phoenix.join( topic )
            Result.Success( response ) ← channel.leave()
            _ = phoenix.close()
        } yield {
            response.isOk shouldBe true
            response.event shouldBe Event.Reply
            response.topic shouldBe topic
            response.error shouldBe None
        }
    }

    it should "receive echo messages" in {
        val topic = Topic( "echo", "foobar" )

        for {
            phoenix ← Phoenix( request )
            Right( channel ) ← phoenix.join( topic )
            Result.Success( response ) ← channel.send( Event( "echo" ), "foobar".asJson )
            _ = phoenix.close()
        } yield {
            response.event shouldBe Event.Reply
            response.topic shouldBe topic
        }
    }

    it should "timeout when the server omits a response" in {
        val topic = Topic( "echo", "foobar" )

        for {
            phoenix ← Phoenix( request )
            Right( channel ) ← phoenix.join( topic )
            result ← channel.send( Event( "no_reply" ), Json.Null )
            _ = phoenix.close()
        } yield {
            result shouldBe Result.None
        }
    }
}