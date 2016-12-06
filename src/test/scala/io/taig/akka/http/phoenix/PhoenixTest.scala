package io.taig.akka.http.phoenix

import akka.stream.scaladsl.{ Keep, Sink, Source }
import io.taig.akka.http.phoenix.message.{ Request, Response }

import scala.concurrent.duration._
import scala.language.postfixOps

class PhoenixTest extends Suite {
    it should "send a heartbeat in" in {
        for {
            phoenix ← Phoenix( request )
            response ← Source
                .empty[Request]
                .via( phoenix.flow )
                .toMat( Sink.head )( Keep.right )
                .run()
            _ = phoenix.close()
        } yield {
            response.isOk shouldBe true
            response.topic shouldBe Topic.Phoenix
            response.event shouldBe Event.Reply
        }
    }

    it should "allow to disable the heartbeat" in {
        for {
            phoenix ← Phoenix( request, heartbeat = None )
            response ← Source
                .empty[Request]
                .completionTimeout( 10 seconds )
                .via( phoenix.flow )
                .toMat( Sink.headOption )( Keep.right )
                .run()
            _ = phoenix.close()
        } yield {
            response shouldBe None
        }
    }

    it should "allow to close the connection" in {
        for {
            phoenix ← Phoenix( request )
            _ = phoenix.close()
            response ← {
                Source
                    .single( Request( Topic.Phoenix, Event( "echo" ) ) )
                    .via( phoenix.flow )
                    .toMat( Sink.headOption[Response] )( Keep.right )
                    .run()
            }
        } yield response shouldBe None
    }

    it should "allow to join a Channel" in {
        val topic = Topic( "echo", "foobar" )

        for {
            phoenix ← Phoenix( request )
            Right( channel ) ← phoenix.join( topic )
            _ = phoenix.close()
        } yield channel.topic shouldBe topic
    }

    it should "fail to join an invalid Channel" in {
        val topic = Topic( "foo", "bar" )

        for {
            phoenix ← Phoenix( request )
            Left( Result.Failure( response ) ) ← phoenix.join( topic )
            _ = phoenix.close()
        } yield {
            response.isError shouldBe true
            response.event shouldBe Event.Reply
            response.topic shouldBe topic
            response.error shouldBe Some( "unmatched topic" )
        }
    }
}