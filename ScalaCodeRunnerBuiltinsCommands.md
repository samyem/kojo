


---

**Under construction**

---


# Command completion and help #

The Edit window knows about most commands and can provide
text completion for them when Ctrl+Space is pressed.

The `help()` command prints a synopsis of the most common commands in the Output window.

# Turtle commands #

The turtle commands are also methods that turtles respond to, e.g. the command
`forward(100)` can be in your script and will be obeyed by the original turtle,
but you can also have

```
val myTurtle = newTurtle()
myTurtle.forward(100)
```

which creates a new turtle and tells that turtle to move forward.  The original
turtle can be referred to explicitly with `turtle0`:

```
turtle0.forward(100)
```


## Movement ##

| **Method**            | **Corresponds to**   | **Description**                                    |
|:----------------------|:---------------------|:---------------------------------------------------|
| `forward(steps)`      | `FORWARD n`          | Moves the turtle forward a given number of steps.  |
| `back(steps)`         | `BACKWARD n`         | Moves the turtle back a given number of steps.     |
| `home()`              |                      | Moves the turtle to its original location, and makes it point "north". |
| `jumpTo(x, y)`        |                      | Sends the turtle to the point (_x_, _y_) without drawing a line. The turtle's heading is not changed. |
| `setPosition(x, y)`   |                      | Sends the turtle to the point (_x_, _y_) without drawing a line. The turtle's heading is not changed. |
| `position`            | `POS`                | Queries the turtle's position.                     |
| `moveTo(x, y)`        | `SETPOS [n n]`       | Turns the turtle towards (_x_, _y_) and moves the turtle to that point. |
| `turn(angle)`         |                      | Turns the turtle through a specified angle. Angles are positive for counter-clockwise turns. |
| `right()`             | `RIGHT 90`           | Turns the turtle 90 degrees right (clockwise).      |
| `right(angle)`        | `RIGHT n`            | Turns the turtle _angle_ degrees right (clockwise). |
| `left()`              | `LEFT 90`            | Turns the turtle 90 degrees left (counter-clockwise).      |
| `left(angle)`         | `LEFT n`             | Turns the turtle _angle_ degrees left (counter-clockwise). |
| `towards(x, y)`       |                      | Turns the turtle towards the point (_x_, _y_).     |
| `setHeading(angle)`   | `SETHEADING n`       | Turns the turtle to _angle_ (0 is towards the right side of the screen ("east"), 90 is up ("north")). |
| `heading`             |                      | Queries the turtle's heading (0 is towards the right side of the screen ("east"), 90 is up ("north")). |

## The pen ##

| **Method**              | **Corresponds to**      | **Description**                                    |
|:------------------------|:------------------------|:---------------------------------------------------|
| `penDown()`             | `PENDOWN`               | Makes the turtle draw lines as it moves (the default setting). |
| `penUp()`               | `PENUP`                 | Makes the turtle _not_ draw lines as it moves.     |
| `setPenColor(color)`    | `SETPENCOLOR [n n n]`   | Specifies the color of the pen that the turtle draws with. |
| `setFillColor(color)`   |                         | Specifies the fill color of the figures drawn by the turtle. |
| `setPenThickness(n)`    |                         | Specifies the width of the pen that the turtle draws with. |
| `saveStyle()`           |                         | Makes Kojo remember the current style.             |
| `restoreStyle()`        |                         | Brings back a saved style.                         |

### Colors ###

The `setPenColor` and `setFillColor` commands expect a color value as argument.  The following colors are pre-defined: `blue`, `red`, `yellow`, `green`, `orange`, `purple`, `pink`, `brown`, `black`, and `white`.  To get other colors, use either the utility functions `color(r, g, b)` or `color(value)`.  The former takes three integers representing the amounts of red, green, and blue in the desired color, and the latter takes a single integer that is a combination of these components.  Example:

```
color(75, 132, 125)     // a medium teal-ish color
color(0x4b, 0x84, 0x7d) // the same color (values are in hexadecimal notation)
color(0x4b847d)         // also the same color (concatenating the hex values)
color(4949117)          // still the same color (back to decimal notation)
```

### Saving and restoring styles ###

The settings of the pen and fill colors and the pen thickness can be saved and
restored later, irrespective of changes made between save and restore.
Multiple saves can be done and will be restored in reverse order.  In
programming terms, styles are saved on a stack.  If you don't know what a stack
is, think of it as telling Kojo to write down the settings on a piece of paper
and put it on a pile (save) and picking up the uppermost piece of paper and
reading the settings from it (restore).

## Other ##

| **Method**              | **Corresponds to**      | **Description**                                    |
|:------------------------|:------------------------|:---------------------------------------------------|
| `beamsOn()`             |                         | Shows crossbeams centered on the turtle - to help with solving puzzles. |
| `beamsOff()`            |                         | Hides crossbeams.                                  |
| `invisible()`           | `HIDETURTLE`            | Hides the turtle.                                  |
| `visible()`             | `SHOWTURTLE`            | Makes the turtle visible again.                    |
| `write(text)`           |                         | Makes the turtle write the specified object as a string at its current location. |
| `setAnimationDelay(d)`  |                         | Sets the turtle's speed. The specified delay _d_ is the amount of time (in milliseconds) taken by the turtle to move through a distance of one hundred steps. |
| `animationDelay`        |                         | Queries the turtle's delay setting.                |

