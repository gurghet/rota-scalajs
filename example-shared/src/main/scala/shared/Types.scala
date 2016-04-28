package shared

/**
  * Created by gurghet on 17.04.16.
  */

import collection.mutable
import upickle.json._
import upickle.default._

case class MutableDay(ofTheMonth: Int, shifts: mutable.Seq[MutableShift])

case class MutableShift(order: Int,
                        team: mutable.Seq[String],
                        preferences: mutable.Map[String, Int],
                        properties: mutable.Seq[String])

case class Shift(order: Int,
                 team: Seq[String],
                 preferences: Map[String, Int],
                 properties: Seq[String])

case class Day(ofTheMonth: Int, shifts: Seq[Shift])

object RotaUtils {
  def freezeShift(shift: MutableShift): Shift = {
    Shift(shift.order, shift.team, shift.preferences.toMap, shift.properties)
  }

  def freezeDay(day: MutableDay): Day = {
    Day(day.ofTheMonth, day.shifts.map(freezeShift))
  }
}


case class JsRota(id: Option[Long], jsonRota: String)

object JsRota {
  val tupled: ((Option[Long], String)) => JsRota = { case (maybeId, days) => JsRota(maybeId, days) }

  import RotaUtils._
  def apply(id: Option[Long], days: Seq[MutableDay]): JsRota = {
    JsRota(id, upickle.json.write(upickle.default.writeJs(days.map(freezeDay))))
  }
  def jsRota2SeqDays(jsRota: JsRota): Seq[Day] = {
    upickle.default.read[Seq[Day]](jsRota.jsonRota)
  }
}