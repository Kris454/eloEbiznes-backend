package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import models.{User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, nonEmptyText}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(cc: MessagesControllerComponents, userRepository: UserRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val updateUserForm: Form[UpdateUserForm] = Form {
    mapping(
      "id" -> longNumber,
      "loginInfo" -> nonEmptyText,
      "email" -> nonEmptyText,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "login" -> nonEmptyText,
    )(UpdateUserForm.apply)(UpdateUserForm.unapply)
  }

  def getUsers: Action[AnyContent] = Action.async { implicit request =>
    val fetchedUsers = userRepository.list()
    fetchedUsers.map(user => Ok(views.html.users(user)))
  }

  def removeUser(id: Long): Action[AnyContent] = Action {
    userRepository.delete(id)
    Redirect("/getusers")
  }

  def updateUser(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepository.getById(id)
    user.map(usr => {
      val userForm = updateUserForm.fill(UpdateUserForm(usr.id, null, usr.email, usr.firstName, usr.lastName, usr.login))
      Ok(views.html.userupdate(userForm))
    })
  }

  def updateUserHandle(): Action[AnyContent] = Action.async { implicit request =>
    updateUserForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.userupdate(errorForm))
        )
      },
      usr => {
        userRepository.update(usr.id, User(usr.id, null, usr.email, usr.firstName, usr.lastName, usr.login)).map { _ =>
          Redirect(routes.UserController.updateUser(usr.id)).flashing("success" -> "user updated")
        }
      }
    )
  }

  def updateUserJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val email = request.body.asJson.get("email").as[String]
    val firstName = request.body.asJson.get("firstName").as[String]
    val lastName = request.body.asJson.get("lastName").as[String]
    val login = request.body.asJson.get("login").as[String]

    userRepository.update(id, User(id, null, email, firstName, lastName, login)).map { user =>
      Ok("Updated user")
    }
  }

  def getUserJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userRepository.getById(id).map { user =>
      Ok(Json.toJson(user))
    }
  }

  def getUsersJson: Action[AnyContent] = Action.async { implicit request =>
    userRepository.list().map { users =>
      Ok(Json.toJson(users))
    }
  }

  def removeUserJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    userRepository.delete(id).map { _ =>
      Ok("User removed")
    }
  }

}

case class UpdateUserForm(id: Long, loginInfo: String, email: String, firstName: String, lastName: String, login: String)
