//package io.taig.akka.http.phoenix.internal
//
//import akka.NotUsed
//import akka.actor.ActorRef
//import akka.stream.Materializer
//import akka.stream.scaladsl.{ Keep, Sink, Source }
//import io.circe.Json
//import io.circe.syntax._
//import io.taig.akka.http.phoenix._
//import io.taig.akka.http.phoenix.message.{ Request, Response }
//import io.taig.{ phoenix_akka ⇒ api }
//import monix.eval.Task
//
//class Phoenix(
//        actor:  ActorRef,
//        source: Source[Either[Phoenix.Error, Response], NotUsed]
//)(
//        implicit
//        materializer: Materializer
//) extends api.Phoenix {
//    private val loggingSource = source.map { message ⇒
//        logger.debug {
//            s"""
//               |Received message:
//               |$message
//             """.stripMargin.trim
//        }
//
//        message
//    }
//
//    /**
//     * Endless stream of incrementing Refs (starting at 0)
//     */
//    private val iterator: Iterator[Ref] = {
//        Stream
//            .iterate( 0L )( _ + 1 )
//            .map( n ⇒ Ref( n.toString ) )
//            .iterator
//    }
//
//    /**
//     * Get a fresh, unique Ref for this Phoenix instance
//     */
//    private def ref(): Ref = synchronized( iterator.next() )
//
//    def send( request: Request ): Task[Option[Response]] = Task.defer {
//        logger.debug {
//            s"""
//               |Sending Request:
//               |$request
//             """.stripMargin.trim
//        }
//
//        val sink = Sink.headOption[Response]
//
//        val response = loggingSource
//            .collect { case Right( response ) ⇒ response }
//            .filter { response ⇒
//                val topic = request.topic == response.topic
//                val ref = request.ref == response.ref
//                topic && ref
//            }
//            .toMat( sink )( Keep.right )
//            .run()
//
//        actor ! request.asJson
//
//        Task.fromFuture( response )
//    }
//
//    override def join( topic: Topic, payload: Json ): Task[Either[Throwable, Channel]] = {
//        Task.defer {
//            logger.debug( s"Requesting to join Channel: $topic" )
//
//            send( message.Request( topic, Event.Join, payload, ref() ) ).map {
//                case Some( _ ) ⇒
//                    logger.debug( s"Successfully joined Channel: $topic" )
//                    Right( new internal.Channel( topic, actor, ref() ) )
//                case None ⇒
//                    Left( new RuntimeException( "Shit did not work out bruh" ) )
//            }
//        }
//    }
//}