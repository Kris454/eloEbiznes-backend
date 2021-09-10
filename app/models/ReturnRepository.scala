package models

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnRepository @Inject()(dbConfigProvider: DatabaseConfigProvider, userRepository: UserRepository)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._
  import userRepository.UserTable

  private class ReturnTable(tag: Tag) extends Table[Return](tag, "return") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def user = column[Long]("user")

    def userFk = foreignKey("user_fk", user, usr)(_.id)

    def * = (id, user) <> ((Return.apply _).tupled, Return.unapply)
  }

  private val rtrn = TableQuery[ReturnTable]
  private val usr = TableQuery[UserTable]


  def create(user: Long): Future[Return] = db.run {
    (rtrn.map(b => (b.user))
      returning rtrn.map(_.id)
      into { case ((user), id) => Return(id, user) }
      ) += (user)
  }

  def list(): Future[Seq[Return]] = db.run {
    rtrn.result
  }

  def getById(id: Long): Future[Return] = db.run {
    rtrn.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Unit] = db.run(rtrn.filter(_.id === id).delete).map(_ => ())

}
