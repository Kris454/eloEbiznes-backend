package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductCommentRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import productRepository.ProductTable
  import dbConfig._
  import profile.api._

  private class ProductCommentTable(tag: Tag) extends Table[ProductComment](tag, "comment") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def product = column[Long]("product")

    def productFk = foreignKey("product_fk", product, bk)(_.id)

    def comment = column[String]("comment")

    def * = (id, product, comment) <> ((ProductComment.apply _).tupled, ProductComment.unapply)
  }

  private val bkComment = TableQuery[ProductCommentTable]
  private val bk = TableQuery[ProductTable]


  def create(product: Long, comment: String): Future[ProductComment] = db.run {
    (bkComment.map(b => (b.product, b.comment))
      returning bkComment.map(_.id)
      into { case ((product, comment), id) => ProductComment(id, product, comment) }
      ) += (product, comment)
  }

  def list(): Future[Seq[ProductComment]] = db.run {
    bkComment.result
  }

  def getById(id: Long): Future[ProductComment] = db.run {
    bkComment.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Unit] = db.run(bkComment.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newProductComment: ProductComment): Future[Unit] = {
    val productCommentToUpdate: ProductComment = newProductComment.copy(id)
    db.run(bkComment.filter(_.id === id).update(productCommentToUpdate)).map(_ => ())
  }
}
