/*
 * Copyright (C) 2010 Lalit Pant <pant.lalit@gmail.com>
 *
 * The contents of this file are subject to the GNU General Public License
 * Version 3 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.gnu.org/copyleft/gpl.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 */

package net.kogics.kojo
package xscala

import core._
import util._

object Builtins extends InitedSingleton[Builtins] {
  def initedInstance(scalaCodeRunner: ScalaCodeRunner) = synchronized {
    instanceInit()
    val ret = instance()
    ret.scalaCodeRunner = scalaCodeRunner
    ret
  }

  protected def newInstance = new Builtins
}

import java.awt.Color

class Builtins extends SCanvas with TurtleMover {
  @volatile var scalaCodeRunner: ScalaCodeRunner = _
  lazy val tCanvas = scalaCodeRunner.tCanvas
  lazy val ctx = scalaCodeRunner.ctx
  lazy val storyTeller = scalaCodeRunner.storyTeller
  lazy val turtle0 = tCanvas.turtle0
  lazy val figure0 = tCanvas.figure0

  type Turtle = core.Turtle
  type Color = java.awt.Color
  type Point = net.kogics.kojo.core.Point

  PuzzleLoader.init()
  val Random = new java.util.Random

  val blue = Color.blue
  val red = Color.red
  val yellow = Color.yellow
  val green = Color.green
  val orange = Color.orange
  val purple = new Color(0x740f73)
  val pink = Color.pink
  val brown = new Color(0x583a0b)
  val black = Color.black
  val white = Color.white

  def forward() = println("Please provide the distance to move forward - e.g. forward(100)")
  override def forward(n: Double) = turtle0.forward(n)
  UserCommand("forward", List("numSteps"), "Moves the turtle forward a given number of steps.")

  def back() = println("Please provide the distance to move back - e.g. back(100)")
  override def back(n: Double) = turtle0.back(n)
  UserCommand("back", List("numSteps"), "Moves the turtle back a given number of steps.")

  override def home(): Unit = turtle0.home()
  UserCommand("home", Nil, "Moves the turtle to its original location, and makes it point north.")

  override def jumpTo(p: Point): Unit = turtle0.jumpTo(p.x, p.y)
  override def jumpTo(x: Double, y: Double) = turtle0.jumpTo(x, y)
  UserCommand.addCompletion("jumpTo", List("x", "y"))

  override def setPosition(p: Point): Unit = turtle0.jumpTo(p)
  override def setPosition(x: Double, y: Double) = turtle0.jumpTo(x, y)
  UserCommand("setPosition", List("x", "y"), "Sends the turtle to the point (x, y) without drawing a line. The turtle's heading is not changed.")

  override def position: Point = turtle0.position
  UserCommand.addSynopsis("position - Queries the turtle's position.")

  def moveTo() = println("Please provide the coordinates of the point that the turtle should move to - e.g. moveTo(100, 100)")
  override def moveTo(x: Double, y: Double) = turtle0.moveTo(x, y)
  override def moveTo(p: Point): Unit = turtle0.moveTo(p.x, p.y)
  UserCommand("moveTo", List("x", "y"), "Turns the turtle towards (x, y) and moves the turtle to that point. ")

  def turn() = println("Please provide the angle to turn in degrees - e.g. turn(45)")
  override def turn(angle: Double) = turtle0.turn(angle)
  UserCommand("turn", List("angle"), "Turns the turtle through a specified angle. Angles are positive for counter-clockwise turns.")

  override def right(): Unit = turtle0.right()
  UserCommand("right", Nil, "Turns the turtle 90 degrees right (clockwise).")
  override def right(angle: Double): Unit = turtle0.right(angle)
  UserCommand("right", List("angle"), "Turns the turtle angle degrees right (clockwise).")

  override def left(): Unit = turtle0.left()
  UserCommand("left", Nil, "Turns the turtle 90 degrees left (counter-clockwise).")
  override def left(angle: Double): Unit = turtle0.left(angle)
  UserCommand("left", List("angle"), "Turns the turtle angle degrees left (counter-clockwise). ")

