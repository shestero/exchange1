// Defines the route for the service.

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._//{as, complete, entity, path, post}
import akka.stream.ActorMaterializer

import org.apache.logging.log4j.scala.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import scala.concurrent.blocking // blocking is used only once for short pause before shut down the application
// in case of fatal error (to let respond with error message pass to HTTP client).

// provide route value
trait ExchService extends JsonSupport with Logging {

  val pathStr = "" // route path suffix, e.g."exch"

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val executionContext: ExecutionContext

  implicit val converter: Future[CurrencyConverter]

  //def stop()

  protected val route =
    ( post & path(pathStr) & entity(as[ExchRequests]) ) { requests =>

      // Note: https://github.com/akka/akka-http/issues/1366
      onComplete( converter ) {

        case Success(converter) =>
          val successful = requests.data.map( converter.convert ).filter(_.isSuccess).map(_.get)
          complete( ExchResponses(successful) )

        case Failure(exception) =>
          val message = s"Cannot initialize currency converter. Error: $exception"
          logger.fatal(message)

          // delayed application shutdown
          Future {
            logger.fatal(s"Shutting down the server application in 1 second because of $exception")
            blocking { Thread.sleep(1000) }
            Http().shutdownAllConnectionPools() andThen { case _ =>
              system.terminate(); java.lang.System.exit(1) }
          }

          complete(message)
      }
    }

}
