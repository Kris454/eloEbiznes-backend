package controllers

import models.{Order, OrderRepository, User, UserRepository}
import play.api.data.Form
import play.api.data.Forms.{longNumber, mapping, number}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class OrderController @Inject()(cc: MessagesControllerComponents, orderRepository: OrderRepository, userRepository: UserRepository)
                               (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val orderForm: Form[CreateOrderForm] = Form {
    mapping(
      "user" -> longNumber,
      "price" -> number
    )(CreateOrderForm.apply)(CreateOrderForm.unapply)
  }

  def getOrders: Action[AnyContent] = Action.async { implicit request =>
    val fetchedOrders = orderRepository.list()
    fetchedOrders.map(order => Ok(views.html.orders(order)))
  }

  def removeOrder(id: Long): Action[AnyContent] = Action {
    orderRepository.delete(id)
    Redirect("/getorders")
  }

  def addOrder(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val users = userRepository.list()
    users.map(user => Ok(views.html.orderadd(orderForm, user)))
  }

  def addOrderHandle(): Action[AnyContent] = Action.async { implicit request =>
    var usr: Seq[User] = Seq[User]()
    userRepository.list().onComplete {
      case Success(cat) => usr = cat
      case Failure(_) => print("fail")
    }

    orderForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.orderadd(errorForm, usr))
        )
      },
      order => {
        orderRepository.create(order.user, order.price).map { _ =>
          Redirect(routes.OrderController.addOrder()).flashing("success" -> "order created")
        }
      }
    )

  }

  def addOrderJson(): Action[AnyContent] = Action.async { implicit request =>
    val user = request.body.asJson.get("user").as[Long]
    val price = request.body.asJson.get("price").as[Int]

    orderRepository.create(user, price).map { order =>
      Ok(Json.toJson(order))
    }
  }

  def updateOrderJson(id: Long): Action[AnyContent] = Action.async { implicit request =>

    val user = request.body.asJson.get("user").as[Long]
    val price = request.body.asJson.get("price").as[Int]

    orderRepository.update(id, Order(id, user, price)).map { _ =>
      Ok("Updated order")
    }
  }

  def getOrderJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    orderRepository.getById(id).map { order =>
      Ok(Json.toJson(order))
    }
  }

  def getOrderByUserJson(userId: Long): Action[AnyContent] = Action.async { implicit request =>
    orderRepository.getByUser(userId).map { order =>
      Ok(Json.toJson(order))
    }
  }

  def getOrdersJson: Action[AnyContent] = Action.async { implicit request =>
    orderRepository.list().map { orders =>
      Ok(Json.toJson(orders))
    }
  }

  def removeOrderJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    orderRepository.delete(id).map { _ =>
      Ok("Order removed")
    }
  }

}

case class CreateOrderForm(user: Long, price: Int)
