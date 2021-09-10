package controllers

import models.{Product, ProductRepository, Category, CategoryRepository}
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ProductController @Inject()(productRepository: ProductRepository, categoryRepo: CategoryRepository, cc: MessagesControllerComponents)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val productForm: Form[CreateProductForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> number,
      "price" -> number,
    )(CreateProductForm.apply)(CreateProductForm.unapply)
  }

  val updateProductForm: Form[UpdateProductForm] = Form {
    mapping(
      "id" -> longNumber,
      "name" -> nonEmptyText,
      "description" -> nonEmptyText,
      "category" -> number,
      "price" -> number,
    )(UpdateProductForm.apply)(UpdateProductForm.unapply)
  }

  def getProducts: Action[AnyContent] = Action.async { implicit request =>
    val fetchedProducts = productRepository.list()
    fetchedProducts.map(products => Ok(views.html.products(products)))
  }

  def getProduct(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val product = productRepository.getByIdOption(id)
    product.map {
      case Some(p) => Ok(views.html.product(p))
      case None => Redirect(routes.ProductController.getProducts())
    }
  }

  def removeProduct(id: Long): Action[AnyContent] = Action {
    productRepository.delete(id)
    Redirect("/getproducts")
  }

  def updateProduct(id: Long): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    var categ: Seq[Category] = Seq[Category]()
    categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    val produkt = productRepository.getById(id)
    produkt.map(product => {
      val prodForm = updateProductForm.fill(UpdateProductForm(product.id, product.name, product.description, product.category, product.price))
      Ok(views.html.productupdate(prodForm, categ))
    })
  }

  def updateProductHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    updateProductForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.productupdate(errorForm, categ))
        )
      },
      product => {
        productRepository.update(product.id, Product(product.id, product.name, product.description, product.category, product.price)).map { _ =>
          Redirect(routes.ProductController.updateProduct(product.id)).flashing("success" -> "product updated")
        }
      }
    )

  }

  def addProduct(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepo.list()
    categories.map(cat => Ok(views.html.Formproduct(productForm, cat)))
  }

  def addProductHandle(): Action[AnyContent] = Action.async { implicit request =>
    var categ: Seq[Category] = Seq[Category]()
    categoryRepo.list().onComplete {
      case Success(cat) => categ = cat
      case Failure(_) => print("fail")
    }

    productForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.Formproduct(errorForm, categ))
        )
      },
      product => {
        productRepository.create(product.name, product.description, product.category, product.price).map { _ =>
          Redirect(routes.ProductController.addProduct()).flashing("success" -> "product created")
        }
      }
    )

  }


  def addProductJson(): Action[AnyContent] = Action.async { implicit request =>
    val productName = request.body.asJson.get("name").as[String]
    val productDescription = request.body.asJson.get("description").as[String]
    val productCategory = request.body.asJson.get("category").as[Int]
    val productPrice = request.body.asJson.get("price").as[Int]
    
    productRepository.create(productName, productDescription, productCategory, productPrice).map { product =>
      Ok(Json.toJson(product))
    }
  }

  def updateProductJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    val productName = request.body.asJson.get("name").as[String]
    val productDescription = request.body.asJson.get("description").as[String]
    val productCategory = request.body.asJson.get("category").as[Int]
    val productPrice = request.body.asJson.get("price").as[Int]
    
    productRepository.update(id, Product(id, productName, productDescription, productCategory, productPrice)).map {_ =>
      Ok("Product updated")
    }
  }

  def getProductJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productRepository.getById(id).map { product =>
      Ok(Json.toJson(product))
    }
  }

  def getProductsJson: Action[AnyContent] = Action.async { implicit request =>
    productRepository.list().map { products =>
      Ok(Json.toJson(products))
    }
  }

  def removeProductJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    productRepository.delete(id).map { _ =>
      Ok("Product removed")
    }
  }

}

case class CreateProductForm(name: String, description: String, category: Int, price: Int)

case class UpdateProductForm(id: Long, name: String, description: String, category: Int, price: Int)
