package shared

/**
  * Created by gurghet on 17.04.16.
  */

import collection.mutable

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