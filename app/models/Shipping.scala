package models

import play.api.libs.json.{Json, OFormat}

case class Shipping(id: Long, user: Long, address: String, typeOf: Int)

object Shipping {
  implicit val shippingFormat: OFormat[Shipping] = Json.format[Shipping]
}
