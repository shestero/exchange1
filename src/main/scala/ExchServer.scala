// Defines the application (object) and HTTP server (class)

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

import scala.io.StdIn


object ExchServer extends App {

  val appCodeName = "exchange-server"

  val port    = 8088

  // ================

  implicit val system = ActorSystem(appCodeName)
  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher // for Future

  implicit val convEuroBank: Future[CurrencyConverter] = Future( new EuroBank ) // sample converter (delayed init)

  // ================

  val server = new ExchServer("localhost",port) // create and start the server

  println("Press RETURN to stop the server...")
  StdIn.readLine() // let it run until user presses return

  server.stop() // shut down the server
  system.terminate()

}

class ExchServer(address:String, port:Int)(
                 implicit val system:ActorSystem,
                 implicit val materializer:ActorMaterializer,
                 implicit val executionContext: ExecutionContext,
                 implicit val converter: Future[CurrencyConverter]
                ) extends ExchService {

  val bindingFuture = Http().bindAndHandle(route, address, port)
  logger.info(s"HTTP server online at http://$address:$port/$pathStr")

  bindingFuture.onComplete { // this usually happens when starting a second instance of the application
    case Failure(e) =>
      logger.fatal( s"HTTP server was unable to start! Error: $e" )
      stop()
      system.terminate()
      java.lang.System.exit(1)

    case _ => Unit
  }

  def stop() = {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .foreach{_ => system.terminate()} //.onComplete(_ => aSystem.terminate()) // ⇒ and shutdown when done
    logger.info( "HTTP server is down." )
  }

}

