package models

import play.api.libs.json.{Json, OFormat}
import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

case class User(id: Long, loginInfo: LoginInfo, email: String, firstName: String, lastName: String, login: String) extends Identity

object User {
  implicit val loginInfoFormat: OFormat[LoginInfo] = Json.format[LoginInfo]
  implicit val userFormat: OFormat[User] = Json.format[User]
}
