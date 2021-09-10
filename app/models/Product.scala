package models

import play.api.libs.json.{Json, OFormat}

case class Product(id: Long, name: String, description: String, category: Int, price: Int)

object Product {
  implicit val bookFormat: OFormat[Product] = Json.format[Product]
}
