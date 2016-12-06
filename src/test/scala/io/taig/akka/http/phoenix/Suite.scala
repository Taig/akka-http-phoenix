package io.taig.akka.http.phoenix

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.WebSocketRequest
import akka.stream.ActorMaterializer
import org.scalatest.{ AsyncFlatSpec, BeforeAndAfterAll, Matchers }

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

trait Suite
        extends AsyncFlatSpec
        with Matchers
        with BeforeAndAfterAll {
    implicit val system = ActorSystem( "test-system" )

    implicit val materializer = ActorMaterializer()

    implicit val executor = system.dispatcher

    val request = WebSocketRequest( "ws://localhost:4000/socket/websocket" )

    override def afterAll(): Unit = {
        super.afterAll()

        Await.result( Http().shutdownAllConnectionPools(), 3 seconds )
        Await.result( system.terminate(), 3 seconds )
        materializer.shutdown()
    }
}