  def towards() = println("Please provide the coordinates of the point that the turtle should turn towards - e.g. towards(100, 100)")
  override def towards(p: Point): Unit = turtle0.towards(p.x, p.y)
  override def towards(x: Double, y: Double) = turtle0.towards(x, y)
  UserCommand("towards", List("x", "y"), "Turns the turtle towards the point (x, y).")

  override def setHeading(angle: Double) = turtle0.setHeading(angle)
  UserCommand("setHeading", List("angle"), "Sets the turtle's heading to angle (0 is towards the right side of the screen ('east'), 90 is up ('north')).")

  override def heading: Double = turtle0.heading
  UserCommand.addSynopsis("heading - Queries the turtle's heading (0 is towards the right side of the screen ('east'), 90 is up ('north')).")
  UserCommand.addSynopsisSeparator()

  override def penDown() = turtle0.penDown()
  UserCommand("penDown", Nil, "Makes the turtle draw lines as it moves (the default setting). ")

  override def penUp() = turtle0.penUp()
  UserCommand("penUp", Nil, "Makes the turtle not draw lines as it moves.")

  def setPenColor() = println("Please provide the color of the pen that the turtle should draw with - e.g setPenColor(blue)")
  override def setPenColor(color: Color) = turtle0.setPenColor(color)
  UserCommand("setPenColor", List("color"), "Specifies the color of the pen that the turtle draws with.")

  def setFillColor() = println("Please provide the fill color for the areas drawn by the turtle - e.g setFillColor(yellow)")
  override def setFillColor(color: Color) = turtle0.setFillColor(color)
  UserCommand("setFillColor", List("color"), "Specifies the fill color of the figures drawn by the turtle.")
  UserCommand.addSynopsisSeparator()

  def setPenThickness() = println("Please provide the thickness of the pen that the turtle should draw with - e.g setPenThickness(1)")
  override def setPenThickness(t: Double) = turtle0.setPenThickness(t)
  UserCommand("setPenThickness", List("thickness"), "Specifies the width of the pen that the turtle draws with.")

  override def saveStyle() = turtle0.saveStyle()
  UserCommand.addCompletion("saveStyle", Nil)

  override def restoreStyle() = turtle0.restoreStyle()
  UserCommand.addCompletion("restoreStyle", Nil)

  override def beamsOn() = turtle0.beamsOn()
  UserCommand("beamsOn", Nil, "Shows crossbeams centered on the turtle - to help with solving puzzles.")

  override def beamsOff() = turtle0.beamsOff()
  UserCommand("beamsOff", Nil, "Hides the turtle crossbeams.")

  override def invisible() = turtle0.invisible()
  UserCommand("invisible", Nil, "Hides the turtle.")

  override def visible() = turtle0.visible()
  UserCommand("visible", Nil, "Makes the hidden turtle visible again.")
  UserCommand.addSynopsisSeparator()

  override def write(obj: Any): Unit = turtle0.write(obj)
  override def write(text: String) = turtle0.write(text)
  UserCommand("write", List("obj"), "Makes the turtle write the specified object as a string at its current location.")

  override def setAnimationDelay(d: Long) = turtle0.setAnimationDelay(d)
  UserCommand("setAnimationDelay", List("delay"), "Sets the turtle's speed. The specified delay is the amount of time (in milliseconds) taken by the turtle to move through a distance of one hundred steps.")

  override def animationDelay = turtle0.animationDelay
  UserCommand.addSynopsis("animationDelay - Queries the turtle's delay setting.")
  UserCommand.addSynopsisSeparator()

  override def undo() = tCanvas.undo()
  UserCommand("undo", Nil, "Undoes the last turtle command.")

  override def clear() = tCanvas.clear()
  UserCommand("clear", Nil, "Clears the screen. To bring the turtle to the center of the window after this command, just resize the turtle canvas.")

  override def zoom(factor: Double, cx: Double, cy: Double) = tCanvas.zoom(factor, cx, cy)
  UserCommand("zoom", List("factor", "cx", "cy"), "Zooms in by the given factor, and positions (cx, cy) at the center of the turtle canvas.")
  UserCommand.addSynopsisSeparator()

  def listPuzzles = PuzzleLoader.listPuzzles
  UserCommand("listPuzzles", Nil, "Shows the names of the puzzles available in the system.")

