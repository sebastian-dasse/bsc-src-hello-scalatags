package hello

import org.scalajs.dom.{document, html, raw}
import scala.util.Random
import scalajs.js.annotation.JSExport

@JSExport
object App {

  @JSExport
  def main(): Unit = {
//    val container = getContainer
    val container = getContainerWScalaTagsJsDom
    setupUiWRawString(container)
    setupUiWRawElements(container)
    setupUiWScalaTagsText(container)
    setupUiWScalaTagsJsDom(container)
  }


  /// version 1
  def getContainer: html.Div = {
    def grabContainer = Option(document.getElementById("container"))
    def createContainer = {
      val c = document.createElement("div")
      c.id = "container"
      document.body.appendChild(c)
    }
    grabContainer.getOrElse(createContainer)
  }.asInstanceOf[html.Div]

  /// version 2 - better
  def getContainerWScalaTagsJsDom: html.Div = {
    import scalatags.JsDom.all._

    def grabContainer = Option(document.getElementById("container"))
    def createContainer = document.body.appendChild(div(id:="container").render)
    grabContainer.getOrElse(createContainer)
  }.asInstanceOf[html.Div]


  /// helper, for versions 1 and 2
  def appendTo(container: html.Element)(str: String): Unit = {
    val aSpan = document.createElement("span")
    aSpan.innerHTML = str
    container.appendChild(aSpan)
  }

  /* -- version 1 --
   *
   * - untyped
   *   -> not checked by compiler
   *   -> might result in malformed HTML
   * - insecure
   *   -> script tags in plain strings
   */
  def setupUiWRawString(container: html.Element): Unit = {
    val (foo, bar) = ("simple", Random.nextInt(100))
    val str = s"""
        |<div id="raw-string-div">
        |  <h1>Hello scalajs.dom with raw string!</h1>
        |  <p>
        |    This is a <i>$foo</i> html snippet
        |    showing a random number: <b>$bar</b>.
        |  </p>
        |</div>
      """.stripMargin

    /// malformed code like this passes unchecked:
//    val str = "<div>foo bar</p>"
    /// bad code like this passes unchecked:
//    val str = "<script> while(true) { console.log(new Date()) } </script>"

    appendTo(container)(str)
  }

  /* -- version 2 - a lot more verbose and not really better --
   *
   * - typed (kind of)
   *   -> checked by compiler
   *   -> malformed HTML still possible
   * - not secure
   *   -> script tags instead of other tags are possible
   */
  def setupUiWRawElements(container: html.Element): Unit = {
    val (foo, bar) = ("simple", Random.nextInt(100))

    def createElemWTextContent(tagName: String, textContent: String): raw.Element = {
      val elem = document.createElement(tagName)
      elem.textContent = textContent
      elem
    }

    def appendChildren(parent: raw.Node)(children: raw.Node*) =
      children.foreach(parent.appendChild)

    val aDiv: raw.Element = document.createElement("div")
    aDiv.id = "raw-scalajs-div"

    /// more narrow typing possible, but cast with asInstanceOf necessary:
//    var anotherDiv: html.Div = document.createElement("div").asInstanceOf[html.Div]
    /// ... and, as the programmer is responsible to do the right type cast, bad code is still not checked:
//    anotherDiv = document.createElement("script").asInstanceOf[html.Div]
//    anotherDiv.textContent = """console.log("hi from insecure script!");"""
//    aDiv.appendChild(anotherDiv)

    val aH1 = document.createElement("h1")
    aH1.textContent = "Hello scalajs.dom with raw elements!"
    aDiv.appendChild(aH1)

    val aP = document.createElement("p")
    appendChildren(aP)(
      document.createTextNode("This is a "),
      createElemWTextContent("i", foo),
      document.createTextNode(" html snippet showing a random number: "),
      createElemWTextContent("b", bar.toString),
      document.createTextNode(".")
    )
    aDiv.appendChild(aP)

    container.appendChild(aDiv)
  }

  /* -- version 3 - better --
   *
   * - type-checked
   * - but eventually generating a string, which then can be assigned to the innerHTML attribute of a HTML element
   */
  def setupUiWScalaTagsText(container: html.Element): Unit = {
    import scalatags.Text.all._

    val (foo, bar) = ("simple", Random.nextInt(100))

    appendTo(container)(
      div(                           // TypedTag[String]
        id:="a-text-div",
        h1("Hello scalatags.Text!"), // TypedTag[String]
        p(
          "This is a ", i(foo), " html snippet showing a random number: ", b(bar), "."
        )
      ).render                       // String
    )

    /// but this insecure assignment is still possible, but the script is not evaluated (which is a good thing)
//    var wannabeDiv: TypedTag[String] = div()
//    wannabeDiv = h1("a heading")
//    wannabeDiv = script("""console.log("hi from wannabe!")""")
//    appendTo(container)(wannabeDiv.render)
  }

  /* version 4 - best
   *
   * - type-checked
   * - directly operating on the DOM
   * - close to secure
   */
  def setupUiWScalaTagsJsDom(container: html.Element): Unit = {
    import scalatags.JsDom.all._

    val (foo, bar) = ("simple", Random.nextInt(100))

    container.appendChild(
      div(                            // TypedTag[html.Div]
        h1("Hello scalatags.JsDom!"), // TypedTag[html.Heading]
        p(
          "This is a ", i(foo), " html snippet showing a random number: ", b(bar), "."
        )                             // TypedTag[html.Paragraph]
      ).render                        // html.Div
    )

    /// type-checked -> does not compile:
    //    var anotherDiv: TypedTag[html.Div] = div()
    //    anotherDiv = h1("a heading")
    //    anotherDiv = br()
    /// but this insecure assignment still works - foo could be placed somewhere inside a div:
    //    var foo: TypedTag[html.Element] = b("some bold text")
    //    foo = div()
    //    foo = script("""console.log("hi from scalatags!");""")
    //    container.appendChild(div(foo).render)
  }
}
