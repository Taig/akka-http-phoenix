//package io.taig.akka.http.phoenix.internal
//
//import akka.actor.ActorRef
//import io.circe.Json
//import io.taig.akka.http.phoenix.{ Event, Ref, Topic }
//import io.taig.akka.http.phoenix.message.{ Request, Response }
//import io.taig.{ phoenix_akka ⇒ api }
//import monix.eval.Task
//
//class Channel(
//        val topic: Topic,
//        actor:     ActorRef,
//        ref:       ⇒ Ref
//) extends api.Channel {
//    override def send( event: Event, payload: Json ): Task[Response] = {
//        val request = Request( topic, event, payload, ??? )
//        ???
//    }
//
//    override def toString = s"Channel($topic)"
//}