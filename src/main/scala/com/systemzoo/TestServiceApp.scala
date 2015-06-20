package com.systemzoo

import akka.actor.ActorDSL._
import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.io.IO
import akka.io.Tcp.Bound
import spray.can.Http
import scala.reflect.ClassTag

object TestServiceApp {
  val serviceName = "test-service"
  val config = TestServiceConfig()

  def main(args:Array[String]) {
    implicit val system = ActorSystem(serviceName + "-system")

    val serviceProps = Props(implicitly[ClassTag[TestServiceActor]].runtimeClass, serviceName, config)
    val service = system.actorOf(serviceProps, serviceName)

    val ioListener = actor("ioListener")(new Act with ActorLogging {
      become {
        case b@Bound(connection) => log.info(b.toString)
      }
    })

    IO(Http).tell(
      Http.Bind(service, "::0", 80),
      ioListener
    )
  }
}
