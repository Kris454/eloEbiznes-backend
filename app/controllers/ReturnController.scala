package controllers

import models.{ReturnRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ReturnController @Inject()(cc: MessagesControllerComponents, returnRepository: ReturnRepository, userRepository: UserRepository)
                                (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val returnForm: Form[CreateReturnForm] = Form {
    mapping(
      "user" -> longNumber
    )(CreateReturnForm.apply)(CreateReturnForm.unapply)
  }

  def getReturns: Action[AnyContent] = Action.async { implicit request =>
    val fetchedReturns = returnRepository.list()
    fetchedReturns.map(rtrn => Ok(views.html.returns(rtrn)))
  }

  def removeReturn(id: Long): Action[AnyContent] = Action {
    returnRepository.delete(id)
    Redirect("/getreturns")
  }

  def addReturn(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = userRepository.list()
    users.map(user => Ok(views.html.returnadd(returnForm, user)))
  }

  def addReturnHandle(): Action[AnyContent] = Action.async { implicit request =>
    var usr: Seq[User] = Seq[User]()
    userRepository.list().onComplete {
      case Success(cat) => usr = cat
      case Failure(_) => print("fail")
    }

    returnForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.returnadd(errorForm, usr))
        )
      },
      rtrn => {
        returnRepository.create(rtrn.user).map { _ =>
          Redirect(routes.ReturnController.addReturn()).flashing("success" -> "return created")
        }
      }
    )

  }

  def addReturnJson(): Action[AnyContent] = Action.async { implicit request =>
    val user = request.body.asJson.get("user").as[Long]

    returnRepository.create(user).map { rtrn =>
      Ok(Json.toJson(rtrn))
    }
  }

  def getReturnJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    returnRepository.getById(id).map { rtrn =>
      Ok(Json.toJson(rtrn))
    }
  }

  def getReturnsJson: Action[AnyContent] = Action.async { implicit request =>
    returnRepository.list().map { returns =>
      Ok(Json.toJson(returns))
    }
  }

  def removeReturnJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    returnRepository.delete(id).map { _ =>
      Ok("Return removed")
    }
  }

}

case class CreateReturnForm(user: Long)
