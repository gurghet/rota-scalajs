package controllers

import play.api.http.MimeTypes
import play.api.mvc._
import shared.SharedMessages
import scalatags.Text.TypedTag
import scalatags.Text.all._

object Application extends Controller {

  def index = Action { implicit request =>
    Ok(indexView(SharedMessages.itWorks).toString).withHeaders(CONTENT_TYPE -> MimeTypes.HTML)
  }

  def indexView(message: String) = {
    val vmodel: Attr = "v-model".attr
    val mycomponent: Tag = "my-component".tag
    val myMsg: Attr = "my-msg".attr
    val checkboxGroup: Tag = "checkbox-group".tag
    val checkboxBtn: Tag = "checkbox-btn".tag
    val body = Seq(
      h2("Play and scala.js share a same message"),
      ul(
        li("Play shouts: ", em(message)),
        li("Scala.js shouts: ", em(id := "scalajsShoutOut", em))
        ),
      div(id := "myApp",
        input(vmodel := "title"),
        p("{{title}}"),
        mycomponent(myMsg := "this is a normal, super normal, message"),
        button("v-if".attr:="mode === 'init'", "v-on:click".attr:="create", "Crea mese"),
        table("v-if".attr:="mode === 'create'",
          tr("v-for".attr:="day in days",
            td("{{ day.ofTheMonth }}"),
            td("v-for".attr:="shift in day.shifts",
              checkboxGroup("v-bind:value".attr:="shift.properties",
                checkboxBtn(value:="gettone", "Gett"),
                checkboxBtn(value:="altroRep", "A/R")
              )
            )
          )
        ),
        button("v-if".attr:="mode === 'create'", "v-on:click".attr:="save", "Salva"),
        div("v-if".attr:="mode === 'confirm'", "Month saved!")
      )
    )
    mainView(body)
  }

  def mainView(content: Seq[Modifier]) = {
    html(
      head(
        link(rel:="stylesheet",
          href:="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css",
          "integrity".attr:="sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7",
          "crossorigin".attr:="anonymous")
      ),
      body(
        content,
        // include the Scala.js scripts that sbt-play-scalajs has copied from the "client"
        // project to the Play public target folder
        scripts("exampleclient")
      )
    )
  }

  def scripts(projectName: String) = selectScripts(projectName) :+ launcher(projectName)

  def selectScripts(projectName: String): Seq[TypedTag[String]] = {
    import play.api.Play
    val vueVersion = "1.0.21"
    if (Play.isProd(Play.current)) {
      Seq(script(src := s"/assets/${projectName.toLowerCase}-opt.js"),
      script(src := s"https://cdnjs.cloudflare.com/ajax/libs/vue/$vueVersion/vue.min.js"))
    } else {
      Seq(script(src := s"/assets/${projectName.toLowerCase}-fastopt.js"),
      script(src := s"https://cdnjs.cloudflare.com/ajax/libs/vue/$vueVersion/vue.js"))
    }
  }

  def launcher(projectName: String) = {
    script(src := s"/assets/${projectName.toLowerCase}-launcher.js", `type` := "text/javascript")
  }

}
