package com.systemzoo

import akka.actor.{ActorLogging, Actor}
import com.typesafe.scalalogging.slf4j.LazyLogging
import spray.http.StatusCodes
import spray.routing.ExceptionHandler
import scala.util.control.NonFatal

class TestServiceActor(val name: String, val config: TestServiceConfig) extends Actor with ActorLogging with LazyLogging with TestService {

  def actorRefFactory = context
  def receive = runRoute(route)
  def actorSystem = context.system

  implicit def loggingExceptionHandler(): ExceptionHandler =
    ExceptionHandler {
      case NonFatal(e) =>
        requestUri { uri =>
          val sw = new java.io.StringWriter
          try {
            val pw = new java.io.PrintWriter(sw)
            try {
              e.printStackTrace(new java.io.PrintWriter(sw))
              logger.error(s"Request to $uri could not be handled normally: \n" +  sw.toString)
            } finally {
              pw.close()
            }
          } finally {
            sw.close()
          }

          //telling the caller the error helps in debugging but could be a security concern, turning off for now
          //complete(StatusCodes.InternalServerError, e.getMessage)

          complete(StatusCodes.InternalServerError)
        }
    }
}
