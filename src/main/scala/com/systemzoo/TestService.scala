package com.systemzoo

import java.text.{SimpleDateFormat, DateFormat}
import java.util.{Date, TimeZone}

import akka.actor.ActorSystem
import com.typesafe.scalalogging.slf4j.LazyLogging
import spray.http.{StatusCode, StatusCodes}
import spray.routing._
import scala.concurrent.ExecutionContext
import scala.util.Random

trait TestService extends HttpService with LazyLogging {

  val config: TestServiceConfig

  def actorSystem: ActorSystem
  protected implicit def executionContext = actorRefFactory.dispatcher

  def delay() = {
    if(config.latency != 0) Thread.sleep(Random.nextInt(config.latency))
  }

  def code(): StatusCode =
    if(Random.nextDouble() > config.failRate) StatusCodes.OK else StatusCodes.InternalServerError

  def returnValue(in: Int): Int =
    if(Random.nextDouble() > config.badResponseRate) in else in + 1

  def logging = mapRequestContext { ctx ⇒
    val startTime = System.currentTimeMillis()
    ctx.withHttpResponseMapped { response ⇒
      val runTime = System.currentTimeMillis() - startTime

      logger.info(s"""{"time":"$currentTime", "service":"${TestServiceConfig.serviceName}", "code":${response.status.intValue},"duration":$runTime, "request":"${ctx.request.uri.path.tail}", "response":"${response.entity.data.asString}"}""")
      response
    }
  }

  def currentTime = {
    val timeZone = TimeZone.getTimeZone("UTC")
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,sss")
    dateFormat.setTimeZone(timeZone)
    dateFormat.format(new Date())
  }

  def simpleGet(implicit ec: ExecutionContext) =
    get {
      detach() {
        delay()
        complete(code(), "")
      }
    }

  def returnGet(implicit ec: ExecutionContext) =
    get {
      path(IntNumber) { value =>
        detach() {
          delay()
          val returnCode = code()
          if(returnCode.intValue == 200)
            complete(code(), returnValue(value).toString)
          else
            complete(code(), "")
        }
      }
    }

  def route: Route = logging { returnGet ~ simpleGet }
}
