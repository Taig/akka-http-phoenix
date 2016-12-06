package io.taig.akka.http.phoenix

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Keep, Sink, Source }
import io.taig.akka.http.phoenix.message.{ Request, Response }
import org.scalatest.{ AsyncFlatSpec, BeforeAndAfterAll, Matchers }
import io.circe.syntax._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.{ implicitConversions, postfixOps }

class PhoenixTest
        extends AsyncFlatSpec
        with Matchers
        with BeforeAndAfterAll {
    implicit val system = ActorSystem( "test-system" )

    implicit val materializer = ActorMaterializer()

    import system.dispatcher

    val request = WebSocketRequest( "ws://localhost:4000/socket/websocket" )

    override def afterAll(): Unit = {
        super.afterAll()

        Await.result( Http().shutdownAllConnectionPools(), 3 seconds )
        Await.result( system.terminate(), 3 seconds )
        materializer.shutdown()
    }

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

    it should "allow to leave a Channel" in {
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
}