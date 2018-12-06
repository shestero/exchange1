import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
//import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.DateTime

import spray.json.DefaultJsonProtocol._
import spray.json.PrettyPrinter
import spray.json.{JsonFormat,JsString,JsValue,JsObject,JsNumber,RootJsonFormat}

import scala.collection.immutable.ListMap

import scala.util.Try


// trait DomainModel
final case class ExchRequest(currencyFrom: String, currencyTo: String, valueFrom: Double)
final case class ExchResponse(currencyFrom: String, currencyTo: String, valueFrom: Double, valueTo: Double, actualDate: Option[DateTime])
case class ExchRequests(data: Seq[ExchRequest]) { def size = data.size  }
case class ExchResponses(data: Seq[ExchResponse])


// the implicit JSON support:
trait JsonSupport extends SprayJsonSupport {

  implicit val printer = PrettyPrinter

  implicit val fmtDateTime = new JsonFormat[DateTime] {
    def write(x: DateTime) = JsString(x.toIsoDateTimeString())
    // actually not used:
    def read(value: JsValue) = value match {
      case JsString(x) => DateTime.fromIsoDateTimeString(x).getOrElse(DateTime.now)
      // case x => throw new RuntimeException(s"Unexpected type ${x.getClass.getName} when trying to parse LocalDateTime")
      case _ => DateTime.now
    }
  }

  implicit val fmtExchRequest  = jsonFormat3(ExchRequest)
  implicit val fmtExchRequests = jsonFormat1(ExchRequests)

  //implicit val fmtExchRespond  = jsonFormat5(ExchResponse)
  // to preserve output field order in JSON:
  // ( "it used to be ListMap but then since 1.3 they changed to Map,
  // anyways according to Json specification order of fields is not preserved". – grotrianster Sep 4 '15 )
  implicit object customResponseJsonProtocol extends RootJsonFormat[ExchResponse] {
    def write(resp: ExchResponse): JsValue = JsObject( ListMap(
      "currencyFrom" -> JsString(resp.currencyFrom),
      "currencyTo" -> JsString(resp.currencyTo),
      "valueFrom" -> JsNumber(resp.valueFrom),
      "valueTo" -> JsNumber(resp.valueTo),
      "actualDate" -> JsString( Try{ resp.actualDate.get.toIsoDateString() }.getOrElse("unknown") )
      // "actualDate" is added to output
    ) )

    // NB this uses in tests only:
    def read(json: JsValue): ExchResponse = jsonFormat5(ExchResponse).read(json)
  }

  implicit val fmtExchResponds = jsonFormat1(ExchResponses)

}
