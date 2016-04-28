package shared

/**
  * Created by gurghet on 17.04.16.
  */

import collection.mutable
import upickle.json._
import upickle.default._

case class Day(ofTheMonth: Int, shifts: mutable.Seq[Shift])

case class Shift(order: Int,
                 team: mutable.Seq[String],
                 preferences: mutable.Map[String, Int],
                 properties: mutable.Seq[String])

case class ImmutableShift(order: Int,
                          team: Seq[String],
                          preferences: Map[String, Int],
                          properties: Seq[String])

case class ImmutableDay(ofTheMonth: Int, shifts: Seq[ImmutableShift])

object RotaUtils {
  def freezeShift(shift: Shift): ImmutableShift = {
    ImmutableShift(shift.order, shift.team, shift.preferences.toMap, shift.properties)
  }

  def freezeDay(day: Day): ImmutableDay = {
    ImmutableDay(day.ofTheMonth, day.shifts.map(freezeShift))
  }
}


case class JsRota(id: Long, jsonRota: String)

object JsRota {
  import RotaUtils._
  def apply(id: Long, days: Seq[Day]): JsRota = {
    JsRota(id, upickle.json.write(upickle.default.writeJs(days.map(freezeDay))))
  }
  def jsRota2SeqDays(jsRota: JsRota): Seq[Day] = {

  }
}