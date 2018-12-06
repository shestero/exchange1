// Sample of currency converter

import org.apache.logging.log4j.scala.Logging

import scala.util.{Try,Success,Failure}
import akka.http.scaladsl.model.DateTime

import scala.xml.XML

class EuroBank extends CurrencyConverter with Logging {

  val url = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml"

  // ==============

  // load XML
  val xml = XML.load(url) // "gesmes:Envelope"

  // load rates
  val rates = ( xml \ "Cube" \ "Cube" \ "Cube" )
    .map( node => ( node.attribute("currency"), node.attribute("rate") ) match {

      // NB Scala 2.13 introduced String::toDoubleOption
      case ( Some(currency), Some(rate) ) => Try { rate.head.toString.toDouble } match {
          case Success(rate) => Some( ( currency.head.toString, rate ) )
          case _ =>
            logger.warn( s"Warning: cannot convert $rate (from request) to Double" )
            None
        }

      case _ =>
        logger.warn( s"Warning: cannot decode XML node $node" )
        None

    } ).filter(_.isDefined).flatten.toMap

  assert(!rates.isEmpty) // fail the server if nothing was loaded here

  // load actual date
  // val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val date = ( xml \ "Cube" \ "Cube" ).head.attribute( "time" ).map(_.toString) // scala.xml.Text --> String
      .flatMap( s => DateTime.fromIsoDateTimeString(s+"T00:00:00") )

  logger.info( s"Loaded from $url ${rates.size} rates, dated ${date.getOrElse("unknown")}" )

  // ==============

  protected def getRatio(currency: String) = {
    val currencyUC =currency.toUpperCase
    if (currencyUC=="EUR") 1.0 else rates.get(currencyUC).get
  }

  // a single exchange
  def convert(req:ExchRequest): Try[ExchResponse] = {
    Try {
      val ratioFrom = getRatio( req.currencyFrom )
      val ratioTo   = getRatio( req.currencyTo )

      val ratio = ratioTo/ratioFrom

      val valueTo = (req.valueFrom*ratio*100).ceil/100

      ExchResponse( req.currencyFrom, req.currencyTo, req.valueFrom, valueTo, date )
    }
  }
}
