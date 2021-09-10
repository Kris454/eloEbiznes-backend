package controllers

import models.{Shipping, ShippingRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, number, nonEmptyText}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ShippingController @Inject()(cc: MessagesControllerComponents, shippingRepository: ShippingRepository, userRepository: UserRepository)
                               (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val shippingForm: Form[CreateShippingForm] = Form {
    mapping(
      "user" -> longNumber,
      "address" -> nonEmptyText,
      "typeOf" -> number,
    )(CreateShippingForm.apply)(CreateShippingForm.unapply)
  }

  def getShippings: Action[AnyContent] = Action.async { implicit request =>
    val fetchedShippings = shippingRepository.list()
    fetchedShippings.map(shipping => Ok(views.html.shipping(shipping)))
  }

  def removeShipping(id: Long): Action[AnyContent] = Action {
    shippingRepository.delete(id)
    Redirect("/getshippings")
  }

  def addShipping(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val user = userRepository.list()
    user.map(user => Ok(views.html.shippingadd(shippingForm, user)))
  }

  def addShippingHandle(): Action[AnyContent] = Action.async { implicit request =>
    var usr: Seq[User] = Seq[User]()
    userRepository.list().onComplete {
      case Success(cat) => usr = cat
      case Failure(_) => print("fail")
    }

    shippingForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.shippingadd(errorForm, usr))
        )
      },
      shipping => {
        shippingRepository.create(shipping.user, shipping.address,shipping.typeOf).map { _ =>
          Redirect(routes.ShippingController.addShipping()).flashing("success" -> "user created")
        }
      }
    )

  }

  def addShippingJson(): Action[AnyContent] = Action.async { implicit request =>
    val user = request.body.asJson.get("user").as[Long]
    val address = request.body.asJson.get("address").as[String]
    val typeOf = request.body.asJson.get("typeOf").as[Int]

    shippingRepository.create(user, address, typeOf).map { shipping =>
      Ok(Json.toJson(shipping))
    }
  }

  def updateShippingJson(id: Long): Action[AnyContent] = Action.async { implicit request =>

    val user = request.body.asJson.get("user").as[Long]
    val address = request.body.asJson.get("address").as[String]
    val typeOf = request.body.asJson.get("typeOf").as[Int]

    shippingRepository.update(id, Shipping(id, user, address, typeOf)).map { _ =>
      Ok("Updated Shipping")
    }
  }

  def getShippingJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    shippingRepository.getById(id).map { shipping =>
      Ok(Json.toJson(shipping))
    }
  }

  def getShippingByUserJson(userId: Long): Action[AnyContent] = Action.async { implicit request =>
    shippingRepository.getByUser(userId).map { shipping =>
      Ok(Json.toJson(shipping))
    }
  }

  def getShippingsJson: Action[AnyContent] = Action.async { implicit request =>
    shippingRepository.list().map { shipping =>
      Ok(Json.toJson(shipping))
    }
  }

  def removeShippingJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    shippingRepository.delete(id).map { _ =>
      Ok("Shipping removed")
    }
  }

}

case class CreateShippingForm(user: Long, address: String, typeOf: Int)