  def loadPuzzle(name: String) {
    val oPuzzleFn = PuzzleLoader.readPuzzle(name)
    if (oPuzzleFn.isDefined) {
      val code = oPuzzleFn.get + """

          def go() {
            val pTurtle = newPuzzler(0,0)
            puzzle(pTurtle)
          }
          go()
      """
      scalaCodeRunner.runCode(code)

      val code2 = """
          clearOutput()
          println("Puzzle Description")
        """
      scalaCodeRunner.runCode(code2)
    }
    else {
      println("Puzzle not available: " + name)
    }
  }
  UserCommand("loadPuzzle", List("name"), "Loads the named puzzle.")

  override def clearPuzzlers() = tCanvas.clearPuzzlers()
  UserCommand("clearPuzzlers", Nil, "Clears out the puzzler turtles and the puzzles from the screen.")
  UserCommand.addSynopsisSeparator()

  override def gridOn() = tCanvas.gridOn()
  UserCommand("gridOn", Nil, "Shows a grid on the canvas.")

  override def gridOff() = tCanvas.gridOff()
  UserCommand("gridOff", Nil, "Hides the grid.")

  override def axesOn() = tCanvas.axesOn()
  UserCommand("axesOn", Nil, "Shows the X and Y axes on the canvas.")

  override def axesOff() = tCanvas.axesOff()
  UserCommand("axesOff", Nil, "Hides the X and Y axes.")

  def newTurtle(): Turtle = newTurtle(0, 0)
  override def newTurtle(x: Int, y: Int) = tCanvas.newTurtle(x, y)
  UserCommand("newTurtle", List("x", "y"), "Makes a new turtle located at the point (x, y).")

  UserCommand.addSynopsis("turtle0 - Gives you a handle to the default turtle.")
  UserCommand.addSynopsisSeparator()

  def showScriptInOutput() = ctx.showScriptInOutput()
  UserCommand("showScriptInOutput", Nil, "Enables the display of scripts in the output window when they run.")

  def hideScriptInOutput() = ctx.hideScriptInOutput()
  UserCommand("hideScriptInOutput", Nil, "Stops the display of scripts in the output window.")

  def showVerboseOutput() = ctx.showVerboseOutput()
  UserCommand("showVerboseOutput", Nil, "Enables the display of output from the Scala interpreter. By default, output from the interpreter is shown only for single line scripts.")

  def hideVerboseOutput() = ctx.hideVerboseOutput()
  UserCommand("hideVerboseOutput", Nil, "Stops the display of output from the Scala interpreter.")
  UserCommand.addSynopsisSeparator()

  def version = println("Scala " + scala.tools.nsc.Properties.versionString)
  UserCommand.addSynopsis("version - Displays the version of Scala being used.")

  def repeat(n: Int) (fn: => Unit) {
    for (i <- 1 to n) {
      fn
      Throttler.throttle()
    }
  }
  UserCommand.addCompletion("repeat", " (${n}) {\n    ${cursor}\n}")
  UserCommand.addSynopsis("repeat(n) {} - Repeats the commands within braces n number of times.")

  def print(obj: Any): Unit = println(obj)
  UserCommand.addCompletion("print", List("obj"))

  def println(obj: Any): Unit = println(if (obj == null) "null" else obj.toString)
  UserCommand.addCompletion("println", List("obj"))
  UserCommand.addSynopsis("println(obj) or print(obj) - Displays the given object as a string in the output window.")

  def readln(prompt: String): String = ctx.readInput(prompt)
  UserCommand("readln", List("promptString"), "Displays the given prompt in the output window and reads a line that the user enters.")

  def readInt(prompt: String): Int = readln(prompt).toInt
  UserCommand("readInt", List("promptString"), "Displays the given prompt in the output window and reads an Integer value that the user enters.")

  def readDouble(prompt: String): Double = readln(prompt).toDouble
  UserCommand("readDouble", List("promptString"), "Displays the given prompt in the output window and reads a Double-precision Real value that the user enters.")

  def random(upperBound: Int) = Random.nextInt(upperBound)
  UserCommand("random", List("upperBound"), "Returns a random Integer between 0 (inclusive) and upperBound (exclusive).")

  def randomDouble(upperBound: Int) = Random.nextDouble * upperBound
  UserCommand("randomDouble", List("upperBound"), "Returns a random Double-precision Real between 0 (inclusive) and upperBound (exclusive).")

