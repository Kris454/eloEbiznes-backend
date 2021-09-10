package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShippingRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, userRepository: UserRepository)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._
  import userRepository.UserTable

  private class ShippingTable(tag: Tag) extends Table[Shipping](tag, "shipping") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def user = column[Long]("user")

    def userFk = foreignKey("user_fk", user, usr)(_.id)

    def address = column[String]("address")

    def typeOf = column[Int]("typeOf")

    def * = (id, user, address, typeOf) <> ((Shipping.apply _).tupled, Shipping.unapply)
  }

  private val shipping = TableQuery[ShippingTable]
  private val usr = TableQuery[UserTable]


  def create(user: Long, address: String, typeOf: Int): Future[Shipping] = db.run {
    (shipping.map(b => (b.user, b.address, typeOf))
      returning shipping.map(_.id)
      into { case ((user, address, typeOf), id) => Shipping(id, user, address, typeOf) }
      ) += (user, address, typeOf)
  }

  def list(): Future[Seq[Shipping]] = db.run {
    shipping.result
  }

  def getById(id: Long): Future[Shipping] = db.run {
    shipping.filter(_.id === id).result.head
  }

  def getByUser(userId: Long): Future[Seq[Shipping]] = db.run {
    shipping.filter(_.user === userId).result
  }

  def delete(id: Long): Future[Unit] = db.run(shipping.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newBookReview: Shipping): Future[Unit] = {
    val bookReviewToUpdate: Shipping = newBookReview.copy(id)
    db.run(shipping.filter(_.id === id).update(bookReviewToUpdate)).map(_ => ())
  }
}
