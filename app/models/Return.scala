package models

import play.api.libs.json.{Json, OFormat}

case class Return(id: Long, user: Long)

object Return {
  implicit val returnFormat: OFormat[Return] = Json.format[Return]
}
