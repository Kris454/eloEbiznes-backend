package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, val categoryRepository: CategoryRepository)(implicit ec: ExecutionContext) {

  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class ProductTable(tag: Tag) extends Table[Product](tag, "product") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def description = column[String]("description")

    def category = column[Int]("category")
    
    def price = column[Int]("price")

    def categoryFk = foreignKey("cat_fk", category, cat)(_.id)

    def * = (id, name, description, category, price) <> ((Product.apply _).tupled, Product.unapply)
  }

  import categoryRepository.CategoryTable

  val book = TableQuery[ProductTable]
  val cat = TableQuery[CategoryTable]


  def create(name: String, description: String, category: Int, price: Int): Future[Product] = db.run {
    (book.map(b => (b.name, b.description, b.category, b.price))
      returning book.map(_.id)
      into { case ((name, description, category, price), id) => Product(id, name, description, category, price) }
      ) += (name, description, category, price)
  }

  def list(): Future[Seq[Product]] = db.run {
    book.result
  }

  def getByCategory(categoryId: Int): Future[Seq[Product]] = db.run {
    book.filter(_.category === categoryId).result
  }

  def getById(id: Long): Future[Product] = db.run {
    book.filter(_.id === id).result.head
  }

  def getByIdOption(id: Long): Future[Option[Product]] = db.run {
    book.filter(_.id === id).result.headOption
  }

  def getByCategories(categoryIds: List[Int]): Future[Seq[Product]] = db.run {
    book.filter(_.category inSet categoryIds).result
  }

  def delete(id: Long): Future[Unit] = db.run(book.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newProduct: Product): Future[Unit] = {
    val productToUpdate: Product = newProduct.copy(id)
    db.run(book.filter(_.id === id).update(productToUpdate)).map(_ => ())
  }
}
