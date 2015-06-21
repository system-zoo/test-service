package com.systemzoo

import org.scalatest._
import spray.http.StatusCodes
import spray.testkit.ScalatestRouteTest
import scala.concurrent.duration._
import scala.util.Random

class TestService_AT extends FunSpec with ScalatestRouteTest with ShouldMatchers {

  implicit val routeTestTimeout = RouteTestTimeout(30.seconds)

  describe("The TestService route should work perfectly with a good config"){

    val routeService = new TestService {
      def actorRefFactory = system
      def actorSystem = system
      val config = TestServiceConfig(0, 0, 0)
    }

    val route = routeService.route

    it("Should return back a simple get") {
      Get(s"/") ~> route ~> check {
        assert(response.status === StatusCodes.OK)
      }
    }

    it("Should return back the same value passed in") {
      val int = Random.nextInt(1000)
      Get(s"/$int") ~> route ~> check {
        assert(response.status === StatusCodes.OK)
        assert(response.entity.data.asString.toInt === int)
      }
    }
  }

  describe("The TestService route should work horribly with a bad config"){

    val routeService = new TestService {
      def actorRefFactory = system
      def actorSystem = system
      val config = TestServiceConfig(100, 1, 1)
    }

    val route = routeService.route

    it("Should return back a simple get") {
      Get(s"/") ~> route ~> check {
        assert(response.status === StatusCodes.InternalServerError)
      }
    }

    it("Should return back the same value passed in") {
      val int = Random.nextInt(1000)
      Get(s"/$int") ~> route ~> check {
        assert(response.status === StatusCodes.InternalServerError)
        assert(response.entity.data.asString === "")
      }
    }
  }
}
