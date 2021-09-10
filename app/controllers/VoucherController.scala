package controllers

import models.VoucherRepository
import play.api.data.Form
import play.api.data.Forms.{mapping, nonEmptyText, number}
import play.api.libs.json.Json
import play.api.mvc._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VoucherController @Inject()(cc: MessagesControllerComponents, voucherRepository: VoucherRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {


  val voucherForm: Form[CreateVoucherForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "value" -> number,
    )(CreateVoucherForm.apply)(CreateVoucherForm.unapply)
  }

  def getVouchers: Action[AnyContent] = Action.async { implicit request =>
    val fetchedVouchers = voucherRepository.list()
    fetchedVouchers.map(vouchers => Ok(views.html.vouchers(vouchers)))
  }

  def removeVoucher(id: Long): Action[AnyContent] = Action {
    voucherRepository.delete(id)
    Redirect("/getdiscountcoupons")
  }

  def addVoucher(): Action[AnyContent] = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.voucheradd(voucherForm))
  }

  def addVoucherHandle(): Action[AnyContent] = Action.async { implicit request =>
    voucherForm.bindFromRequest().fold(
      errorForm => {
        Future.successful(
          BadRequest(views.html.voucheradd(errorForm))
        )
      },
      voucher => {
        voucherRepository.create(voucher.name, voucher.value).map { _ =>
          Redirect(routes.VoucherController.addVoucher()).flashing("success" -> "voucher created")
        }
      }
    )

  }

  def addVoucherJson(): Action[AnyContent] = Action.async { implicit request =>
    val name = request.body.asJson.get("name").as[String]
    val value = request.body.asJson.get("value").as[Int]

    voucherRepository.create(name, value).map { product =>
      Ok(Json.toJson(product))
    }
  }

  def getVoucherJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    voucherRepository.getById(id).map { voucher =>
      Ok(Json.toJson(voucher))
    }
  }

  def getVouchersJson: Action[AnyContent] = Action.async { implicit request =>
    voucherRepository.list().map { vouchers =>
      Ok(Json.toJson(vouchers))
    }
  }

  def removeVoucherJson(id: Long): Action[AnyContent] = Action.async { implicit request =>
    voucherRepository.delete(id).map { _ =>
      Ok("Discount coupon removed")
    }
  }

}

case class CreateVoucherForm(name: String, value: Int)
