package models

import play.api.libs.json.{Json, OFormat}

case class Voucher(id: Long, name: String, value: Int)

object Voucher {
  implicit val voucherFormat: OFormat[Voucher] = Json.format[Voucher]
}
