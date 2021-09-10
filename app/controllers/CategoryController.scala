package controllers

import models.CategoryRepository
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CategoryController @Inject()(cc: MessagesControllerComponents, categoryRepository: CategoryRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  val categoryForm: Form[CreateCategoryForm] = Form {
    mapping(
      "name" -> nonEmptyText,
    )(CreateCategoryForm.apply)(CreateCategoryForm.unapply)
  }

  def getCategories: Action[AnyContent] = Action.async { implicit request =>
    val fetchedCategories = categoryRepository.list()
    fetchedCategories.map(categories => Ok(views.html.categories(categories)))
  }

  def removeCategory(id: Int): Action[AnyContent] = Action {
    categoryRepository.delete(id)
    Redirect("/getcategories")
  }

  def addCategory(): Action[AnyContent] = Action.async { implicit request: MessagesRequest[AnyContent] =>
    val categories = categoryRepository.list()
    categories.map(cat => Ok(views.html.Formcategory(categoryForm)))
  }

  def addCategoryHandle(): Action[AnyContent] = Action.async { implicit request =>
    categoryForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.Formcategory(errorForm))
        )
      },
      category => {
        categoryRepository.create(category.name).map { _ =>
          Redirect(routes.CategoryController.addCategory()).flashing("success" -> "category created")
        }
      }
    )

  }

  def addCategoryJson(): Action[AnyContent] = Action.async { implicit request =>
    val name = request.body.asJson.get("name").as[String]

    categoryRepository.create(name).map { category =>
      Ok(Json.toJson(category))
    }
  }

  def getCategoriesJson: Action[AnyContent] = Action.async { implicit request =>
    categoryRepository.list().map { category =>
      Ok(Json.toJson(category))
    }
  }

  def removeCategoryJson(id: Int): Action[AnyContent] = Action.async { implicit request =>
    categoryRepository.delete(id).map { _ =>
      Ok("Category removed")
    }
  }

}

case class CreateCategoryForm(name: String)
