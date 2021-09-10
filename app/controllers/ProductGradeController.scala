package controllers

import models.{Product, ProductRepository, ProductGrade, ProductGradeRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProductGradeController @Inject()(cc: MessagesControllerComponents, productGradeRepository: ProductGradeRepository,
                                     productRepository: ProductRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val productGradeForm: Form[CreateProductGradeForm] = Form {
    mapping(
      "product" -> number,
      "grade" -> number,
    )(CreateProductGradeForm.apply)(CreateProductGradeForm.unapply)
  }

  val updateProductGradeForm: Form[UpdateProductGradeForm] = Form {
    mapping(
      "id" -> longNumber,
      "product" -> longNumber,
      "grade" -> number,
    )(UpdateProductGradeForm.apply)(UpdateProductGradeForm.unapply)
  }

  def getProductGrades: Action[AnyContent] = Action.async { implicit request =>
    val fetchedProductGrades = productGradeRepository.list()
    fetchedProductGrades.map(productGrades => Ok(views.html.productgrades(productGrades)))
  }

  def removeProductGrade(id: Long): Action[AnyContent] = Action {
    productGradeRepository.delete(id)
    Redirect("/getproductgrades")
  }

  def addProductGrade(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val products = productRepository.list()
    products.map(product => Ok(views.html.productgradeadd(productGradeForm, product)))
  }

  def addProductGradeHandle(): Action[AnyContent] = Action.async { implicit request =>
    var product: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete {
      case Success(bk) => product = bk
      case Failure(_) => print("fail")
    }

    productGradeForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productgradeadd(errorForm, product))
        )
      },
      productGrade => {
        productGradeRepository.create(productGrade.product, productGrade.grade).map { _ =>
          Redirect(routes.ProductGradeController.addProductGrade()).flashing("success" -> "product grade created")
        }
      }
    )
  }

  def updateProductGrade(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var product: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete {
      case Success(bk) => product = bk
      case Failure(_) => print("fail")
    }

    val produkt = productGradeRepository.getById(id)
    produkt.map(productGrade => {
      val prodForm = updateProductGradeForm.fill(UpdateProductGradeForm(productGrade.id, productGrade.product, productGrade.grade))
      Ok(views.html.productgradeupdate(prodForm, product))
    })
  }

  def updateProductGradeHandle(): Action[AnyContent] = Action.async { implicit request =>
    var product: Seq[Product] = Seq[Product]()
    productRepository.list().onComplete {
      case Success(bk) => product = bk
      case Failure(_) => print("fail")
    }

    updateProductGradeForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productgradeupdate(errorForm, product))
        )
      },
      productGrade => {
        productGradeRepository.update(productGrade.id, ProductGrade(productGrade.id, productGrade.product, productGrade.grade)).map { _ =>
          Redirect(routes.ProductGradeController.updateProductGrade(productGrade.id)).flashing("success" -> "product updated")
        }
      }
    )

  }

  def addGradeJson(): Action[AnyContent] = Action.async { implicit request =>
    val product = request.body.asJson.get("product").as[Long]
    val grade = request.body.asJson.get("grade").as[Int]

    productGradeRepository.create(product, grade).map { productGrade =>
      Ok(Json.toJson(productGrade))
    }
  }

  def updateGradeJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val product = request.body.asJson.get("product").as[Long]
    val grade = request.body.asJson.get("grade").as[Int]

    productGradeRepository.update(id, ProductGrade(id, product, grade)).map { productGrade =>
      Ok("Product grade updated")
    }
  }

  def getGradeJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productGradeRepository.getById(id).map { productGrade =>
      Ok(Json.toJson(productGrade))
    }
  }

  def getGradesJson: Action[AnyContent] = Action.async { implicit request =>
    productGradeRepository.list().map { productGrades =>
      Ok(Json.toJson(productGrades))
    }
  }

  def removeGradeJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productGradeRepository.delete(id).map { _ =>
      Ok("Product grade removed")
    }
  }

}

case class CreateProductGradeForm(product: Int, grade: Int)

case class UpdateProductGradeForm(id: Long, product: Long, grade: Int)