  def inspect(obj: AnyRef) = ctx.inspect(obj)
  UserCommand("inspect", List("obj"), "Explores the internal fields of the given object.")

  def stClear() {
    storyTeller.clear()
  }
  UserCommand.addSynopsisSeparator()
  UserCommand("stClear", Nil, "Clears the Story Teller Window.")

  type Para = story.Para
  val Para = story.Para
  type Page = story.Page
  val Page = story.Page
  type IncrPage = story.IncrPage
  val IncrPage = story.IncrPage
  type Story = story.Story
  val Story = story.Story
  type StoryPage = story.Viewable

  def stPlayStory(st: story.Story) {
    storyTeller.playStory(st)
  }

  def stFormula(latex: String) =
    <div style={"text-align:center;margin:6px;"}>
      <img src={xml.Unparsed(story.CustomHtmlEditorKit.latexPrefix + latex)}/>
    </div>
  UserCommand("stFormula", List("latex"), "Converts the supplied latex string into html that can be displayed in the Story Teller Window.")

  def playMp3(mp3File: String) {
    storyTeller.play(mp3File)
  }
  UserCommand("playMp3", List("fileName"), "Plays the specified MP3 file.")

  def playMp3InBg(mp3File: String) {
    storyTeller.playInBg(mp3File)
  }
  UserCommand("playMp3InBg", List("fileName"), "Plays the specified MP3 file in the background.")

  def stAddButton(label: String)(fn: => Unit) {
    storyTeller.addButton(label)(fn)
  }
  UserCommand.addCompletion("stAddButton", " (${label}) {\n    ${cursor}\n}")
  UserCommand.addSynopsis("stAddButton(label) {code} - Adds a button with the given label to the Story Teller Window, and runs the supplied code when the button is clicked.")

  def stAddField(label: String, default: Any) {
    storyTeller.addField(label, default)
  }
  UserCommand("stAddField", List("label", "default"), "Adds an input field with the supplied label and default value to the Story Teller Window.")

  implicit val StringNoS = util.NoS.StringNoS
  implicit val DoubleNoS = util.NoS.DoubleNoS
  implicit val IntNoS = util.NoS.IntNoS
  import util.NumberOrString
  def stFieldValue[T](label: String, default: T)(implicit nos: NumberOrString[T]): T = {
    storyTeller.fieldValue(label, default)
  }
  UserCommand("stFieldValue", List("label", "default"), "Gets the value of the specified field.")

  def stShowStatusMsg(msg: String) {
    storyTeller.showStatusMsg(msg)
  }
  UserCommand("stShowStatusMsg", List("msg"), "Shows the specified message in the Story Teller status bar.")

  def stSetScript(code: String) = ctx.setScript(code)
  UserCommand("stSetScript", List("code"), "Copies the supplied code to the script editor.")

  def stShowStatusError(msg: String) {
    storyTeller.showStatusError(msg)
  }
  UserCommand("stShowStatusError", List("msg"), "Shows the specified error message in the Story Teller status bar.")

  def help() = {
    println("""You can press Ctrl-Space in the script window at any time to see available commands and functions.

Here's a partial list of the available commands:
              """ + UserCommand.synopses)
  }

  // undocumented
  override def style: Style = turtle0.style
  def color(r: Int, g: Int, b: Int) = new Color(r, g, b)
  def color(rgbHex: Int) = new Color(rgbHex)
  def clearOutput() = ctx.clearOutput()
  override def exportImage(filePrefix: String) = tCanvas.exportImage(filePrefix)
  override def exportThumbnail(filePrefix: String, height: Int) = tCanvas.exportThumbnail(filePrefix, height)
  override def newFigure(x: Int, y: Int) = tCanvas.newFigure(x, y)
  override def newPuzzler(x: Int, y: Int) = tCanvas.newPuzzler(x, y)
  override def zoomXY(xfactor: Double, yfactor: Double, cx: Double, cy: Double) =
    tCanvas.zoomXY(xfactor, yfactor, cx, cy)

  def println(s: String): Unit = {
    // Runs on Actor pool (interpreter) thread
    scalaCodeRunner.println(s + "\n")
    Throttler.throttle()
  }
}
