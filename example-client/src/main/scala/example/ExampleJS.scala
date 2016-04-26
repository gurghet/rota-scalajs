package example

import com.felstar.scalajs.vue.Vue
import org.scalajs.dom.ext.Ajax
import upickle.Js
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

import scala.scalajs.js
import org.scalajs.dom
import js.Dynamic.literal
import shared._
import scala.scalajs.js.annotation.JSExport
import scala.scalajs.js.{JSON, ThisFunction}
import scalatags.Text.all._
import collection.mutable.{Seq => MutableSeq, Map => MutableMap}
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

  def shift2literal(shift: Shift): js.Object = {
    literal(
      order = shift.order,
      team = shift.team.toJSArray,
      preferences = shift.preferences.toJSDictionary,
      properties = shift.properties.toJSArray
    )
  }

  def day2literal(day: Day): js.Object = {
    literal(
      ofTheMonth = day.ofTheMonth,
      shifts = day.shifts.map{ case shift =>
          shift2literal(shift)
      }.toJSArray
    )
  }

  @JSExport
  def createDay(ofTheMonth: Int, shifts: MutableSeq[Shift]): Day = {
    Day(ofTheMonth, shifts)
  }

  @JSExport
  def createShift(order: Int,
                  team: mutable.Seq[String],
                  preferences: mutable.Map[String, Int],
                  properties: mutable.Seq[String]): Shift = {
    Shift(order, team, preferences, properties)
  }

  def freezeShift(shift: Shift): ImmutableShift = {
    ImmutableShift(shift.order, shift.team, shift.preferences.toMap, shift.properties)
  }

  def freezeDay(day: Day): ImmutableDay = {
    ImmutableDay(day.ofTheMonth, day.shifts.map(freezeShift).toSeq)
  }

  @js.native
  trait Hello extends Vue {
    var days: js.Array[js.Object] = js.native
    var scalaDays: js.Array[Day] = js.native
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

    new Vue(
      literal(
        el = "#myApp",
        data = () => { literal(
          title = "example text",
          mode = "init",
          days = MutableSeq(createDay(1, MutableSeq(createShift(1, MutableSeq(), MutableMap(), MutableSeq("dummy prop")))))
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
            dom.console.info(s"init was called")
            val retval = for (d <- 1 to nDays) yield
             createDay(d, collection.mutable.Seq.concat((1 to nShifts).map{sh => createShift(1, MutableSeq(), MutableMap(), MutableSeq("dummy"))}))
            vm.days = retval.map(day => day2literal(day)).toJSArray
            vm.scalaDays = retval.toJSArray
          }): ThisFunction,
          save= ((vm: Hello)=>{
            import upickle.default._
            Ajax.put(
              url = "/month/2016/05",
              data = upickle.json.write(upickle.default.writeJs(vm.scalaDays.map(freezeDay).toSeq)),
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
