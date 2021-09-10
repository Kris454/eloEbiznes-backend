package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CartRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, userRepository: UserRepository)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._
  import userRepository.UserTable

  private class CartTable(tag: Tag) extends Table[Cart](tag, "cart") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def user = column[Long]("user")

    def userFk = foreignKey("user_fk", user, usr)(_.id)

    def * = (id, user) <> ((Cart.apply _).tupled, Cart.unapply)
  }

  private val cart = TableQuery[CartTable]
  private val usr = TableQuery[UserTable]


  def list(): Future[Seq[Cart]] = db.run {
    cart.result
  }

  def getByUser(userId: Long): Future[Seq[Cart]] = db.run {
    cart.filter(_.user === userId).result
  }

  def create(user: Long): Future[Cart] = db.run {
    (cart.map(c => (c.user))
      returning cart.map(_.id)
      into { case ((user), id) => Cart(id, user) }
      ) += (user)
  }

  def delete(id: Long): Future[Unit] = db.run(cart.filter(_.id === id).delete).map(_ => ())

}
