/*
 * Copyright (C) 2012 Lalit Pant <pant.lalit@gmail.com>
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

// Do not format source. It messes up help code formatting.

object Help {

  implicit def elem2str(e: xml.Elem) = e.toString
  val CommonContent = Map[String, String](
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
    "zoom" -> "zoom(factor, cx, cy) - Zooms in by the given factor, and positions (cx, cy) at the center of the turtle canvas.",
    "gridOn" -> "gridOn() - Shows a grid on the turtle canvas.",
    "gridOff" -> "gridOff() - Hides the grid on the turtle canvas.",
    "axesOn" -> "axesOn() - Shows the X and Y axes on the turtle canvas.",
    "axesOff" -> "axesOff() - Hides the X and Y axes on the turtle canvas.",
    "showScriptInOutput" -> "showScriptInOutput() - Enables the display of scripts in the output window when they run.",
    "hideScriptInOutput" -> "hideScriptInOutput() - Stops the display of scripts in the output window.",
    "showVerboseOutput" -> "showVerboseOutput() - Enables the display of output from the Scala interpreter. By default, output from the interpreter is shown only for single line scripts.",
    "hideVerboseOutput" -> "hideVerboseOutput() - Stops the display of output from the Scala interpreter.",
    "retainSingleLineCode" -> "retainSingleLineCode() - Makes Kojo retain a single line of code after running it. By default, single lines of code are cleared after running.",
    "clearSingleLineCode" -> "clearSingleLineCode() - Makes Kojo clear a single line of code after running it. This is the default behavior.",
    "version" -> "version - Displays the version of Scala being used.",
    "println" -> "println(obj) or print(obj) - Displays the given object as a string in the output window.",
    "print" -> "println(obj) or print(obj) - Displays the given object as a string in the output window.",
    "readln" -> "readln(promptString) - Displays the given prompt in the output window and reads a line that the user enters.",
    "readInt" -> "readInt(promptString) - Displays the given prompt in the output window and reads an Integer value that the user enters.",
    "readDouble" -> "readDouble(promptString) - Displays the given prompt in the output window and reads a Double-precision Real value that the user enters.",
    "random" -> "random(upperBound) - Returns a random Integer between 0 (inclusive) and upperBound (exclusive).",
    "randomDouble" -> "randomDouble(upperBound) - Returns a random Double-precision Real between 0 (inclusive) and upperBound (exclusive).",
    "inspect" -> "inspect(obj) - Opens up a window showing the internal fields of the given object",
    "playMusic" -> "playMusic(score) - Plays the specified melody, rhythm, or score.",
    "playMusicUntilDone" -> "playMusicUntilDone(score) - Plays the specified melody, rhythm, or score, and waits till the music finishes.",
    "playMusicLoop" -> "playMusicLoop(score) - Plays the specified melody, rhythm, or score in the background - in a loop.",
    "textExtent" -> "textExtent(text, fontSize) - Determines the size/extent of the given text fragment for the given font size.",
    "runInBackground" -> "runInBackground(command) - Runs the given code in the background, concurrently with other code that follows right after this command.",
    "playMp3" -> "playMp3(fileName) - Plays the specified MP3 file.",
    "playMp3Loop" -> "playMp3Loop(fileName) - Plays the specified MP3 file in the background."
  )

  val TwContent = Map[String, String](
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
    "setPenColor" -> 
    <div>
      <strong>setPenColor</strong>(color) - Specifies the color of the pen that the turtle draws with. <br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        setPenColor(blue)
        // makes a blue line
        forward(100)
                    
        setPenColor(green)
        // makes a green line
        forward(100)
      </pre>
    </div>
    ,
    "setFillColor" -> 
    <div>
      <strong>setFillColor</strong>(color) - Specifies the fill color of the figures drawn by the turtle. <br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        setFillColor(blue)
        // make a circle filled with blue
        circle(50)
                    
        setFillColor(green)
        // make a circle filled with green
        circle(50)
      </pre>
    </div>
    ,
    "setPenThickness" -> 
    <div>
      <strong>setPenThickness</strong>(thickness) - Specifies the width of the pen that the turtle draws with. <br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        setPenThickness(10)
        // make a line that is 10 units thick
        forward(100)
                    
        setPenThickness(15)
        // make a line that is 15 units thick
        forward(100)
      </pre>
    </div>
    ,
    "setPenFontSize" -> 
    <div>
      <strong>setPenFontSize</strong>(n) - Specifies the font size of the pen that the turtle writes with. <br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        setPenFontSize(15)
        // write with a font size of 15
        write("Hi There")
                    
        setPenFontSize(20)
        // write with a font size of 20
        write("Hi There")
      </pre>
    </div>
    ,
    "savePosHe" -> 
    <div>
      <strong>savePosHe</strong>() - Saves the turtle's current position and heading, so that they can 
      easily be restored later.<br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        // save the turtle's position and heading
        savePosHe()
                    
        // move wherever
        forward(100)
        right(45)
        forward(60)
                    
        // now restore the saved position and heading, 
        // so that the turtles gets back to 
        // exactly where it started out from 
        restorePosHe()
      </pre>
    </div>
    ,
    "restorePosHe" -> 
    <div>
      <strong>restorePosHe</strong>() - Restores the turtle's current position and heading 
      based on an earlier <tt>savePosHe()</tt>.<br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        // save the turtle's position and heading
        savePosHe()
                    
        // move wherever
        forward(100)
        right(45)
        forward(60)
                    
        // now restore the saved position and heading, 
        // so that the turtles gets back to 
        // exactly where it started out from 
        restorePosHe()
      </pre>
    </div>
    ,
    "beamsOn" -> "beamsOn() - Shows crossbeams centered on the turtle - to help with thinking about the turtle's heading/orientation.",
    "beamsOff" -> "beamsOff() - Hides the turtle crossbeams that are turned on by beamsOn().",
    "invisible" -> "invisible() - Hides the turtle.",
    "visible" -> "visible() - Makes the hidden turtle visible again.",
    "write" -> "write(obj) - Makes the turtle write the specified object as a string at its current location.",
    "setAnimationDelay" -> 
    <div>
      <strong>setAnimationDelay</strong>(delay) - Sets the turtle's speed. The specified delay 
      is the amount of time (in milliseconds) taken by the turtle to move through a distance of one hundred steps.<br/>
      The default delay is 1000 milliseconds (or 1 second).<br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        // default animation delay
        // drawing the line takes 1 second
        forward(100)
                    
        setAnimationDelay(500)
        // drawing the line takes 1/2 seconds
        forward(100)
                    
        setAnimationDelay(100)
        // drawing the line takes 1/10 seconds
        forward(100)
      </pre>
    </div>
    ,
    "animationDelay" -> "animationDelay - Queries the turtle's delay setting.",
    "clear" -> "clear() - Clears the turtle canvas, and brings the turtle to the center of the canvas.",
    "clearWithUL" -> "clearWithUL(unit) - Clears the turtle canvas, sets the given unit length (Pixel, Cm, or Inch), and brings the turtle to the center of the canvas.",
    "Picture" -> 
    <div>
      <strong>Picture</strong>{{ drawingCode }} - Makes a picture out of the given turtle drawing code. <br/>
      The picture needs to be drawn for it to become visible in the turtle canvas. <br/>
      <br/>
      <em>Examples:</em> <br/>
      <br/>
      <pre>
        // create a function for making a picture with a circle in it
        def p = Picture {{
          circle(50)
        }}

        clear()
        invisible()
        // draw the picture
        draw(p)
      </pre>
    </div>
    ,
    "rot" -> 
    <div>
      <strong>rot</strong>(angle) -> picture <br/>
      Rotates the given picture by the given angle. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def p = Picture {{
          repeat (4) {{
            forward(50)
            right()
          }}
        }}

        clear()
        invisible()
        val pic = rot(30) -> p
        draw(pic)
      </pre>
    </div>
    ,
    "trans" -> 
    <div>
      <strong>trans</strong>(x, y) -> picture <br/>
      Translates the given picture by the given x and y values. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def p = Picture {{
          repeat (4) {{
            forward(50)
            right()
          }}
        }}

        clear()
        invisible()
        val pic = trans(10, 5) -> p
        draw(pic)
      </pre>
    </div>,
    "scale" -> 
    <div>
      <strong>scale</strong>(factor) -> picture <br/>
      <strong>scale</strong>(xf, yf) -> picture <br/>
      Scales the given picture by the given scaling factor(s). <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def p = Picture {{
          repeat (4) {{
            forward(50)
            right()
          }}
        }}

        clear()
        invisible()
        val pic = scale(2) -> p
        draw(pic)
      </pre>
    </div>,
    "fillColor" -> 
    <div>
      <strong>fillColor</strong>(color) -> picture <br/>
      Fills the given picture with the given color. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def p = Picture {{
          repeat (4) {{
            forward(50)
            right()
          }}
        }}

        clear()
        invisible()
        val pic = fillColor(green) -> p
        draw(pic)
      </pre>
    </div>,
    "penColor" -> 
    <div>
      <strong>penColor</strong>(color) -> picture <br/>
      Sets the pen color for the given picture to the given color. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def p = Picture {{
          repeat (4) {{
            forward(50)
            right()
          }}
        }}

        clear()
        invisible()
        val pic = penColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "penWidth" -> 
    <div>
      <strong>penWidth</strong>(thickness) -> picture <br/>
      Sets the pen width for the given picture to the given thickness. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def p = Picture {{
          repeat (4) {{
            forward(50)
            right()
          }}
        }}

        clear()
        invisible()
        val pic = penWidth(10) -> p
        draw(pic)
      </pre>
    </div>,
    "stClear" -> "stClear() - Clears the Story Teller Window.",
    "stPlayStory" -> "stPlayStory(story) - Plays the given story.",
    "stFormula" -> "stFormula(latex) - Converts the supplied latex string into html that can be displayed in the Story Teller Window.",
    "stPlayMp3" -> "stPlayMp3(fileName) - Plays the specified MP3 file.",
    "stPlayMp3Loop" -> "stPlayMp3Loop(fileName) - Plays the specified MP3 file in the background.",
    "stAddButton" -> "stAddButton(label) {code} - Adds a button with the given label to the Story Teller Window, and runs the supplied code when the button is clicked.",
    "stAddField" -> "stAddField(label, default) - Adds an input field with the supplied label and default value to the Story Teller Window.",
    "stFieldValue" -> "stFieldValue(label, default) - Gets the value of the specified field.",
    "stShowStatusMsg" -> "stShowStatusMsg(msg) - Shows the specified message in the Story Teller status bar.",
    "stSetScript" -> "stSetScript(code) - Copies the supplied code to the script editor.",
    "stRunCode" -> "stRunCode(code) - Runs the supplied code (without copying it to the script editor).",
    "stClickRunButton" -> "stClickRunButton() - Simulates a click of the run button.",
    "stShowStatusError" -> "stShowStatusError(msg) - Shows the specified error message in the Story Teller status bar.",
    "stNext" -> "stNext() - Moves the story to the next page/view."
  )
  
  val StagingContent = Map[String, String](
    "clear" -> "clear() - Clears the canvas.",
    "clearWithUL" -> "clearWithUL(unit) - Clears the canvas, and sets the given unit length (Pixel, Cm, or Inch)."
  )

  val MwContent = Map[String, String]()

  @volatile var modeSpecificContent: Map[String, String] = TwContent
  
  def activateTw() {
    modeSpecificContent = TwContent
  }

  def activateMw() {
    modeSpecificContent = MwContent
  }

  def activateStaging() {
    modeSpecificContent = StagingContent
  }

  def apply(topic: String) = {
    CommonContent.getOrElse(topic, modeSpecificContent.getOrElse(topic, null))
  }
}
