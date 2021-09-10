package models

import play.api.libs.json.{Json, OFormat}

case class Order(id: Long, user: Long, price: Int)

object Order {
  implicit val orderFormat: OFormat[Order] = Json.format[Order]
}
