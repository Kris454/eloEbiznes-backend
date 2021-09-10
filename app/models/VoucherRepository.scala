package models


import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VoucherRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class VoucherTable(tag: Tag) extends Table[Voucher](tag, "voucher") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def value = column[Int]("value")

    def * = (id, name, value) <> ((Voucher.apply _).tupled, Voucher.unapply)
  }

  private val voucher = TableQuery[VoucherTable]

  def create(name: String,value: Int): Future[Voucher] = db.run {
    (voucher.map(b => (b.name, b.value))
      returning voucher.map(_.id)
      into { case ((name, value), id) => Voucher(id, name, value) }
      ) += (name, value)
  }

  def list(): Future[Seq[Voucher]] = db.run {
    voucher.result
  }

  def getById(id: Long): Future[Voucher] = db.run {
    voucher.filter(_.id === id).result.head
  }

  def delete(id: Long): Future[Unit] = db.run(voucher.filter(_.id === id).delete).map(_ => ())
}