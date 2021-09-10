package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductGradeRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import productRepository.ProductTable
  import dbConfig._
  import profile.api._

  private class ProductGradeTable(tag: Tag) extends Table[ProductGrade](tag, "rate") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def product = column[Long]("product")

    def productFk = foreignKey("product_fk", product, bk)(_.id)

    def grade = column[Int]("grade")

    def * = (id, product, grade) <> ((ProductGrade.apply _).tupled, ProductGrade.unapply)
  }

  private val bkGrade = TableQuery[ProductGradeTable]
  private val bk = TableQuery[ProductTable]


  def create(product: Long, grade: Int): Future[ProductGrade] = db.run {
    (bkGrade.map(b => (b.product, b.grade))
      returning bkGrade.map(_.id)
      into { case ((product, grade), id) => ProductGrade(id, product, grade) }
      ) += (product, grade)
  }

  def list(): Future[Seq[ProductGrade]] = db.run {
    bkGrade.result
  }

  def getById(id: Long): Future[ProductGrade] = db.run {
    bkGrade.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Unit] = db.run(bkGrade.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newProductGrade: ProductGrade): Future[Unit] = {
    val productGradeToUpdate: ProductGrade = newProductGrade.copy(id)
    db.run(bkGrade.filter(_.id === id).update(productGradeToUpdate)).map(_ => ())
  }
}
