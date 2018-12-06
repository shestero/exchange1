// Trait for currency converter's

import scala.util.Try

trait CurrencyConverter {
  def convert(req:ExchRequest): Try[ExchResponse]
}
