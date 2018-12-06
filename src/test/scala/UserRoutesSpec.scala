import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

import scala.math.abs


class ExchRoutesSpec extends WordSpec
  with Matchers with ScalaFutures with ScalatestRouteTest with JsonSupport with ExchService {

  //implicit val system = ActorSystem(s"${AppExchangeServer.appCodeName}-test")

  // We use the real UserRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe()
  //override val userRegistryActor: ActorRef = system.actorOf(UserRegistryActor.props, "userRegistry")


  implicit val executionContext = system.dispatcher // for Future
  implicit val converter: Future[CurrencyConverter] = Future(new EuroBank)


  "UserRoutes" should {

    "return empty response for empty queries (POST /)" in {
      val request = Post("/", HttpEntity( ContentTypes.`application/json`,
        """{"data":[]}"""
        //"""{"data":[{"currencyFrom":"RUB","currencyTo":"USD","valueFrom":66.62},{"currencyFrom":"eur","currencyTo":"rub","valueFrom":20.1}]}"""
      ) )

      request ~> route ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String].filter(_>' ') should ===("""{"data":[]}""")
      }
    }

    "convertion to itself should give the same result" in {
      val orig = 1234 // any number >=0.1
      val cur  = "RUB"
      val request = Post("/", HttpEntity( ContentTypes.`application/json`,
        s"""{"data":[{"currencyFrom":"$cur","currencyTo":"$cur","valueFrom":$orig}]}"""
      ) )

      request ~> route ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // only one response expected
        val responses = entityAs[ExchResponses]
        responses.data.size should ===(1)

        if (responses.data.size>0) {
          val converted = responses.data.head.valueTo.toDouble

          (abs(converted-orig)<=0.01) should ===(true) // aprox

          responses.data.head.valueFrom should ===(orig)
        }
      }
    }

    "to and back convertion should give the same result" in {
      val orig = 5678 // any number >=0.1
      val from = "USD"
      val to   = "CHF"
      val request1 = Post("/", HttpEntity( ContentTypes.`application/json`,
        s"""{"data":[{"currencyFrom":"$from","currencyTo":"$to","valueFrom":$orig}]}"""
      ) )

      request1 ~> route ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // only one response expected
        val responses1 = entityAs[ExchResponses]
        responses1.data.size should ===(1)

        if (responses1.data.size>0) {
          responses1.data.head.valueFrom should ===(orig)

          val converted1 = responses1.data.head.valueTo.toDouble

          val request2 = Post("/", HttpEntity( ContentTypes.`application/json`,
            s"""{"data":[{"currencyFrom":"$to","currencyTo":"$from","valueFrom":$converted1}]}"""
          ) )

          request2 ~> route ~> check {
            status should ===(StatusCodes.OK)

            // we expect the response to be json:
            contentType should ===(ContentTypes.`application/json`)

            // only one response expected
            val responses2 = entityAs[ExchResponses]
            responses2.data.size should ===(1)

            if (responses2.data.size > 0) {
              responses2.data.head.valueFrom should ===(converted1)

              val converted2 = responses2.data.head.valueTo.toDouble
              // println(s"orig=$orig conv1=$converted1 conv2=$converted2")

              (abs(converted2-orig) <= 0.02) should ===(true) // aprox
            }
          }
        }
      }
    }

  }

}