# Canvas commands #

The canvas commands are user commands that don't directly involve a turtle.

## Appearance ##

| **Method**              | **Description**                                    |
|:------------------------|:---------------------------------------------------|
| `clear()`               | Clears the screen. To bring the turtle to the center of the window after this command, just resize the turtle canvas. |
| `gridOn()`              | Shows a grid on the canvas.                        |
| `gridOff()`             | Hides the grid.                                    |
| `zoom(factor, cx, cy)`  | Zooms in (i.e. scales the coordinate axes) by the given _factor_, and positions (_cx_, _cy_) at the center of the turtle canvas. |
| `zoom(xfactor, yfactor, cx, cy)`| Scales the axes independently, and positions (_cx_, _cy_) at the center of the turtle canvas. |
| `undo()`                | Undoes the last turtle command.                    |

## Image export ##

**Note:** these commands are not in version Beta-160710-3.

| **Method**                              | **Description**                                    |
|:----------------------------------------|:---------------------------------------------------|
| `exportImage(filePrefix)`               | Creates and stores an image of the canvas.         |
| `exportThumbnail(filePrefix, height)`   | Creates and stores an image of the canvas, scaled to the given height. |

The full path and name of the image file is printed in the Output window.

## Making things ##

| **Method**                              | **Description**                                    |
|:----------------------------------------|:---------------------------------------------------|
| `newTurtle()`                           | Creates a new turtle at (0, 0) and returns a reference to it. |
| `newTurtle(x, y)`                       | Creates a new turtle at (_x_, _y_) and returns a reference to it. |
| `newPuzzler(x, y)`                      | Creates a special turtle at (_x_, _y_) and returns a reference to it. |
| `newFigure(x, y)`                       | Creates a drawable figure at (_x_, _y_) and returns a reference to it. |

### Figures ###

In addition to drawing with the turtle, the user can draw shapes with
figures.  The default figure is referred to as `figure0`, and additional
figures can be created like so:

```
val myFigure = newFigure(x, y)
```

See FigureDrawing for a description of the methods that figures respond to.

### Puzzlers ###

A puzzler is a special kind of turtle that moves on a layer below all other
turtles and is blue instead of green.  The puzzler and lines drawn by it aren't
erased by the `clear` command.  Puzzlers are used to draw example figures that
the user can imitate using the default turtle, but the user can also create new
puzzlers with `newPuzzler`.

The predefined puzzler scripts are `circle`, `msquare`, `square`, and
`triangle`.  To use e.g. the `square` puzzler, write

```
loadPuzzle("square")
```

| **Method**                              | **Description**                                    |
|:----------------------------------------|:---------------------------------------------------|
| `listPuzzles()`                         | Shows the names of the puzzles available in the system. |
| `loadPuzzle(name)`                      | Loads the named puzzle.                            |
| `clearPuzzlers()`                       | Clears out the puzzler turtles and the puzzles from the screen. |

# Output (and input) commands #

| **Method**                              | **Description**                                    |
|:----------------------------------------|:---------------------------------------------------|
| `print(obj)`                            | Displays _obj_ as a string in the output window.   |
| `print(str)`                            | Displays _str_ in the output window.               |
| `println(str)`                          | Displays _str_ in the output window.               |
| `readln(promptString)`                  | Displays the given prompt in the output window and reads a line that the user enters. |
| `readInt(promptString)`                 | Displays the given prompt in the output window and reads an Integer value that the user enters. |
| `readDouble(promptString)`              | Displays the given prompt in the output window and reads a Double-precision floating point value that the user enters. |
| `help()`                                | Displays a synopsis of the most common commands    |
| `version()`                             | Displays the version of Scala being used.          |
| `clearOutput()`                         | Clears the output window.                          |

## Output window feedback ##

The user can control the amount of feedback that running a script provides in
the output window.  By default, only error messages, strings printed with
output commands, and the results of single-line scripts are printed in the
output window.  The _script in output_ option also quotes the script in the
output window, and the _verbose output_ option shows the values of assignments
and simple expressions.

**Note:** the commands affect the output _next_ time a script is executed.

| **Method**                              | **Description**                                    |
|:----------------------------------------|:---------------------------------------------------|
| `showScriptInOutput()`                  | Enables the display of scripts in the output window when they run. |
| `hideScriptInOutput()`                  | Stops the display of scripts in the output window. |
| `showVerboseOutput()`                   | Enables the display of the output from the Scala interpreter. |
| `hideVerboseOutput()`                   | Stops the display of the output from the Scala interpreter. |

# Inspect command #

The command `inspect(object)` explores the internal fields of the given object.
Kojo opens a new tab window to show the name, type, ID, and value of the
object.  To the left of the object name is an expand button (`[+]`) which, when
clicked, reveals the fields of the object.

# Iteration command #

The command `repeat(n) { body }` repeats the commands in the _body_ _n_ times.  The following script draws a square:

```
repeat (4) {
    forward(100)
    right()
}
```

# (pseudo)Random numbers #

| **Method**                              | **Description**                                    |
|:----------------------------------------|:---------------------------------------------------|
| `random(upperBound)`                    | Returns a random Integer between 0 (inclusive) and upperBound (exclusive). |
| `randomDouble(upperBound)`              | Returns a random Double-precision number between 0 (inclusive) and upperBound (exclusive). |