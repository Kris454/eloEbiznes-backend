package models

import play.api.libs.json.{Json, OFormat}

case class ProductComment(id: Long, product: Long, comment: String)

object ProductComment {
  implicit val productCommentFormat: OFormat[ProductComment] = Json.format[ProductComment]
}
