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

package net.kogics.kojo
package xscala

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
        clear()
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
    "zoom" -> 
    """zoom(factor) - Zooms in by the given factor, leaving the center point unchanged.<br/>
       <br/>
       zoom(factor, cx, cy) - Zooms in by the given factor, and positions (cx, cy) at the center of the turtle canvas.
""",
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
    "println" -> "println(obj) - Displays the given object as a string in the output window, with a newline at the end.",
    "print" -> "print(obj) - Displays the given object as a string in the output window, without a newline at the end.",
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
    "playMp3Loop" -> "playMp3Loop(fileName) - Plays the specified MP3 file in the background.",
    "ColorHSB" -> "ColorHSB(h, s, b) - Creates a color with the given Hue (0-360), Saturation (0-100), and Brighness (0-100) values.",
    "Color" -> "Color(r, g, b, opac) - Creates a color with the given red, green, blue, and opacity (optional) values.",
    "ColorG" -> "ColorG(x1, y1, color1, x2, y2, color2, cyclic) - Creates a color gradient for filling shapes. The cyclic value is optional.",
    "setBackground" -> "setBackground(color) - Sets the canvas background to the specified color. You can use predefined colors for setting the background, or you can create your own colors using the Color, ColorHSB, and ColorG functions.",
    "setBackgroundH" -> "setBackgroundH(color1, color2) - Sets the canvas background to a horizontal color gradient defined by the two specified colors.",
    "setBackgroundV" -> "setBackgroundV(color1, color2) - Sets the canvas background to a vertical color gradient defined by the two specified colors."
  )

  val TwContent = Map[String, String](
    "forward" -> 
    <div>
      <strong>forward</strong>(numSteps) - Moves the turtle forward by the given number of steps. <br/>
      <br/>
      <em>Example:</em> <br/><br/>
      <pre>
        clear()
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
      <em>Example:</em> <br/><br/>
      <pre>
        clear()
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
        clear()
        // move the turtle out
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
        clear()
        // move the turtle out
        forward(100)
        right()
        forward(50)
          
        // now report its position
        print(position) // Point(50.00, 100.00)
      </pre>
    </div>
    ,
    "style" -> "style - Tells you the turtle's current style. See the help for saveStyle() for more information on styles.",
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
        clear()
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
        clear()
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
      <em>Example:</em> <br/>
      <br/>
      <pre>
        clear()
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
      <em>Example:</em> <br/>
      <br/>
      <pre>
        clear()
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
      <em>Example:</em> <br/>
      <br/>
      <pre>
        clear()
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
      <em>Example:</em> <br/>
      <br/>
      <pre>
        clear()
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
      easily be restored later with a <tt>restorePosHe()</tt>.<br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        clear()
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
        clear()
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
    "saveStyle" -> 
    <div>
      <strong>saveStyle</strong>() - Saves the turtle's current style, so that it can 
      easily be restored later with <tt>restoreStyle()</tt> .<br/>
      <p>
        The turtle's style includes:
        <ul>
          <li>Pen Color</li>
          <li>Pen Thickness</li>
          <li>Fill color</li>
          <li>Pen Font Size</li>
        </ul>
      </p>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def tick(n: Int) {{
            // save current style, position and heading
            saveStyle()
            savePosHe()
            setPenColor(gray)
            right()
            forward(n)
            back(n * 2)
            restorePosHe()
            restoreStyle()
            // restore caller's style, position and heading
        }}

        clear()
        setPenColor(green)
        right()
        // green line
        forward(100)
        // grey tick
        tick(10)
        // green line
        forward(100)
      </pre>
    </div>
    ,
    "restoreStyle" -> 
    <div>
      <strong>restoreStyle</strong>() - Restores the turtle's style
      based on an earlier <tt>saveStyle()</tt>.
      <br/>
      <p>
        The turtle's style includes:
        <ul>
          <li>Pen Color</li>
          <li>Pen Thickness</li>
          <li>Fill color</li>
          <li>Pen Font Size</li>
        </ul>
      </p>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        def tick(n: Int) {{
            // save current style, position and heading
            saveStyle()
            savePosHe()
            setPenColor(gray)
            right()
            forward(n)
            back(n * 2)
            restorePosHe()
            restoreStyle()
            // restore caller's style, position and heading
        }}

        clear()
        setPenColor(green)
        right()
        // green line
        forward(100)
        // grey tick
        tick(10)
        // green line
        forward(100)
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
    "wipe" -> "wipe() - Wipes the turtle canvas by earsing all pictures. Meant to be used during an animation.",
    "clearOutput" -> "clearOutput() - Clears the output window.",
    "clearWithUL" -> "clearWithUL(unit) - Clears the turtle canvas, sets the given unit length (Pixel, Cm, or Inch), and brings the turtle to the center of the canvas.",
    "arc" ->
    <div>
        <strong>arc</strong>(radius, angle) - Gets the turtle to make an arc with the given 
        radius and angle.<br/>
        Positive angles make the turtle go left (ant-clockwise). Negative angles make the turtle go right (clockwise) <br/>
        <br/>
        <em>Examples:</em> <br/>
        <br/>
        <pre>
            // a simple arc
            clear()    
            arc(100, 45)

            // a pattern of arcs
            clear()   
            right(135)
            repeat (5) {{
              arc(50, 90)
              arc(50, -90)
            }}
        </pre>
    </div>
    ,
    "circle" ->
    <div>
        <strong>circle</strong>(radius) - Gets the turtle to make a circle with the given 
        radius. <br/>
        A circle(50) command is equivalent to an arc(50, 360) command.<br/>
        <br/>
        <em>Example:</em> <br/>
        <br/>
        <pre>
            clear()    
            circle(50)
        </pre>
    </div>
    ,
    "def" ->
    <div>
        <strong>def</strong> - Let's you define a new command or function.<br/>
        <br/>
        <em>Examples:</em> <br/>
        <br/>
        <pre>
            // A User defined command named square
            // Takes one input
            def square(side: Int) {{
                repeat(4) {{
                    forward(side)
                    right()
                }}
            }}
            clear()
            // two different calls to square command
            square(100)
            square(200)


            // A User defined function named sum
            // Takes two inputs, and returns a result
            def sum(n1: Int, n2: Int) = {{
                n1 + n2
            }}
            clearOutput()
            // call to the sum function within print command
            print(sum(3, 5))
            // another call to the sum function
            print(sum(20, 7))
        </pre>
    </div>
    ,
    "if" ->
    <div>
        <strong>if</strong> or <strong>if-else</strong> - Let you do conditional execution.<br/>
        <br/>
        <em>Examples:</em> <br/>
        <br/>
        <pre>
            clear()    
            val size = 50 
            // conditionally run a command
            // the else part is optional
            if (size > 100) {{
                setFillColor(blue)
            }}
            else {{
                setFillColor(green)
            }}
            circle(size)


            val n = 100
            // conditionally evaluate an expression
            val big = if (n > 50) true else false
            clearOutput()
            println(big)
        </pre>
    </div>
    ,
    "val" ->
    <div>
        <strong>val</strong> - Let's you create a named value (thus letting you 
        associate a name with a value). This makes your programs easier to modify 
        and easier to understand.<br/>
        <br/>
        <em>Example:</em> <br/>
        <br/>
        <pre>
            clear()    
            val size = 50 
            circle(size)
            repeat (4) {{
                forward(size)
                right()
            }}
        </pre>
    </div>
    ,
    "pict" -> "pict { t => } is obsolete. Use the PictureT (preferred) or Picture function instead.",
    "Picture" -> 
    <div>
      <strong>Picture</strong>{{ drawingCode }} - Makes a picture out of the given turtle drawing code. <br/>
      The picture needs to be drawn for it to become visible in the turtle canvas. <br/><br/>
      <em>Note - every picture has its own turtle. For pictures created with the <tt>Picture</tt> function, 
        Kojo's defalt turtle is set to the picture's turtle while the picture is being drawn. 
        Your drawing code can then continue to use the default turtle for drawing. Contrast this with 
        picture's created using the <tt>PictureT</tt> function. For those, a turtle is explicitly supplied to 
        your drawing code, and your code needs to draw using that turtle. Kojo's default turtle is left 
        alone in that case.</em><br/>
      <br/>
      <em>Example:</em> <br/>
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
    "PictureT" -> 
    <div>
      <strong>PictureT</strong>{{ t => drawingCode }} - Makes a picture out of the given turtle drawing code, 
      which needs to draw using the supplied turtle <tt>t</tt>.<br/>
      The picture needs to be drawn for it to become visible in the turtle canvas. <br/>
      <br/>
      <em>Example:</em> <br/>
      <br/>
      <pre>
        // create a function for making a picture with a circle in it
        def p = PictureT {{ t =>
          import t._
          circle(50)
        }}

        clear()
        invisible()
        // draw the picture
        draw(p)
      </pre>
    </div>
    ,
    "HPics" -> 
    <div>
      <strong>HPics</strong>(pictures) <br/>
      A container for pictures that lays out the given pictures horizontally. <br/>
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
        val pic = HPics(
            p,
            p,
            p
        )
        draw(pic)
      </pre>
    </div>
    ,
    "VPics" -> 
    <div>
      <strong>VPics</strong>(pictures) <br/>
      A container for pictures that lays out the given pictures vertically. <br/>
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
        val pic = VPics(
            p,
            p,
            p
        )
        draw(pic)
      </pre>
    </div>
    ,
    "GPics" -> 
    <div>
      <strong>GPics</strong>(pictures) <br/>
      A container for pictures that lays out the given pictures one on top of the other. <br/>
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
        val pic = GPics(
            p,
            rot(30) -> p,
            rot(60) -> p
        )
        draw(pic)
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
    "offset" -> 
    <div>
      <strong>offset</strong>(x, y) -> picture <br/>
      Offsets the given picture by the given x and y values, with respect to the
      global (canvas) coordinate system. <br/>
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
        axesOn()
        val pic = rot(60) * offset(100, 0) -> p
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
    "hue" -> 
    <div>
      <strong>hue</strong>(factor) -> picture <br/>
      Changes the hue of the given picture's fill color by the given factor. <br/>
      The factor needs to be between -1 and 1. <br/>
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
        val pic = hue(0.5) * fillColor(blue) -> p
        // val pic = hue(-0.5) * fillColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "sat" -> 
    <div>
      <strong>sat</strong>(factor) -> picture <br/>
      Changes the saturation of the given picture's fill color by the given factor. <br/>
      The factor needs to be between -1 and 1. <br/>
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
        val pic = sat(-0.5) * fillColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "brit" -> 
    <div>
      <strong>brit</strong>(factor) -> picture <br/>
      Changes the brightness of the given picture's fill color by the given factor.<br/>
      The factor needs to be between -1 and 1. <br/>
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
        val pic = brit(-0.5) * fillColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "opac" -> 
    <div>
      <strong>opac</strong>(factor) -> picture <br/>
      Changes the opacity of the given picture by the given factor.<br/>
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
        val pic = opac(-0.5) * fillColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "axes" -> 
    <div>
      <strong>axes</strong> -> picture <br/>
      Turns on local axes for the picture (to help during picture construction). <br/>
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
        val pic = axes * fillColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "flipY" -> 
    <div>
      <strong>flipY</strong> -> picture <br/>
      Flips the given picture around the local Y axis. <br/>
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
        axesOn()
        val pic = trans(100, 0) * flipY * fillColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "flipX" -> 
    <div>
      <strong>flipX</strong> -> picture <br/>
      Flips the given picture around the local X axis. <br/>
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
        axesOn()
        val pic = trans(100, 0) * flipX * fillColor(blue) -> p
        draw(pic)
      </pre>
    </div>,
    "flip" -> 
    <div>
      <strong>flip</strong> -> picture <br/>
      The same thing as flipY. <br/>
      Flips the given picture around the local Y axis. <br/>
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
        axesOn()
        val pic = trans(100, 0) * flip * fillColor(blue) -> p
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
    clearLangContent()
  }

  def activateMw() {
    modeSpecificContent = MwContent
    clearLangContent()
  }

  def activateStaging() {
    modeSpecificContent = StagingContent
    clearLangContent()
  }

  val langContent: collection.mutable.Map[String, Map[String, String]] = collection.mutable.Map()
  def addContent(lang: String, content: Map[String, String]) {
//    import util.Typeclasses._
//    langContent +=  (lang -> (langContent.getOrElse(lang, Map()) |+| content))
    langContent +=  (lang -> (langContent.getOrElse(lang, Map()) ++ content))
  }
  
  def clearLangContent() {
    langContent.clear()
  }
  
  def langHelp(name: String, lang: String): Option[String] = {
    langContent.get(lang) match {
      case Some(content) => content.get(name)
      case None => None
    }
  }
  
  def apply(topic: String) = {
    CommonContent.getOrElse(
      topic, 
      modeSpecificContent.getOrElse(
        topic, 
        langHelp(topic, System.getProperty("user.language")).getOrElse(null)
      )
    )
  }
}
