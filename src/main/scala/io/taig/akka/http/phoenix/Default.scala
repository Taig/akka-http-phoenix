package io.taig.akka.http.phoenix

import scala.concurrent.duration._
import scala.language.postfixOps

object Default {
    val heartbeat: Option[FiniteDuration] = Some( 7 seconds )

    val timeout: Duration = 5 seconds
}