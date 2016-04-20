package example

import com.felstar.scalajs.vue.Vue
import org.scalajs.dom.ext.{Ajax, AjaxException}
import upickle.Js
import upickle.Js.Value
import scala.concurrent.ExecutionContext.Implicits.global

import scala.scalajs.js
import org.scalajs.dom
import js.Dynamic.literal
import shared.{WorkerId, Shift, Day, SharedMessages}
import scala.scalajs.js.ThisFunction
import scala.scalajs.js.annotation.{JSExport, JSName, ScalaJSDefined}
import scalatags.Text.all._
import js.JSConverters._

object ExampleJS extends js.JSApp {
  @js.native
  trait CheckboxGroup extends Vue {
    var style: String = js.native
    var value: js.Array[String] = js.native
  }

  @js.native
  trait CheckboxBtn extends Vue {
    override val $parent: CheckboxGroup = js.native
    var value: String = js.native
    var checked: Boolean = js.native
    def style: String = js.native
  }

  type JSWorkerId = Int
  type JSProperties = js.Array[String]
  type JSTeam = js.Array[JSWorkerId]

  @js.native
  trait JsShift extends js.Object {
    val order: Int = js.native
    var team: JSTeam = js.native
    var properties: JSProperties = js.native
    //def toShift: Shift = Shift(order, Some(team.toList.map(WorkerId)), Some(properties.toList))
  }

  @js.native
  trait JsDay extends js.Object {
    val ofTheMonth: Int = js.native
    val shifts: js.Array[JsShift] = js.native
    //def toDay: Day = Day(ofTheMonth, shifts.toList.map(_.toShift))
  }

  object DayImplicits {
    implicit class DayObjOps(val self: JsDay.type) extends AnyVal {
      def apply(ofTheMonth: Int,
                shifts: List[JsShift]): JsDay = {
        val jsShifts = shifts.toJSArray
        JsDay(ofTheMonth, jsShifts)
      }
    }
  }

  object JsDay {
    def apply(ofTheMonth: Int, jsShifts: js.Array[JsShift]): JsDay = {
      literal(ofTheMonth = ofTheMonth, shifts = jsShifts).asInstanceOf[JsDay]
    }
  }

  object ShiftImplicits {
    implicit class ShiftObjOps(val self: JsShift.type) extends AnyVal {
      def apply(order: Int,
                maybeTeam: Option[List[WorkerId]] = Option.empty,
                maybeProperties: Option[List[String]] = Option.empty): JsShift = {
        val jsTeam = if (maybeTeam.isDefined) {
          maybeTeam.get.map(_.id).toJSArray
        } else {
          js.Array(): JSTeam
        }
        val jsProperties = if (maybeProperties.isDefined) {
          maybeProperties.get.toJSArray: JSProperties
        } else {
          js.Array(): JSProperties
        }
        JsShift(order, jsTeam, jsProperties)
      }
    }
  }

  object JsShift {
    def apply(order: Int,
              jsTeam: JSTeam = js.Array(),
              jsProperties: JSProperties = js.Array()): JsShift = {
      literal(order = order, team = jsTeam, properties = jsProperties).asInstanceOf[JsShift]
    }
  }

  @js.native
  trait Hello extends Vue {
    var days: js.Array[JsDay] = js.native
    def init(nDays: Int, nShifts: Int): Unit = js.native
  }

  def main(): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val `:class` = "v-bind:class".attr
    val `:checked` = "v-bind:checked".attr
    val `@click` = "v-on:click".attr
    val slot = "slot".tag

    Vue.component("my-component", literal(
      props=js.Array("myMsg"),
      template=p("A custom component with msg {{myMsg}}").render
    ))

    Vue.component("checkbox-btn", literal(
      template=label(
        `class`:="btn",
        `:class`:="""{
             'active':checked,
             'btn-success':style === 'success',
             'btn-warning':style === 'warning',
             'btn-info':style === 'info',
             'btn-danger':style === 'danger',
             'btn-default':style === 'default',
             'btn-primary':style === 'primary'
             }""",
        input(`type`:="checkbox",
          autocomplete:="false",
          `:checked`:="checked",
          `@click`:="handleClick"),
        slot()
      ).render,
      props=literal(
        value=literal(
          "type" -> js.eval("String")
        ),
        checked=literal(
          "type" -> js.eval("Boolean"),
          "default" -> js.eval("false")
        )
      ),
      computed=literal(
        style=((th: CheckboxBtn) => th.$parent.style): js.ThisFunction
      ),
      methods=literal(
        handleClick=((vm: CheckboxBtn) => {
          val parent = vm.$parent
          val index = parent.value.indexOf(vm.value)
          if (index == -1) parent.value.push(vm.value) else parent.value.splice(index, 1)
          vm.checked = !vm.checked
        }): js.ThisFunction
      ),
      created=((vm: CheckboxBtn) => {
        if (vm.$parent.value.length > 0) {
          vm.checked = vm.$parent.value.indexOf(vm.value) > -1
        } else if (vm.checked) {
          vm.$parent.value.push(vm.value)
        }
      }): js.ThisFunction
    ))

    Vue.component("checkbox-group", literal(
      template=div(`class`:="btn-group", data.toggle:="buttons", "slot".tag).render,
      props=literal(
        "style" -> literal("default" -> "default"),
        "value" -> literal("default" -> (() => js.Array()))
      )
    ))

    import ExampleJS.DayImplicits._
    import ExampleJS.ShiftImplicits._
    import upickle.default._
    new Vue(
      literal(
        el = "#myApp",
        data = () => { literal(
          title = "example text",
          mode = "init",
          days = js.Array(JsDay.apply(1, List(JsShift.apply(1, None, Some(List("dummy"))))))
        )},
        methods = literal(
          create=((vm: Hello) => {
            val nDays = Js.Num(31); val nShifts = Js.Num(3) // todo: not used
            Ajax.post(
              url = "/month",
              data = upickle.json.write(upickle.Js.Obj("nDays" -> nDays, "nShifts" -> nShifts))
            ).map{r => if (r.status == 201) {
              vm.init(31, 3)
              vm.$set("mode", "create")
            } else {
              dom.console.error(s"error is ${r.responseText}")
            }}
          }): ThisFunction,
          init= ((vm: Hello, nDays: Int, nShifts: Int) => {
            val retval = for (d <- 1 to nDays) yield
             JsDay.apply(d, (1 to nShifts).map(JsShift(_)).toList)
            vm.days = retval.toJSArray
          }): ThisFunction,
          save= ((vm: Hello)=>{
            val data = vm.days.toList.map(jsDay =>
              Day(jsDay.ofTheMonth, jsDay.shifts.toList.map(jsShift =>
                Shift(jsShift.order,
                  Some(jsShift.team.toList.map(WorkerId)),
                  Some(jsShift.properties.toList)
                )
              ))
            )
            Ajax.put(
              url = "/month/2016/05",
              data = write(data),
              headers = Map("content-type" -> "application/json")
            ).map{r => if (r.status == 200) {
              vm.$set("mode", "confirm")
            } else {
              dom.console.error("error is " + r.responseText)
            }}
          }): ThisFunction
        )
      )
    )
  }

  /** Computes the square of an integer.
   *  This demonstrates unit testing.
   */
  def square(x: Int): Int = x*x
}
