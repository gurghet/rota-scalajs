package example

import com.felstar.scalajs.vue.Vue

import scala.scalajs.js
import org.scalajs.dom
import js.Dynamic.literal
import shared.SharedMessages
import scala.scalajs.js.ThisFunction
import scalatags.Text.all._

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
      props=js.Array("value", "checked"),
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
          days = js.Array(
            literal(ofTheMonth = 1, shifts = js.Array(
              literal(order = 1, properties = js.Array("dummy prop"))
            ))
          )
        )},
        methods = literal(
          create=((vm: Vue) => vm.$set("mode", "create")): ThisFunction,
          init=(a:Int,b:Int)=>(),
          save=()=>()
        )
      )
    )
  }

  /** Computes the square of an integer.
   *  This demonstrates unit testing.
   */
  def square(x: Int): Int = x*x
}
