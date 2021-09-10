package controllers

import models.{CartRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class CartController @Inject()(cc: MessagesControllerComponents, cartRepository: CartRepository,
                               userRepository: UserRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val cartForm: Form[CreateCartForm] = Form {
    mapping(
      "user" -> longNumber,
    )(CreateCartForm.apply)(CreateCartForm.unapply)
  }

  def getCarts: Action[AnyContent] = Action.async { implicit request =>
    val fetchedCarts = cartRepository.list()
    fetchedCarts.map(cart => Ok(views.html.carts(cart)))
  }

  def removeCart(id: Long): Action[AnyContent] = Action {
    cartRepository.delete(id)
    Redirect("/getCarts")
  }

  def addCart(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = userRepository.list()
    users.map(user => Ok(views.html.cartadd(cartForm, user)))
  }

  def addCartHandle(): Action[AnyContent] = Action.async { implicit request =>
    var usr: Seq[User] = Seq[User]()
    userRepository.list().onComplete {
      case Success(cat) => usr = cat
      case Failure(_) => print("fail")
    }

    cartForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.cartadd(errorForm, usr))
        )
      },
      cart => {
        cartRepository.create(cart.user).map { _ =>
          Redirect(routes.CartController.addCart()).flashing("success" -> "cart created")
        }
      }
    )

  }

  def addCartJson(): Action[AnyContent] = Action.async { implicit request =>
    val user = request.body.asJson.get("category").as[Long]

    cartRepository.create(user).map { cart =>
      Ok(Json.toJson(cart))
    }
  }


  def getCartJson(userId: Long): Action[AnyContent] = Action.async { implicit request =>
    cartRepository.getByUser(userId).map { cart =>
      Ok(Json.toJson(cart))
    }
  }

  def getCartsJson: Action[AnyContent] = Action.async { implicit request =>
    cartRepository.list().map { carts =>
      Ok(Json.toJson(carts))
    }
  }

  def removeCartJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    cartRepository.delete(id).map { _ =>
      Ok("Cart removed")
    }
  }

}

case class CreateCartForm(user: Long)
