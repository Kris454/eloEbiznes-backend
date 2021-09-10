package models

import play.api.libs.json.{Json, OFormat}

case class ProductGrade(id: Long, product: Long, grade: Int)

object ProductGrade {
  implicit val productGradeFormat: OFormat[ProductGrade] = Json.format[ProductGrade]
}
