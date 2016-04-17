package shared

/**
  * Created by gurghet on 17.04.16.
  */

case class WorkerId(id: Int)
case class Shift(order: Int,
                 team: Option[List[WorkerId]] = Option.empty,
                 properties: Option[List[String]] = Option.empty)
case class Day(ofTheMonth: Int, shifts: List[Shift])