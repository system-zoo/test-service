package com.systemzoo

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
    Thread.sleep(Random.nextInt(config.latency))
  }

  def code(): StatusCode =
    if(Random.nextDouble() > config.failRate) StatusCodes.OK else StatusCodes.InternalServerError

  def returnValue(in: Int): Int =
    if(Random.nextDouble() > config.badResponseRate) in else in + 1

  def logging = mapRequestContext { ctx ⇒
    val startTime = System.currentTimeMillis()
    ctx.withHttpResponseMapped { response ⇒
      val runTime = System.currentTimeMillis() - startTime
      if(ctx.request.uri.path.tail.toString() == "")
        logger.info(s"""{"code":${response.status.intValue},"duration":$runTime}""")
      else
        logger.info(s"""{"code":${response.status.intValue},"duration":$runTime, "request":${ctx.request.uri.path.tail}, "response":${response.entity.data.asString}}""")

      response
    }
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
          complete(code(), returnValue(value).toString)
        }
      }
    }

  def route: Route = logging { returnGet ~ simpleGet }
}
