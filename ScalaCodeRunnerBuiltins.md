


---

**Under construction**

---


# Introduction #

The Builtins object in ScalaCodeRunner is where the basic Kojo commands are
defined, each command being a method of the object.  The methods typically
don't do real work themselves, instead they usually delegate to one of

  * the code runner's [run context](http://code.google.com/p/kojo/source/browse/KojoEnv/src/net/kogics/kojo/core/codeRunner.scala),
  * the [canvas](http://code.google.com/p/kojo/source/browse/KojoEnv/src/net/kogics/kojo/SpriteCanvas.scala), or
  * the default [turtle](http://code.google.com/p/kojo/source/browse/KojoEnv/src/net/kogics/kojo/turtle/Turtle.scala).

See ScalaCodeRunnerBuiltinsCommands for a list of commands with brief explanations.

If you are building your own version of Kojo, you can add commands of your own
choosing by adding methods to the `Builtins` object; this section will show you
how.

# Example command 1. Random color #

A very simple command does some self-contained calculation and returns a value
that can be used in a script.  As an example, let's make a command that picks a
random color from the list of predefined colors.  Add the following in some
suitable place in `Builtins`:

```
    def randomColor() = {
      val colors = List(blue, red, yellow, green, orange, purple, pink, brown, black, white)

      colors(random(colors.size))
    }
    UserCommand("randomColor", Nil, "Yields a random color.")
```

Compile and run Kojo.  Typing "ran" and pressing Ctrl+Space makes a popup menu
containing random, randomColor, and randomDouble appear.  Choosing randomColor
inserts the text "randomColor()" in the script.  Calling the `help` command
prints the synopsis for randomColor together with the usual help text.  Running
the script

```
repeat (4) {
    setPenColor(randomColor())
    forward(100)
    right()
}
```

draws a square with a random color on each side.

# Example command 2. User input #

A command can also use the facilities provided by Kojo by calling methods of `RunContext`, `TurtleMover`, `SCanvas`, `Figure`, etc.

| **Trait or Class**                      | **Call through...** |
|:----------------------------------------|:--------------------|
| `RunContext`                            | `ctx`               |
| `TurtleMover`                           | `turtle0`           |
| `SCanvas`                               | `tCanvas`           |
| `Figure`                                | `figure0`           |

For example, this command uses the run context to ask for an integer, and manipulates the default turtle in response:

```
    def askForDirection() {
      ctx.readInput("Which way? (0: straight ahead, 1: left, 2: right").toInt match {
        case 1 => turtle0.left()
        case 2 => turtle0.right()
        case _ => // no turn made
      }
    }
```

With this command in place, the script

```
repeat (10) {
    askForDirection()
    forward(50)
}
```

ten times asks the user for a direction, upon which the turtle turns that way and moves forward 50 steps.

Note that I didn't bother to add a synopsis or completion for this command --- it is completely optional to do so.

# Example command 3. Isometric drawing #

# Setting up command completion and synopsis #

After you've added a method to `Builtins`, you can optionally add a synopsis
for it to the text that is printed by the `help` command, and also add a
command completion for it.

Calling `UserCommand(name, args, synopsis)` performs both of these tasks.
`UserCommand.addSynopsis` just adds the help text, and
`UserCommand.addCompletion` just adds the completion.
`UserCommand.addSynopsisSeparator` adds an empty line to the help text.

## Adding a synopsis ##

Adding a synopsis can be done in two ways.  Passing a simple string to
`addSynopsis` simply appends that string to the help text.  Passing a command
name, a (possibly empty) sequence of parameter names, and a synopsis string
results in a string being constructed and added to the help text.  Example:

```
UserCommand.addSynopsis("foo", List("bar", "baz"), "The foo command does <this>.")
```

appends the string `"foo(bar, baz) - The foo command does <this>."` to the help text.

A synopsis string should be a short string without newlines.  A newline and two
spaces will be prepended to it automatically.

## Adding command completion ##

Adding command completion can also be done in two ways.  Passing (name, args)
as strings adds the tuple (name -> name + args) to the completion collection.
Passing (name, args) as a string and a (possibly empty) sequence of strings
constructs a tuple and adds it to the collection.  Example:

```
UserCommand.addCompletion("foo", List("bar", "baz"))
```

adds the tuple (`"foo"` -> `"foo(${bar}, ${baz})"`) to the completion collection.