/*
 * Copyright (C) 2009 Lalit Pant <pant.lalit@gmail.com>
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

package net.kogics.kojo.xscala

object CodeCompletionUtils {
  val NotIdChars = """ .(){}!%&+\-<=>?@\\^`|~#:/*""" + "\n\r\t"

  import org.netbeans.modules.scala.core.lexer.ScalaTokenId
  val Keywords = 
    ScalaTokenId.values.filter { v =>
      v.asInstanceOf[ScalaTokenId.V].primaryCategory == "keyword"
    }.map { v =>
      v.asInstanceOf[ScalaTokenId.V].fixedText
    }.toList

  val KeywordTemplates = Map(
    "for" -> "for (i <- 1 to ${n}) {\n    ${cursor}\n}",
    "while" -> "while (${condition}) {\n    ${cursor}\n}",
    "if" -> "if (${condition}) {\n    ${cursor}\n}"
  )

  // UserCommand adds to this
  val BuiltinsMethodTemplates = collection.mutable.Map(
    "switchTo" -> "switchTo()",
    "onKeyPress" -> "onKeyPress { k =>\n    k match {\n    case Kc.VK_RIGHT => ${cursor}\n       case _ => \n    }\n}",
    "onMouseClick" -> "onMouseClick { (x, y) =>\n    ${cursor}\n}",
    "onMouseDrag" -> "onMouseDrag { (x, y) =>\n    ${cursor}\n}",
    "stAddLinkHandler" -> "stAddLinkHandler(${handlerName}) {d: ${argType} =>\n    ${cursor}\n}",
    "VPics" -> "VPics(\n      p,\n      p\n)",
    "HPics" -> "HPics(\n      p,\n      p\n)",
    "GPics" -> "GPics(\n      p,\n      p\n)",
    // Todo - is there any commonality here with the staging templates
    "trans" -> "trans(${x}, ${y})",
    "rot" -> "rot(${angle})",
    "rotp" -> "rotp(${angle}, ${x}, ${y})",
    "scale" -> "scale(${factor})",
    "opac" -> "opac(${changeFactor})",
    "hue" -> "hue(${changeFactor})",
    "hueMod" -> "hueMod(${color}, ${changeFactor})",
    "sat" -> "sat(${changeFactor})",
    "satMod" -> "satMod(${color}, ${changeFactor})",
    "brit" -> "brit(${changeFactor})",
    "britMod" -> "britMod(${color}, ${changeFactor})",
    "translate" -> "translate(${x}, ${y})",
    "transv" -> "transv(${vector})",
    "offset" -> "offset(${x}, ${y})",
    "rotate" -> "rotate(${angle})",
    "rotateAboutPoint" -> "rotateAboutPoint(${angle}, ${x}, ${y})",
    "fillColor" -> "fillColor(${color})",
    "penColor" -> "penColor(${color})",
    "penWidth" -> "penWidth(${n})",
    "spin" -> "spin(${n})",
    "reflect" -> "reflect(${gap})",
    "show" -> "draw(${pic/s})",
    "draw" -> "draw(${pic})",
    "drawAndHide" -> "drawAndHide(${pic})",
    "animate" -> "animate {\n    ${cursor}\n}",
    "onAnimationStop" -> "onAnimationStop {\n    ${cursor}\n}",
    "act" -> "act { me => \n    ${cursor}\n}",
    "row" -> "row(${picture}, ${n})",
    "col" -> "col(${picture}, ${n})",
    "setUnitLength" -> "setUnitLength(${unit})",
    "clearWithUL" -> "clearWithUL(${unit})",
    "intersects" -> "intersects(${otherPic})",
    "intersection" -> "intersection(${otherPic})",
    "collidesWith" -> "collidesWith(${otherPic})",
    "collisions" -> "collisions(pics)",
    "distanceTo" -> "distanceTo(${otherPic})",
    "stopAnimation" -> "stopAnimation()",
    "isKeyPressed" -> "isKeyPressed(Kc.VK_${cursor})",
    "activateCanvas" -> "activateCanvas()",
    "setBackground" -> "setBackground(${paint})",
    "setBackgroundH" -> "setBackgroundH(${color1}, ${color2})",
    "setBackgroundV" -> "setBackgroundV(${color1}, ${color2})",
    "Color" -> "Color(${red}, ${green}, ${blue}, ${opacity})",
    "ColorG" -> "ColorG(${x1}, ${y1}, ${color1}, ${x2}, ${y2}, ${color2}, ${cyclic})",
    "ColorHSB" -> "ColorHSB(${h}, ${s}, ${b})",
    "Vector2D" -> "Vector2D(${x}, ${y})",
    "angle" -> "angle(${vector})",
    "angleTo" -> "angleTo(${vector})",
    "fastDraw" -> "fastDraw {\n    ${cursor}\n}",
    "stopMp3" -> "stopMp3()",
    "stopMusic" -> "stopMusic()"
  )
  
  val TwMethodTemplates = Map(
    "pict" -> "Picture {\n    ${cursor}\n}",
    "Picture" -> "Picture {\n    ${cursor}\n}",
    "Pic" -> "Pic { t =>\n    import t._\n    ${cursor}\n}",
    "circle" -> "circle(${radius})",
    "arc" -> "arc(${radius}, ${angle})"
  ) 

  val MwMethodTemplates = Map(
    "figure" -> "figure(${name})",
    "point" -> "point(${x}, ${y}, ${optionalLabel})",
    "pointOn" -> "pointOn(${line}, ${x}, ${y})",
    "line" -> "line(${point1}, ${point2})",
    "lineSegment" -> "lineSegment(${point1}, ${point2Orlength})",
    "ray" -> "ray(${point1}, ${point2})",
    "circle" -> "circle(${center}, ${radius})",
    "angle" -> "angle(${point1}, ${point2}, ${pointOrSize})",
    "intersect" -> "intersect(${shape1}, ${shape2})",
    "midpoint" -> "midpoint(${lineSegment})",
    "perpendicular" -> "perpendicular(${toLine}, ${thruPoint})",
    "parallel" -> "perpendicular(${toLine}, ${thruPoint})",
    "setLabel" -> "setLabel(${label})",
    "setColor" -> "setColor(${color})",
    "add" -> "add(${shapes})",
    "show" -> "show(${shapes})",
    "showGrid" -> "showGrid()",
    "hideGrid" -> "hideGrid()",
    "showAxes" -> "showAxes()",
    "hideAxes" -> "hideAxes()",
    "showAlgebraView" -> "showAlgebraView()",
    "hideAlgebraView" -> "hideAlgebraView()",
    "turtle" -> "turtle(${x}, ${y})",
    "labelPosition" -> "labelPosition(${label})",
    "findPoint" -> "findPoint(${label})",
    "findLine" -> "findLine(${label})",
    "findAngle" -> "findAngle(${label})",
    "beginPoly" -> "beginPoly()",
    "endPoly" -> "endPoly()",
    "showAngles" -> "showAngles()",
    "showLengths" -> "showLengths()",
    "hideLengths" -> "hideLengths()",
    "hideAngles" -> "hideAngles()",
    "showExternalAngles" -> "showExternalAngles()",
    "hideExternalAngles" -> "hideExternalAngles()"
  )

  val StagingMethodTemplates = Map(
    "point" -> "point(${x}, ${y})",
    "line" -> "line(${x0}, ${y0}, ${x1}, ${y1})",
    "rectangle" -> "rectangle(${x0}, ${y0}, ${width}, ${height})",
    "text" -> "text(${content}, ${x}, ${y})",
    "circle" -> "circle(${cx}, ${cy}, ${radius})",
    "ellipse" -> "ellipse(${cx}, ${cy}, ${width}, ${height})",
    "arc" -> "arc(${cx}, ${cy}, ${rx}, ${ry}, ${startDegree}, ${extentDegree})",
    "refresh" -> "refresh {\n    ${cursor}\n}",
    "screenSize" -> "screenSize(${width}, ${height})",
    "background" -> "background(${color})",
    "dot" -> "dot(${x}, ${y})",
    "square" -> "square(${x}, ${y}, ${side})",
    "roundRectangle" -> "roundRectangle(${x}, ${y}, ${width}, ${height}, ${rx}, ${ry})",
    "pieslice" -> "pieslice(${cx}, ${cy}, ${rx}, ${ry}, ${startDegree}, ${extentDegree})",
    "openArc" -> "openArc(${cx}, ${cy}, ${rx}, ${ry}, ${startDegree}, ${extentDegree})",
    "chord" -> "chord(${cx}, ${cy}, ${rx}, ${ry}, ${startDegree}, ${extentDegree})",
    "vector" -> "vector(${x0}, ${y0}, ${x1}, ${y1}, ${headLength})",
    "star" -> "star(${cx}, ${cy}, ${inner}, ${outer}, ${numPoints})",
    "polyline" -> "polyline(${points})",
    "polygon" -> "polygon(${points})",
    "triangle" -> "triangle(${point1}, ${point2}, ${point3})",
    "quad" -> "quad(${point1}, ${point2}, ${point3}, ${point4})",
    "svgShape" -> "svgShape(${element})",
    "grayColors" -> "grayColors(${highGrayNum})",
    "grayColorsWithAlpha" -> "grayColorsWithAlpha(${highGrayNum}, ${highAlphaNum})",
    "rgbColors" -> "rgbColors(${highRedNum}, ${highGreenNum}, ${highBlueNum})",
    "rgbColorsWithAlpha" -> "rgbColorsWithAlpha(${highRedNum}, ${highGreenNum}, ${highBlueNum}, ${highAlphaNum})",
    "hsbColors" -> "hsbColors(${highHueNum}, ${highSaturationNum}, ${highBrightnessNum})",
    "namedColor" -> "namedColor(${colorName})",
    "lerpColor" -> "lerpColor(${colorFrom}, ${colorTo}, ${amount})",
    "fill" -> "fill(${color})",
    "noFill" -> "noFill()",
    "stroke" -> "stroke(${color})",
    "noStroke" -> "noStroke()",
    "strokeWidth" -> "strokeWidth(${width})",
    "withStyle" -> "withStyle (${fillColor}, ${strokeColor}, ${strokeWidth}) {\n    ${cursor}\n}",
    "constrain" -> "constrain(${value}, ${min}, ${max})",
    "norm" -> "norm(${value}, ${low}, ${high})",
    "map" -> "map(${value}, ${min1}, ${max1}, ${min2}, ${max2})",
    "sq" -> "sq(${value})",
    "sqrt" -> "sqrt(${value})",
    "dist" -> "dist(${x0}, ${y0}, ${x1}, ${y1})",
    "mag" -> "mag(${x}, ${y})",
    "lerp" -> "lerp(${low}, ${high}, ${value})",
    "loop" -> "loop {\n    ${cursor}\n}",
    "stop" -> "stop()",
    "reset" -> "reset()",
    "wipe" -> "wipe()",
    "sprite" -> "sprite(${x}, ${y}, ${filename})",
    "path" -> "path(${x}, ${y})",
    "lineTo" -> "lineTo(${x}, ${y})",
    "setFontSize" -> "setFontSize(${size})",
    "setContent" -> "setContent(${content})",
    "show" -> "show()",
    "hide" -> "hide()",
    "erase" -> "erase()"
  )
  
  @volatile var ExtraMethodTemplates: collection.Map[String, String] = TwMethodTemplates

  def activateTw() {
    ExtraMethodTemplates = TwMethodTemplates
  }

  def activateMw() {
    ExtraMethodTemplates = MwMethodTemplates
  }

  def activateStaging() {
    ExtraMethodTemplates = StagingMethodTemplates
  }

  val MethodDropFilter = List("turtle0")
  val VarDropFilter = List("builtins", "predef")
  val InternalVarsRe = java.util.regex.Pattern.compile("""res\d+|\p{Punct}.*""")
  val InternalMethodsRe = java.util.regex.Pattern.compile("""_.*|.*\$.*""")

  def notIdChar(c: Char): Boolean =  NotIdChars.contains(c)

  def findLastIdentifier(rstr: String): Option[String] = {
    val str = " " + rstr
    var remaining = str.length
    while(remaining > 0) {
      if (notIdChar(str(remaining-1))) return Some(str.substring(remaining))
      remaining -= 1
    }
    None
  }

  def findIdentifier(str: String): (Option[String], Option[String]) = {
    if (str.length == 0) return (None, None)

    if (str.endsWith(".")) {
      (findLastIdentifier(str.substring(0, str.length-1)), None)
    }
    else {
      val lastDot = str.lastIndexOf('.')
      if (lastDot == -1) (None, findLastIdentifier(str))
      else {
        val tPrefix = str.substring(lastDot+1)
        if (tPrefix == findLastIdentifier(tPrefix).get)
          (findLastIdentifier(str.substring(0, lastDot)), Some(tPrefix))
        else
          (None, findLastIdentifier(tPrefix))
      }
    }
  }
  
  implicit def elem2str(e: xml.Elem) = e.toString
  val Help = Map[String, String](
    "forward" -> 
    <div>
      <strong>forward</strong>(numSteps) - Moves the turtle forward by the given number of steps. <br/>
      <br/>
      <em>Examples:</em> <br/><br/>
      <pre>
        // move forward by 100 steps
        forward(100) 
          
        // move forward by 200 steps
        forward(200)
      </pre>
    </div>
    ,
    
    "back" -> 
    <div>
      <strong>back</strong>(numSteps) - Moves the turtle back by the given number of steps. <br/>
      <br/>
      <em>Examples:</em> <br/><br/>
      <pre>
        // move back by 100 steps
        back(100)
                        
        // move back by 200 steps
        back(200)
      </pre>
    </div>
    ,
    "repeat" -> 
    <div>
      <strong>repeat</strong>(n){{ }} - Repeats the commands within braces n number of times.<br/>
      <br/>
      <em>Example:</em> <br/><br/>
      <pre>
        // make a square with the help of the repeat command
        repeat (4) {{
        forward(100)
        right()
        }}
      </pre>
    </div>
    ,
    "repeati" -> "repeati(n) {i => } - Repeats the commands within braces n number of times. The current repeat index is available within the braces.",
    "repeatWhile" -> "repeatWhile(cond) {} - Repeats the commands within braces while the given condition is true.",
    "repeatUntil" -> "repeatUntil(cond) {} - Repeats the commands within braces until the given condition is true.",
    "home" -> 
    <div>
      <strong>home</strong>() - Moves the turtle to its original location, and makes it point north. <br/>
      <br/>
      <em>Example:</em> <br/><br/>
      <pre>
        // move the turtle out
        clear()
        forward(100)
        right()
        forward(50)
          
        // now take it back home
        home()
      </pre>
    </div>
    ,
    "setPosition" -> 
    <div>
      <strong>setPosition</strong>() - (x, y) - Sends the turtle to the point (x, y) without drawing a line. The turtle's heading is not changed. <br/>
      <br/>
      <em>Examples:</em> <br/><br/>
      <pre>
        setPosition(100, 50)
                  
        setPosition(80, 150)
      </pre>
    </div>
    ,
    "position" -> 
    <div>
      <strong>position</strong> - Tells you the turtle's current position. <br/>
      <br/>
      <em>Example:</em> <br/><br/>
      <pre>
        // move the turtle out
        clear()
        forward(100)
        right()
        forward(50)
          
        // now report its position
        print(position) // Point(50.00, 100.00)
      </pre>
    </div>
    ,
    "moveTo" -> "moveTo(x, y) - Turns the turtle towards (x, y) and moves the turtle to that point. ",
    "turn" -> 
    <div>
      <strong>turn</strong>(angle) - Turns the turtle through the specified angle.<br/>
      Positive angles are in the anti-clockwise direction. Negative angles are in the clockwise direction. <br/>
      <br/>
      <em>Note: </em>It's easier to use <strong>left</strong>(angle) or <strong>right</strong>(angle) to turn the turtle.
    </div>
    ,
    "right" -> 
    <div>
      <strong>right</strong>() - Turns the turtle 90 degrees right (clockwise). <br/>
      <strong>right</strong>(angle) - Turns the turtle right (clockwise) through the given angle in degrees.<br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        // turn right by 90 degrees
        right()
                
        // turn right by 30 degrees
        right(30)
      </pre>
    </div>
    ,
    "left" -> 
    <div>
      <strong>left</strong>() - Turns the turtle 90 degrees left (anti-clockwise). <br/>
      <strong>left</strong>(angle) - Turns the turtle left (anti-clockwise) through the given angle in degrees.<br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        // turn left by 90 degrees
        left()
                
        // turn left by 30 degrees
        left(30)
      </pre>
    </div>
    ,
    "towards" -> "towards(x, y) - Turns the turtle towards the point (x, y).",
    "setHeading" -> "setHeading(angle) - Sets the turtle's heading to angle (0 is towards the right side of the screen ('east'), 90 is up ('north')).",
    "heading" -> "heading - Queries the turtle's heading (0 is towards the right side of the screen ('east'), 90 is up ('north')).",
    "penDown" -> 
    <div>
      <strong>penDown</strong>() - Pushes the turtle's pen down, and makes it draw lines as it moves. <br/>
      The turtle's pen is down by default. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        // pull the turtle's pen up
        penUp()
        // the turtle moves forward without drawing a line
        forward(100) 
                    
        // push the turtle's pen down
        penDown()
        // now the turtle draws a line as it moves forward
        forward(100) 
      </pre>
    </div>
    ,
    "penUp" -> 
    <div>
      <strong>penUp</strong>() - Pulls the turtle's pen up, and prevents it from drawing lines as it moves. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        // pull the turtle's pen up
        penUp()
        // the turtle moves forward without drawing a line
        forward(100) 
                    
        // push the turtle's pen down
        penDown()
        // now the turtle draws a line as it moves forward
        forward(100) 
      </pre>
    </div>
    ,
    "setPenColor" -> "setPenColor(color) - Specifies the color of the pen that the turtle draws with.",
    "setFillColor" -> "setFillColor(color) - Specifies the fill color of the figures drawn by the turtle.",
    "setPenThickness" -> "setPenThickness(thickness) - Specifies the width of the pen that the turtle draws with.",
    "setPenFontSize" -> "setPenFontSize(n) - Specifies the font size of the pen that the turtle writes with.",
    "savePosHe" -> "savePosHe() - Saves the turtle's current position and heading",
    "restorePosHe" -> "restorePosHe() - Restores the turtle's current position and heading",
    "beamsOn" -> "beamsOn() - Shows crossbeams centered on the turtle - to help with solving puzzles.",
    "beamsOff" -> "beamsOff() - Hides the turtle crossbeams.",
    "invisible" -> "invisible() - Hides the turtle.",
    "visible" -> "visible() - Makes the hidden turtle visible again.",
    "write" -> "write(obj) - Makes the turtle write the specified object as a string at its current location.",
    "setAnimationDelay" -> "setAnimationDelay(delay) - Sets the turtle's speed. The specified delay is the amount of time (in milliseconds) taken by the turtle to move through a distance of one hundred steps.",
    "animationDelay" -> "animationDelay - Queries the turtle's delay setting."
  )
}
