package shared

/**
  * Created by gurghet on 17.04.16.
  */

import collection.mutable

case class Shift(order: Int,
                 team: mutable.Seq[String],
                 preferences: mutable.Map[String, Int],
                 properties: mutable.Seq[String])
case class Day(ofTheMonth: Int, shifts: mutable.Seq[Shift])