package models

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordInfo
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.reflect.ClassTag
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext, implicit val classTag: ClassTag[PasswordInfo]) extends IdentityService[User]{
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  case class UserDto(id: Long, providerId: String, providerKey: String, email: String, firstName: String, lastName: String, login: String)

  class UserTable(tag: Tag) extends Table[UserDto](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def providerId = column[String]("providerId")

    def providerKey = column[String]("providerKey")

    def email = column[String]("email")

    def firstName = column[String]("firstName")

    def lastName = column[String]("lastName")

    def login = column[String]("login")

    def * = (id, providerId,providerKey,email,firstName, lastName, login) <> ((UserDto.apply _).tupled, UserDto.unapply)
  }

  val user = TableQuery[UserTable]

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = db.run {
    user.filter(_.providerId === loginInfo.providerID)
      .filter(_.providerKey === loginInfo.providerKey)
      .result
      .headOption
  }.map(_.map(dto => toModel(dto)))

  def list(): Future[Seq[User]] = db.run {
    user.result
  }.map(_.map(dto => toModel(dto)))

  def create(providerId: String, providerKey: String, email: String, firstName: String, lastName: String, login: String): Future[User] = db.run {
    (user.map(c => (c.providerId, c.providerKey, c.email, c.firstName, c.lastName, c.login))
      returning user.map(_.id)
      into { case ((providerId, providerKey, email, firstName, lastName, login), id) => UserDto(id, providerId, providerKey, email,firstName, lastName, login) }
      ) += (providerId, providerKey, email, firstName, lastName, login)
  }.map(dto => toModel(dto))

  def getById(id: Long): Future[User] = db.run {
    user.filter(_.id === id).result.head
  }.map(dto => toModel(dto))

  def delete(id: Long): Future[Unit] = db.run(user.filter(_.id === id).delete).map(_ => ())

  def update(id: Long, newUser: User): Future[Unit] = {
    val userToUpdate: User = newUser.copy(id)
    db.run(user.filter(_.id === id).update(toDto(userToUpdate))).map(_ => userToUpdate)
  }

  private def toModel(dto: UserDto): User =
    User(dto.id, LoginInfo(dto.providerId, dto.providerKey), dto.email, dto.firstName, dto.lastName, dto.login)

  private def toDto(model: User): UserDto =
    UserDto(model.id, model.loginInfo.providerID, model.loginInfo.providerKey, model.email, model.firstName, model.lastName, model.login)
}
