package controllers

import models.{Product, ProductRepository, ProductComment, ProductCommentRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProductCommentController @Inject()(cc: MessagesControllerComponents, productCommentRepository: ProductCommentRepository,
                                     productRepository: ProductRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val productCommentForm: Form[CreateProductCommentForm] = Form {
    mapping(
      "product" -> number,
      "comment" -> nonEmptyText,
    )(CreateProductCommentForm.apply)(CreateProductCommentForm.unapply)
  }

  val updateProductCommentForm: Form[UpdateProductCommentForm] = Form {
    mapping(
      "id" -> longNumber,
      "product" -> longNumber,
      "comment" -> nonEmptyText,
    )(UpdateProductCommentForm.apply)(UpdateProductCommentForm.unapply)
  }

  def getProductComments: Action[AnyContent] = Action.async { implicit request =>
    val fetchedProductComments = productCommentRepository.list()
    fetchedProductComments.map(productComments => Ok(views.html.productcomments(productComments)))
  }

  def removeProductComment(id: Long): Action[AnyContent] = Action {
    productCommentRepository.delete(id)
    Redirect("/getproductcomments")
  }

  def addProductComment(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val products = productRepository.list()
    products.map(product => Ok(views.html.productcommentadd(productCommentForm, product)))
  }

  def addProductCommentHandle(): Action[AnyContent] = Action.async { implicit request =>
    var product: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete {
      case Success(bk) => product = bk
      case Failure(_) => print("fail")
    }

    productCommentForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productcommentadd(errorForm, product))
        )
      },
      productComment => {
        productCommentRepository.create(productComment.product, productComment.comment).map { _ =>
          Redirect(routes.ProductCommentController.addProductComment()).flashing("success" -> "product comment created")
        }
      }
    )
  }

  def updateProductComment(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var product: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete {
      case Success(bk) => product = bk
      case Failure(_) => print("fail")
    }

    val produkt = productCommentRepository.getById(id)
    produkt.map(productComment => {
      val prodForm = updateProductCommentForm.fill(UpdateProductCommentForm(productComment.id, productComment.product, productComment.comment))
      Ok(views.html.productcommentupdate(prodForm, product))
    })
  }

  def updateProductCommentHandle(): Action[AnyContent] = Action.async { implicit request =>
    var product: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete {
      case Success(bk) => product = bk
      case Failure(_) => print("fail")
    }

    updateProductCommentForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productcommentupdate(errorForm, product))
        )
      },
      productComment => {
        productCommentRepository.update(productComment.id, ProductComment(productComment.id, productComment.product, productComment.comment)).map { _ =>
          Redirect(routes.ProductCommentController.updateProductComment(productComment.id)).flashing("success" -> "product updated")
        }
      }
    )

  }

  def addCommentJson(): Action[AnyContent] = Action.async { implicit request =>
    val product = request.body.asJson.get("product").as[Long]
    val comment = request.body.asJson.get("comment").as[String]

    productCommentRepository.create(product, comment).map { productComment =>
      Ok(Json.toJson(productComment))
    }
  }

  def updateCommentJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val product = request.body.asJson.get("product").as[Long]
    val comment = request.body.asJson.get("comment").as[String]

    productCommentRepository.update(id, ProductComment(id, product, comment)).map { productComment =>
      Ok("Product comment updated")
    }
  }

  def getCommentJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productCommentRepository.getById(id).map { productComment =>
      Ok(Json.toJson(productComment))
    }
  }

  def getCommentsJson: Action[AnyContent] = Action.async { implicit request =>
    productCommentRepository.list().map { productComments =>
      Ok(Json.toJson(productComments))
    }
  }

  def removeCommentJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productCommentRepository.delete(id).map { _ =>
      Ok("Product comment removed")
    }
  }

}

case class CreateProductCommentForm(product: Int, comment: String)

case class UpdateProductCommentForm(id: Long, product: Long, comment: String)
