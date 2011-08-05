// This story will Auto-Run in a moment (if it is loaded via the Stories Menu)

/*
 * Copyright (C) 2009 Anthony Bagwell
 * Copyright (C) 2009 Phil Bagwell
 * Copyright (C) 2011 Lalit Pant <pant.lalit@gmail.com>
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

// Click the Run button in the toolbar above to start the story
//
// =================================================================
//
// This Tutorial originally created by Anthony Bagwell for simplyscala.com
// and adapted for Kojo by Phil Bagwell

// Set Up Styles for tutorial pages

val pageStyle = "background-color:#99CCFF; margin:15px;font-size:x-small;"
val centerStyle = "text-align:center;"
val headerStyle = "text-align:center;font-size:110%;color:maroon;"
val codeStyle = "font-size:90%;"
val smallNoteStyle = "color:gray;font-size:95%;"
val sublistStyle = "margin-left:60px;"
showVerboseOutput()
retainSingleLineCode()
def pgHeader(hdr: String) =
    <p style={headerStyle}>
        {new xml.Unparsed(hdr)}
		{nav}
        <hr/>
        
    </p>
 
def tCode(cd:String)=Para(
<div style="background-color:CCFFFF;"> <pre style={codeStyle}> {cd} </pre> </div>,
code = {clearOutput;stRunCode(cd);stSetScript(cd)}
) 

var pages = new collection.mutable.ListBuffer[StoryPage]
var pg: StoryPage = _
var header: xml.Node = _

def link(page: String) = "http://localpage/%s" format(page)
val homeLink = <div style={smallNoteStyle+centerStyle}><a href={link("home#7")}>Start Page</a></div>

def nav={<div style={smallNoteStyle}>
		  <a href={link(("Menu").toString)}>Menu</a>
           </div>}

// Mark up support

implicit def toSHtm(s:String):SHtm={new SHtm(s,0)}
def escTrx(s:String) = s.replace("&" , "&amp;").replace(">" , "&gt;").replace("<" , "&lt;")
def row(c:SHtm *)={
    val r=c.map(x=>{new SHtm("<td>" + x.s + "</td>",0)}).reduce(_ + _) 
    new SHtm("<tr>" + r.s + "</tr>",0)
}
def table(c:SHtm *)={
    val r=c.reduce(_ + _) 
    new SHtm("""<table border="1">""" + r.s + "</table>",0)
}
//def toHtm(h:SHtm *)={h.reduce(_ + _)}

def tPage(title:String,h:SHtm *)={
<body style={pageStyle}>
     <p style={headerStyle}>
        {new xml.Unparsed(title)}
		{nav}
        <hr/>
    {new xml.Unparsed(h.reduce(_ + _).s)}
    
    </p>
</body>	
    
}
val codeExamples = new Array[String](1000)
var codeID = 0 
// Mark up DSL definitions class

class SHtm (var s:String,var t:Int){
def h2=new SHtm("<h2>" + escTrx(s) + "</h2>",0)    
def h3=new SHtm("<h3>" + escTrx(s) + "</h3>",0)
def i=new SHtm("<i>" + escTrx(s) + "</i>",0)    
def h1=new SHtm("<h1>" + escTrx(s) + "</h1>",0)
def p=new SHtm("<p>" + escTrx(s) + "</p>",0)
def b=new SHtm("<b>" + escTrx(s) + "</b>",0)
def c={
	codeID+=1
	codeExamples(codeID)=s
	new SHtm("""<hr/><div style=background-color:CCFFFF;"> <pre><code><a href="http://runhandler/example/""" + 
	codeID.toString +
	""" " style="text-decoration: none;font-size:x-small;">""" + escTrx(s) + """</a></code></pre><hr/></div>""",1)
}
def + (a:SHtm)={new SHtm(s + "\n" + a.s,0) }
}

// **********  Start of Tutorial  *************

pages += Page(
    name = "Menu",
    body =
        <body style={pageStyle}>
		<div style={pageStyle+centerStyle}>
		<h1>A Scala Tutorial</h1>
		</div>
		
		<div style={pageStyle}>
		<p>This tutorial was adapted for Kojo from the simplyscala.com version by Anthony Bagwell. You can move through the tutorial by clicking on the forward/next button on the bottom of this window. Or jump directly to a tutorial page through the following menu.</p>		
		<a href={link("GS")}>Getting Started</a> <br/>
        <a href={link("Flow")}>Flow Control If, Else and While</a> <br/>
		<a href={link("Literals")}>Literals, Integers, Floats and Strings</a> <br/>
		<a href={link("Functions")}>Functions</a> <br/>
		<a href={link("OandC")}>Objects and Classes</a> <br/>
		<a href={link("SSM")}>Simpler Syntax and Match(Switch)</a> <br/>
		<a href={link("BTree")}>More Advanced Matching - Binary Tree</a> <br/>
		<a href={link("STI")}>Static Typing and Inferencing</a> <br/>
		<a href={link("FAO")}>Functions are Objects</a> <br/>	
		<a href={link("MF")}>Mathematical Functions</a> <br/>
		<a href={link("OPA")}>Operator Precedence and Associativity</a> <br/>
		<a href={link("US")}>Using Strings</a> <br/>
		<a href={link("UM")}>Using Lists</a> <br/>
		<a href={link("UT")}>Using the Turtle</a> <br/>
		<a href={link("GAG")}>Graphics and Games</a> <br/>
		<a href={link("LM")}>Learning More</a> <br/>
		</div>
        </body>       
)


pages += Page(
name = "GS",
body = tPage("Getting Started",
"Getting Started".h2,
"Examples are run simply by clicking on them. They will be copied to the Kojo Script Editor and run for you. There you can modify the code and run again by clicking on the green triangle. Try with this one and then try changing the message and re-running it".p,
"""println("hello world")""".c, 
"The tutorial is broken into pages which can be stepped through by clicking on the arrow buttons on the bottom of the Tutorial window. Or you can jump to a specific page through the menu link at the top of each page. The Button with a square stops the tutorial.".p,
"Already executed code can be recalled by using the left and right arrows in the editor bar. These you can then edit in the normal way. You can try an example, then call it back, make changes, try different ideas and check how the syntax works. You can save (or load) your programs that are displayed in the Editor window by using the File options".p,
"Kojo comes with a nice set of features that can be fun to use while learning Scala. Read the Kojo Overview Story for more details on creating music, exploring mathematics and setting up physics demos. You can have fun using Scala to control the Turtle too. There are more examples and a list of Turtle commands in the Turtle section of the tutorial. Here is the first one.".p,
"""
  clear
  forward(100)
  right(120)
  forward(100)
  right(120)
  forward(100)
  right(120)
""".c,
"Scala has many features that will be familiar to programmers. Although you need little knowledge of any programming language to follow this tutorial previous experience will speed things up. Skip or move quickly through the parts you already understand.".p,
"So let's get started.".p, 
"Expressions".h2,
"Simple arithmetic expressions work much as you would expect. The usual operators and precedence apply. Parentheses can be used to control the order of application.".p
,
"1+2".c,
"3+4*(2-3)".c,
"""23%5  // Modulus or Division Remainder""".c,
"3.5*9.4+6/4".c,

"""Scala understands different number types. 3.5 is a "double" while 6 is an "integer". Notice in this last case that integer division results in a truncation to an integer value. Scala will coerce values to the appropriate types in a mixed expression where possible.""".p,
"""The result of an expression may be stored in a variable. Variable and other identifiers are made up of letters, numbers and symbols like * / + - : = ! < > & ^ | .  "Golf1", "helpLine" "*+" and "Res4" are all examples of identifiers or variable names. The result of an expression can be associated with a variable name. This association is signalled by a "var" or "val" keyword. "val" is used when the association is to be made once and not changed. For the time being you can use "var". Why are there "val" and "var"? "val" defines an immutable value and as you will learn later, is essential for functional style programming.""".p,
"val pixel=34+5".c,
"""var height=pixel+4
println(height)""".c,
"You may have noticed that the output window displays the type of the result of an expression, Int, Double and so on. Now try,".p,
"""pixel=10 // this gives an error, trying to change a val""".c,
"""height+=4
println(height)""".c,
"""Comments  can be added to code at the end of a line with // or over several lines using a /* and */ pair. All comments will be ignored.""".p,
"""
/*
  Example of multi-line comments
  Centigrade to Farenheit
*/
val tempf = 98.4
println( "tempc",(tempf-32)*5/9)  // more comments on the line
""".c,

"There are a full set of bit manipulation operators too. These allow you to do bitwise operations.".p,
"""3&2 // logical and""".c,
"""1|2 // logical or""".c,
"""1^2 // logical xor""".c,
"""1<<2 // shift left""".c,
"""-24>>2 // shift right and preserve sign""".c,
"""-14>>>2 // shift right and zero left bits""".c,
"So now you already have quite a powerful calculator but program flow control and functions for calculations that are to be repeated would be useful.".p    )
)

pages += Page(
    name = "Flow",
    body = tPage("Flow Control If, Else and While",
"Up until now you have written programs which start at the begining and execute all of the lines or expressions in order. Flow control means that you can control the order in which some or all of the code is executed based on some condition. This allows you to repeat some lines or skip over lines during the program execution.".p,
" You can specify a block of code by enclosing it in curly brackets {}. This block may contain any number of code lines or further sub-blocks of code. The last expression executed in a block determines the value of that block.".p, 
"There are a number of different flow control structures that enable you to control your programs flow.".p,  	
""""if (cond) block/expression else block/expression" is the first. If the condition is true the first block or expression will be evaluated while if it is false the block or expression following the 'else' will.""".p,
"""if(true) println("True") else println("Untrue")""".c,
"""The condition must be an expression that yields a boolean result, namely true or false. There are a number of comparison operators that do just that. Here are some of them that are useful with numbers. Later you will learn about others that are appropriate for other types of things.""".p,
"""1>2 // greater than""".c,
"""1<2 // less than""".c,
"""1==2 // equals""".c,
"""1>=2 // greater than or equal""".c,
"""1!=2 // not equal""".c,
"""1<=2 // less than or equal""".c,
"With the if statement if the condition is true then the expression before the else is evaluated otherwise the expression after it is evaluated. Unlike in some languages the if else evaluates to a value. To make the comparisons clear integer numbers have been used but these can be replaced by any valid expression.".p,
"""if(1>2) 4 else 5 // greater than""".c,
"""if (1<2) 6 else 7 // less than""".c,
"""val try1=if (1==2) 8 else 9 // equals""".c,
"""val isBook = 6>=3
val price=16
val vol=10
val sale=if (isBook)price*vol else price/vol""".c,
"You may need to combine individual conditions in some way. There are two operators which do this for you. && meaning And. || meaning Or. These combine boolean values and are not equivalent to & | which combines bit values.".p,
"""val isBook = 6>=3
val price=16
val vol=10
val sale=if (((isBook)&&(price>5))||(vol>30))price*vol else price/vol""".c,
"while".h2,
""""while (cond) block/exp" allows you to repeat a block of code or an expression while the condition is true. First an expression.""".p,
"""var total=18
while(total < 17) total+=3""".c,
""""do block/exp while (cond)" allows you to repeat a block of code or an expression while the condition is true.  The condition is evaluated after doing each iteration.""".p,
"""var total=18
do{
  total+=3
  }while (total < 17)""".c,
"Notice in this case that total end up as 21 rather than 18 in the previous example with the while. Here is while being used to calculate the Greatest Common Divisor or GCD.".p,

"""// find the greatest common divisor
var x = 36
var y = 99
while (x != 0) {
    val temp = x
    x = y % x
    y = temp
    }
println("gcd is",y)
""".c,

"for".h2,
""""for (range) block/exp" allows you to repeat a block of code for all the values in a range or iterate through the members of a collection.""".p,
"""for(i <- 1 to 4) println("hi five")""".c,
"The value of i takes all the values from 1 to 4. If you want the end range value not to be included the until version should be used.".p,
"""for(i <- 1 until 4) {
  val sqr = i*i
  println(i,sqr)
  }""".c,
"Multi-dimensional iterations are elegantly handled using multiple ranges. Notice that the two ranges are separated by a semi-colon.".p,
"for(i <- 1 until 4 ; j <- 1 to 3) println(i,j)".c,
""""for" may also be used to iterate through collections. A string is a collection of characters so "for"  may be used to iterate through it.""".p,
"""for(c<-"hello")println(c)""".c,
"Now use the Turtle canvas plotting capabilities of Kojo to display a graph of a*x^2+b*x+c using 'for'".p,
"""
clear
def poly(x:Double)=0.001*x*x+0.5*x+10
gridOn();axesOn()
val range=200
setPosition(-range,poly(-range))
for(x <- -range+10 to range; if (x % 10 == 0)) moveTo(x, poly(x))
""".c,
"Now turn off axes.".p,
"gridOff();axesOff();clear()".c
))

pages += Page(
    name = "Literals",
    body = tPage("Literals, Integers, Floats and Strings",
"Literals allow you to define the value of one of the basic types in your code. They are pretty much the same as those you find in Java and similar to other languages".p,

"Base Types".h2,
"Scala has a set of base types that are predefined. They are summarized as follows.".p,
table(
row("Byte","8-bit signed 2's complement integer (-128 to 127 inclusive)"),
row("Short","16-bit signed 2's complement integer (-32,768 to 32,767 inclusive)"),
row("Int","32-bit signed 2's complement integer (-2,147,483,648 to 2,147,483,647 inclusive)"),
row("Long","64-bit signed 2's complement integer (-2^63 to 2^63-1, inclusive)"),
row("Float","32-bit IEEE 754 single-precision float"),
row("Double","64-bit IEEE 754 double-precision float"),
row("Char","16-bit unsigned Unicode character"),
row("String","a sequence of Unicode characters"),
row("Boolean","true/false")),
"Integers".h2,
"There are four types of integer namely Int, Long, Short, and Byte. You can use literals expressed in different bases, they are decimal, hexadecimal, and octal. You signal which form you are using by the first characters.".p,
"Decimal(base 10): Any number starting with a non-zero digit.".p,
"17".c,
"298".c,
"Hexadecimal(base 16): starts with a 0x or 0X and is followed by the hex digits 0 to 9, a to f or A to F".p, 
"""0x23  //hex = 35 dec""".c,
"""0x01FF  //hex = 511 dec""".c,
"""0xcb17 //hex = 51991 dec""".c,
"Octal(base 8): starts with a 0 and is followed by the octal digits 0 to 7".p,
"""023 // octal = 19 dec""".c,
"""0777  // octal = 511 dec""".c,
"""0373  // octal = 251 dec""".c,
"""By default these will be created as type Int. You can force them to type Long by adding the letter "l" or "L".""".p,
"""0XFAF1L // hex long = 64241""".c,
"035L".c,
"You can assign literals to Short or Byte variables. However, the value must be in the appropriate range for that type.".p,
"val abyte: Byte = 27".c,
"val ashort: Short = 1024".c,
"""val errbyte: Byte = 128 // Error - not in range -128 to 127""".c,
"Floating point".h3,
"Floating point literals are numbers containing a decimal point. They must start with a non-zero digit and can be followed by E or e that prefixes an exponent indicating the power of 10 to use. Some examples are:-".p,

"9.876".c,
"val tiny= 1.2345e-5".c,
"val large = 9.87E45".c,
"""By default floating literals are created as type Double but you can force them to type Float by adding the letter "f" or "F". Optionally "d" or "D" can be appended to a floating literal.""".p, 
"val sma = 1.5324F".c,
"val ams = 3e5f".c,
"Character".h3,
"Character literals are specified by any Unicode character in single quotes.".p,
"val chr = 'A'".c,
"You may also specify its value in several other ways.".p,
"""Octal: An octal number between '\0' and '\377'.""".p,
"""val chr = '\101'  // code for A""".c,
"Unicode:A hexidecimal number between '\134u0000' and '\134uFFFF'".p,
"""val chra = "'\134u0041 is an A" """.c,
"""val chre = "\134u0045 is a E" """.c,
"Finally, there are also a few character literals represented by special escape sequences. These all start with a back slash. See reference for complete list.".p,
"Strings".h3,
"A string literal is a sequence of characters enclosed in double quotes:".p,
"""val helloW = "hello world" """.c,
"To include some special characters in your string it is convenient to use 'escape' sequences. These start with a \\ and are followed by a character designating the required character.".p,
table(
  row("""\n""", "line feed","""\b""", "backspace","""\t""",      "tab","""\f""", "form feed"),
  row("""\r""", "carriage return","""\" """, "double quote", """\'""", "single quote","""\\""", "backslash")
),
"""val someEsc = "\\\"\'" """.c,
"Scala includes a special syntax to avoid these multiple escape characters. if you start and end a string with triple quotes (\"\"\") then all the characters such as newlines, quotation marks, and special characters are treated just like others.".p,
"println(\"\"\"Welcome to Simply Scala.\nClick 'About' for more information.\"\"\")".c,
"Boolean".h3,
"The Boolean type has two possible values and the literals are true or false:".p,
"val isBig = true".c,
"val isFool = false".c
))

pages += Page(
    name = "Functions",
    body = tPage("Functions",
"""Functions give you the capability to define calculations that you wish to repeat. A function is defined using the "def" key word. The example that follows creates a function that returns an integer, the max value for the two integer arguments. Typically a function will return a value of some type. However, for some functions no return value is expected and in this case the return will be of type "Unit" meaning no value or void.""".p,
"""def max(x: Int, y: Int): Int = {
    if (x > y) x
    else y
    }
""".c,
"""The name of the function,"max" in this case,  follows the "def" then the parameters with their associated types within parentheses. Type annotation is added after the parameter name and preceded by a colon.This function has two parameters of type Int. Then the return type is defined following the colon, again Int in this case. Finally there is an equal sign and the function body enclosed in curly brackets. Once you have defined a function you can use it by calling it with the appropriate parameters.""".p,
"max(6,7)".c,
"Functions can make recursive calls to themselves. Recursive functions form an alternative way of controlling iterations. In the following function that computes the Greatest Common Divisor no variables are required for intermediate values.".p,

"""def gcd(x: Long, y: Long): Long =
    if (y == 0) x else gcd(y, x % y)
""".c,
"""Compare this to the earlier version written with a "while" loop.""".p,
"gcd(96,128)".c,
"Now here is another recursive function, it calls itself twice to create the branches of a rather pretty binary tree using the Turtle. The recursions stop when the distance is less than or equals to 4.".p,
"""def tree(distance: Double) {
    if (distance > 4) {
        setPenThickness(distance/7)
        setPenColor(color(distance.toInt, Math.abs(255-distance*3).toInt, 125))
        forward(distance)
        right(25)
        tree(distance*0.8-2)
        left(45)
        tree(distance-10)
        right(20)
        back(distance)
    }
}

clear()
invisible()
setAnimationDelay(60)
penUp()
back(200)
penDown()
tree(90)
""".c,

"In Scala, by convention, functions that have side effects and return Unit are called 'Procedures', functions that are part of a class definition are called 'Methods' or 'Operators'. When we think of 'mathematical' expressions we tend to think of operators. When we think in OO terms then we tend to think methods. In Scala the two are really just the same. The next section will introduce classes and objects".p
)
)

pages += Page(
    name = "OandC",
    body = tPage("Objects and Classes",
"Everything is an Object".h2,
"Scala is an Object Oriented language. The underlying premis, like other OO languages, is that there are objects that contain state and this state is manipulated or accessed by means of Methods. Method is the name given to functions that form the programmers interface to Objects. Often it is nice to refer to Methods performed by some object, like the Turtle for example, as commands. 'forward(10)' or 'turn(30)'. ".p, 

"Objects are defined by the means of a class hierarchy. When you define a class you are also defining a new type of object. This new type and those already defined such as Int or Double are treated in a uniform way. The benefits of this uniformity, everything is an object, will soon become apparent. You can start by defining an object that represents a point.".p,

"""class Point {
var x=0
var y=0
}
""".c,

"""This is an abstract definition for the object. An instance of of the object can be created by using the "new" keyword.""".p,
"val p=new Point".c,
"""The variables within an Object can be accessed by using "." """.p,
"""p.x=3
p.y=4
""".c,
"You can retrieve the state in the same way.".p,
"println(p.x,p.y)".c,
"Setting the variables individually each time an instance of a new point is created is time consuming. By adding parameters to the class definition then the instance will be constructed with the desired values.".p,
"class Point( var x:Int,var y:Int)".c,

"And then create a point. Test it with println as before".p,
"val p=new Point(3,4)".c,
"Now suppose you would like to add two points together to create a new point. The equivalent of vector addition. Then you may add an appropriate method to do so.".p,
"""class Point(var x:Int,var y:Int){
    def vectorAdd(newpt:Point):Point={
          new Point(x+newpt.x,y+newpt.y)
          }
   }
""".c,
"Given this definition two points can be created and their vector addition made.".p,
"""val p1=new Point(3,4)
val p2=new Point(7,2)
val p3=p1.vectorAdd(p2)
println(p3.x,p3.y)
""".c,

"""So far this looks pretty much as a Java programmer would expect. However,it would be more natural to write "p1+p2". In Scala you can do so. Method names can be composed using almost all of the non-alphanumeric symbols. A few combinations are reserved and you will get an error if you try to use them. So the class can be rewritten to use "+" and a method for "-" created too.""".p,
"""class Point(var x:Int,var y:Int){
def +(newpt:Point):Point={
  new Point(x+newpt.x,y+newpt.y)
  }
def -(newpt:Point):Point={
  new Point(x-newpt.x,y-newpt.y)
  }
override def toString="Point("+x+","+y+")"
}
val p1=new Point(3,4)
val p2=new Point(7,2)
val p3=new Point(-2,2)
""".c,
"""val p4=p1+p2-p3
println(p4.x,p4.y)
""".c,
"With this arrangement you can create a very natural looking vector calculus and a whole lot more readable than the traditional equivalent.".p,
"In Scala all classes are created with a default 'toString' method which produces a string representation of the object, by default its reference. You can override that method to give a more user friendly representation as has been done with Point above.".p,  

"""In Scala there is a further simplification of the class creation syntax with the introduction of "case classes". Taking the Point class above it can be expressed as a case class.""".p,
"""case class Point(x:Int,y:Int){
def +(newpt:Point)=Point(x+newpt.x,y+newpt.y)
def -(newpt:Point)=Point(x-newpt.x,y-newpt.y)
override def toString="Point("+x+","+y+")"
}
val p1=Point(3,4)
val p2=Point(7,2)
val p3=Point(-2,2)
""".c,
"p1+p2-p3".c,

"""You see that the "new" is not required to create a new instance. The Scala compiler recognises that the new instance is required and creates it for you. Too note that in this case the curly brackets have been dropped in the "def". They are not required as the right hand side of the "def" is a simple expression and not statements. This is a general property of "def" and not just limited to case classes. Lastly see that the return type has been dropped in the function. Scala can infer what this is from the function definition.""".p
)
)

pages += Page(
    name = "SSM",
    body =tPage("Simpler Syntax and Match(Switch)",
"Everything is an object. As such, you may have wondered why the example above was not written more in Java style.".p,
"p1.+(p2.-(p3))".c,

"This is one of the nice syntactic features of Scala that helps to give clarity and uniformity to your code. You may leave out the parentheses and dots as Scala can infer were they belong. It is this carefully thought out syntax that allows you to implement Domain Specific Languages (DSLs). So all objects, including numbers are just objects with methods. For example you can perform the + method on the number 1 with the extended syntax too.".p, 
"(1).+(2)".c,
"All the base types are in fact objects too that can be used and extended just like any other object.".p, 

"""Note the first set of parentheses around the "1" are required here to remove an ambiguity. "1." is a type Double and the result would be type Double rather than Int. Try making the change.""".p,
"switch - Pattern match".h2,
"You may already be familiar with the 'switch' with 'case' form used in many languages to allow multi-way branching on value. In Scala this concept is extended to provide full algebraic pattern matching using 'match'. However, the simple switch on value can also be represented easily with match.".p,

"""def  decode(n:Int){
  n match {
    case 1 => println("One")
    case 2 => println("Two")
    case 5 => println("Five")
    case _ => println("Error")
  }
}
""".c, 
"decode(2)".c,
"The '=>' symbol is used to separate the match pattern from the expression or block to be evaluated. The '_' symbol is used in Scala to mean wild-card or in this case match anything. The last case statement behaves like default in the classical switch. 'match', like most other functions returns a value so the above function could be written more concisely.".p,

"""def  decode(n:Int){
  println(n match {
    case 1 => "One"
    case 2 => "Two"
    case 5 => "Five"
    case _ => "Error"
    }
  )
}
""".c,

"decode(3)".c,
"Unlike the traditional Java 'switch' the above mapping can easily be reversed.".p,
"""def  encode(s:String){
  println(s match {
    case "One" => 1
    case "Two" => 2 
    case "Five" => 5
    case _ => 0
    }
  )
}
""".c, 
"""encode("Five")""".c
)
)

pages += Page(
    name = "BTree",
    body =tPage("More Advanced Matching - Binary Tree",
"For the next example a binary tree is defined with internal nodes and values stored on leaf nodes. A function is created to find the value in the tree associated with the given key. Now see how the case pattern matching is used to determine the node type and bind names to the parameters, the lower case letters k,l,r and v. These are called pattern variables. They may also be constants, indicated by a starting uppercase letters or a literal. In which case the constant value is matched directly with the instances field value.".p,
"The structure has been created by defining an abstract class and defining the internal and external nodes as subclasses. To inherit the properties of a class the sub-class uses the 'extends' keyword to show that the class is a sub class of the other. Both the internal and external nodes are TreeN nodes.".p,
"The keyword 'abstract' defines a class as not being able to be created as an instance using.".p,     
"""abstract class TreeN
case class InterN(key:String,left:TreeN,right:TreeN) extends TreeN
case class LeafN(key:String,value:Int) extends TreeN

def find(t:TreeN,key:String):Int={
     t match {
         case InterN(k,l,r) => find((if(k>=key)l else r),key)
         case LeafN(k,v) => if(k==key) v else 0
    }
}
// create a binary tree
val t=InterN("b",InterN("a",LeafN("a",1),LeafN("b",2)),LeafN("c",3)) 
/*       [b]
         / \
       [a] c,3
       / \
     a,1 b,2 
*/	 
""".c,
"Note the use of the case class constructor to efficiently create a test binary tree. Now you can try the find.".p,
"""find(t,"a")""".c,
"""find(t,"c")""".c,
"You may like to try wrapping this up into a Binary tree class, including member methods for adding, finding and deleting entries.".p, 

"Suppose, for some reason, you would like to hide the key 'c' during the find. A simple modification to the find function does this nicely and illustrates the use of a constant match.".p,    
"""def find(t:TreeN,key:String):Int={
     t match {
         case InterN(k,l,r) => find((if(k>=key)l else r),key)
		 case LeafN("c",_) => 0
         case LeafN(k,v) => if(k==key) v else 0
    }
}
""".c,

"Notice the use of '_' as a wild card to match any value and remember that the case statements are evaluated in order.".p) 
)
pages += Page(
    name = "STI",
    body = tPage("Static Typing and Inferencing",
"Scala is a statically typed language, all the variables and functions have types that are fully defined at compile time. Using a variable or a function in a way inappropriate for the type will give compiler type errors. This means that the compiler can find many unintended programming errors for you before execution. However, you will have noticed that in the examples there are few type definitions. This is because Scala can usually infer what type a variable must be from the way you have used it.".p, 

"For example, if you write 'val x=3' then the compiler infers that 'x' must be type Int as '3' is a integer literal. In a few situations the compiler will not be able to decide what you intended and will generate a type ambiguity error. In these cases you simply add the intended type annotation.".p,

"In general you must define function parameter types, however the compiler can usually infer the return type so it can usually be omitted. The exception to this rule is if you define recursive functions, ones that call themselves, you must define the return type.".p,

"Type inferencing dramatically reduces the amount of typing you must do and gives a great deal more clarity to the code. It is this type inferencing that gives Scala the feel of being dynamically typed.".p

)
)

pages += Page(
    name = "FAO",
    body = tPage("Functions are Objects",
"Functions are Objects too".h2,
"In Scala everything is an object and so are functions. They may be passed as arguments, returned from other functions or stored in variables. This feature of Scala enables some very concise and elegant solutions to common programming problems as well as allowing extremely flexible program flow control structures.  The Scala Actors make heavy use of this capability for supporting concurrent programming. However, list manipulation provides a good starting point for an introduction. For example, how do you find all the odd integers in a list? Here is a suitable list.".p,
"val lst=List(1,7,2,8,5,6,3,9,14,12,4,10)".c,
"Three list methods head,tail and :: will be used in these examples. From these you will see how many other useful list functions can be created. 'head' returns the first or leftmost element '1' in the above list, 'tail' returns the list with the first element, the '1' removed and :: returns a new list with an element added.".p,  
"""def odd(inLst:List[Int]):List[Int]={
  if(inLst==Nil) Nil 
  else if(inLst.head%2==1) inLst.head::odd(inLst.tail) 
  else odd(inLst.tail)
}""".c,
"odd(lst)".c,  
"A simple solution and to change this to return a list of the even integers is simple too.".p,
"""def even(inLst:List[Int]):List[Int]={
  if(inLst==Nil) Nil 
  else if(inLst.head%2==0) inLst.head::even(inLst.tail) 
  else even(inLst.tail)
}""".c,
"even(lst)".c,     
"However, by passing a function that encapsulates the filtering condition as an argument a more general solution appears.".p,
"First the filter condition function is defined.".p,
"def isodd(v:Int)= v%2==1".c,
"And then the modified filter function itself, a parameter is added to pass the function, just like any other object. Notice the form of the type declaration. The type will be a function with one Int parameter and will return a Boolean. Only functions of this type can be passed as arguments. In the function body 'cond' is used just like any other function.".p,
"""def filter(inLst:List[Int],cond:(Int)=>Boolean):List[Int]={
  if(inLst==Nil) Nil 
  else if(cond(inLst.head)) inLst.head::filter(inLst.tail,cond) 
  else filter(inLst.tail,cond)
}""".c,
"filter(lst,isodd)".c,
"Although the even case can be added in the same way a more concise version can be created using Anonymous functions. The def and name are dropped, creating an anonymous function definition that can be used directly as an argument. Notice the use of the => to separate the function parameter list from the body of the function.".p,
"filter(lst,(v:Int)=> v%2==0)".c,
"This filter function now does just what was required.".p, 
"Taste of Generic Programming".h2,
"Suppose you now want to create a filter for type Double. You could go through the code and simply replace all the type definitions of Int with Double. You would perhaps then call the new function filterD. For each new type you would go through the same exercise and end up with many versions of the same thing.".p,

"In Scala you can avoid this duplicated effort nicely by using a generic type in place of the actual ones. It is like using a variable to represent the type instead of the an actual type. The actual types get filled in later by the compiler where you make a call to the function.".p,

"Here 'T' is used, though any letters will do, in place of Int in filter and the function name is annotated with [T] to indicate that this is a generic function then the example becomes. The returned list has been reversed to give the same order as the input list.".p,

"""def filter[T](inLst:List[T],cond:(T)=>Boolean):List[T]={
  if(inLst==Nil) Nil 
  else if(cond(inLst.head)) inLst.head::filter(inLst.tail,cond) 
  else filter(inLst.tail,cond)
}""".c,
"filter(lst,(v:Int)=> v%2==0)".c,
"Then you can try using this generic version of filter with a list of Doubles to find all the elements greater than 5.".p,
"""val lstd=List(1.5,7.4,2.3,8.1,5.6,6.2,3.5,9.2,14.6,12.91,4.23,10.04)
filter(lstd,(v:Double)=> v>5)""".c,
"Or with a list of strings to find those with a length greater than 3".p, 
"""val lsts=List("It's","a","far","far","better","thing","I","do","now")
filter(lsts,(v:String)=> v.length>3)""".c,
"In the reference you will find that lists have a filter function so you could equally write.".p, 
"lsts.filter((v:String)=> v.length>3)".c,
"Generics will be explored more later but now, more about functions as objects.".p,
"More on 'Functions are objects'".p,
"The ability to carry out comprehensions, namely applying a function(s) for all members of a collection, is very powerful and leads to some very compact coding.".p, 

"Many times you will want to pass quite simple functions as arguments and Scala has some nice shorthand that you will find useful. You have already seen that inferencing can help. The lst.filter example can be simplified as the compiler knows the argument must be a function with a String type. This allows you to drop the parentheses and type annotation.".p,   
"lsts.filter(v=>v.length>3)".c,   
"Since binary operators are frequently used, Scala provides you with an even nicer shorthand. The anonymous function '(x,y)=>x+y' can be replaced by '_+_' . Similarly 'v=>v.Method' can be replaced by '_.Method'. The _ acts as a place holder for the arguments and you are saved the chore of inventing repetitive boiler plate names. So once more lst.filter can be simplified to.".p,
"lsts.filter(_.length>3)".c, 
"Reading other peoples Scala code you will come across these short forms quite often. Sometimes you must use the longer forms.".p,
 
"Here are some more examples of list manipulations with functions as arguments taken from the Reference section.".p,
"flatMap".h3,
"lsts.flatMap(_.toList)".c,
"You see that flatMap takes a list and uses your given function to create a new list. In this case it 'flattens' your list of words into characters and concatenates these sublists to produce the result.".p,
"sort".h3,
"The words could be sorted in ascending order using the sort method.".p,
"lsts.sort(_<_)".c, 
"Or descending order".p,
"lsts.sort(_>_)".c,
"Or ignoring the case of the characters.".p,
"lsts.sort(_.toUpperCase<_.toUpperCase)".c,
"By passing the appropriate function you can create a whole family of sorts without having to re-code the sort itself. This is a really neat way to vary behavior without rewriting the whole method.".p,
"fold".h3,
"foldLeft and foldRight allow you to combine adjacent list elements using an arbitrary function you pass. The process either starts from the left of the list or the right and provide a starting value. The option to start left or right allows you to choose which order you want to pass through the list. Starting with the list.".p,
"""val lst=List(1,7,2,8,5,6,3,9,14,12,4,10)
lst.foldLeft(0)(_+_)""".c,
"With the passed '_+_' function the foldLeft function starts with 0 adds 1 to it. Next 7 is then added to this result and so on through the rest of the list.".p,
"removeDuplicates".h3,
"Suppose you wish to know the unique letters that are in a list of words such as:-".p,
"""val lstw=List("Once","more","unto","the","breach")
lstw.flatMap(_.toList).map(_.toUpperCase).removeDuplicates.sort(_<_)""".c,
"'flatMap' flattens all the words into a list of letters. 'map' converts them all to uppercase. All the duplicates are then removed and the result sorted into ascending order.".p,

"Tuples".h2,
"It is often useful to return more than one value from a function. You can of course always create a class to do this but the typing overhead of making the definition becomes onerous. Scala allows you to create what are in effect anonymous classes in line. A tuple is simply a set of values enclosed in paretheses. A tuple can contain a mixture of types.".p,
"(3,'c')".c,  
"The Tuple is an object so can accessed using the dot notation but since the fields have no names they are accessed using a name created with an underscore followed by the position index.".p, 
"""(3,'c')._1
(3,'c')._2""".c,
"However the tuple deconstruction is more frequently used. For example,".p,
"val (i,c)=(3,'a')".c,
"You will see how this is used in the following program to create a letter frequency table from our words list.".p,

"The approach taken is to first flatten 'lstw', convert to upper case and then sort the the list of characters.".p,
"Then a fold will be used to count the duplicate letters. You have already seen that a fold takes an input value and combines it successively with each list element to produce a new input value. In our case while the characters are the same a count needs to be incremented and when they differ the character and count need to be added to the freqency table.".p,

"The objective is to produce a list of tuples with two entries letter, Count. First create a sorted list of characters.".p, 
    
"val ltrs=lstw.flatMap(_.toList).map(_.toUpperCase).sort(_<_)".c,
"Now the fold. The initial condition is an empty output frequency table, a list of tuples. The fold will expect a function that takes a list of tuples combines with a Char, the next character in the list, and returns a list of tuples.".p,

"""	
ltrs.foldLeft(List[(Char,Int)]()){
  case ((prevchr,cnt)::tl,chr) if(prevchr==chr) =>(prevchr,cnt+1)::tl
  case (tbl,chr) => (chr,1)::tbl
}""".c,

"To understand what is happening here perhaps some new pieces of Scala syntax need to be understood.".p,

"First, a sequence of cases (i.e. alternatives) in curly braces can be used anywhere a function literal can be used. It acts as a function with input parameter(s) and an implied match at the begining of the block. You can consider this as a convenient shorthand for the standard anonymous function form which would have been:-".p, 
"(a,b)=>(a,b) match {case ...}".b,    

"Second, you saw earlier that case is used to pattern match. Here that pattern matching is used to deconstruct our arguments and the list of tuples. '(prevchr,cnt)::tl' matches a list with a head element and tail and associates the names with those items. While 'chr' refers to the second fold arguement, the next list character.".p,

"Third, case syntactic pattern matches may not be a precise constraint, as is the case here so a pattern guard can be used. The guard starts with an if and can contain an arbitrary boolean expression. In this case the guard is used to restrict the case to just those where the previous character is the same as the current one.".p,

"So now we can read the expression as: For each character in the input list, if there is already an entry for it in the head of the frequency table, replace the head with a new head with the count incremented. In the other case simply add a new entry to the frequency table with a count of one.".p,        

"Of course you could always use a more Java like approach too.".p,

"""def iFreqCount(in:List[Char]):List[(Char,Int)]={
  var Tbl=List[(Char,Int)]()
  if(in.isEmpty)Tbl
  else{
    var prevChr=in.head
    var nxt=in.tail
    var Cnt=1
    while (!nxt.isEmpty){
      if(nxt.head==prevChr)Cnt+=1
      else {
        Tbl=(prevChr,Cnt)::Tbl
        Cnt=1
        prevChr=nxt.head
        }
      nxt=nxt.tail
      }
    (prevChr,Cnt)::Tbl
  }
}""".c,
"iFreqCount(ltrs)".c,

"You will find this ability to treat functions as objects is very useful in all sorts of programming tasks, for example, passing callback functions in event driven IO, passing tasks to Actors in concurrent processing environments or in scheduling work loads. They often result in far more concise code as you see.".p, 

"You will find the ability to use immutable data functions simplifies concurrent applications. It is well worth learning how to create them.".p
)
)


pages += Page(
    name = "MF",
    body = tPage("Mathematical Functions",
"Maths"h3,
"Here are some useful Maths functions. Remember to import them before using.".p,
"import Math._".c,
"Math Constants".c,
"Two common constants are defined in the Math class.".p,
table(
  row("E".c,"Value of e, 2.718282..., base of the natural logarithms."),
  row("Pi".c,"Value of pi, 3.14159265 ....")
),
"Trigonometric Methods".h3,
"All trigonometric method parameters are measured in radians, the normal mathematical system of angles, and not in degrees, the normal human angular measurement system. Use the toRadians or toDegrees methods to convert between these systems, or use the fact that there are 2*PI radians in 360 degrees. In addition to the methods below, the arc methods are also available.".p,
table(
  row("sin(Pi/6)".c,"sine of P1."),
  row("cos(Pi/6)".c,"cosine of P1."),
  row("tan(Pi/6)".c,"tangent of P1."),
  row("toRadians(45)".c,"P1 (angle in degrees) converted to radians."),
  row("toDegrees(Pi/2)".c,"P1 (angle in radians) converted to degrees.")
  ),
"Exponential Methods".h3,
"The two basic functions for logarithms and power are available. These both use the base e (Math.E) as is the usual case in mathematics.".p,
table(
  row("exp(Pi)".c,"e (2.71...) to the power P1."),
  row("pow(6,3)".c,"P1 raised to P2."),
  row("log(10)".c,"logarithm of P1 to base e.")
  ),
"Misc Methods".h3,
table(
  row("sqrt(225)".c,"square root of P1."),
  row("abs(-7)".c,"absolute value of P1 with same type as the parameter: int, long, float, or double."),
  row("max(8,3)".c,"maximum of P1 and P2 with same type as the parameter: int, long, float, or double."),
  row("min(8,3)".c,"minimum of P1 and P2 with same type as the parameter: int, long, float, or double.")
  ),
"Integer Related Methods".h3,
"The following methods translate floating point values to integer values, although these values may still be stored in a double.".p,
table(
  row("floor(3.12)".c,"closest integer-valued double which is equal to or less than P1."),
  row("ceil(3.12)".c,"closest integer-valued double which is equal to or greater than P1."),
  row("rint(3.51)".c,"closest integer-valued double to P1."),
  row("round(3.48)".c,"long which is closest in value to the double P1."),
  row("round(2.6)".c,"int which is closest in value to the float P1.")
  ),
"Random Numbers".h3,
table(
row("random".c,"Returns a pseudo random number in the range, 0.0 <= x < 1.0.")
)
)
)

pages += Page(
    name = "OPA",
    body = tPage("Operator Precedence and Associativity",
"Operator Precedence".h3,
"Operators are any valid identifier but their precedence within expressions is according to table below, highest precedence first. The precedence of multi-character operators is defined by the first character. For example an operator +* would have the precedence given by the + sign.".p, 
"(all other special characters)".p,
"""( * / % , + - : , = ! , < > , & , ^ , | ) highest precedence on left""".p, 
"(all letters)".p,
"(all assignment operators) eg = += -= *= /= etc".p,
"Operator Associativity".h3,
"The associativity of an operator in Scala is determined by its last character. Any method that ends in a ':' character is invoked on its right operand, passing in the left operand. Methods that end in any other character are the other way around. They are invoked on their left operand, passing in the right operand. So a * b yields a.*(b), but a ::: b yields b.:::(a).".p
)
)

pages += Page(
    name = "US",
    body = tPage("Using Strings",
"Functions".h2,
"Strings".h3,
"String manipulation is a frequent task. Here are some useful functions and definitions. In the descriptions all the function parameters are labeled in order so will be of the form P1.functionName(P2,P3...). You will find that other sequences like lists have similar methods.".p,  
"Escape characters for strings.".h3,
table(
  row("""\n""", "line feed","""\b""", "backspace","""\t""",      "tab","""\f""", "form feed"),
  row("""\r""", "carriage return","""\" """, "double quote", """\'""", "single quote","""\\""", "backslash")
),
"Length".h3,
table(
 row(""""four".length""".c,"length of the string P1.")
),
"Comparison".h3, 
"note: use these instead of == and !=".p,
table(
  row(""""high".compareTo("higher")""".c,"compares to P1. returns <0 if P1<P2, 0 if ==, >0 if P1>P2"),
  row(""""high".compareToIgnoreCase("High")""".c,"same as above, but upper and lower case are same"),
  row(""""book".equals("loot")""".c,"true if the two strings have equal values"),
  row(""""book".equalsIgnoreCase("BOOK")""".c,"same as above ignoring case"),
  row(""""book".startsWith("bo")""".c,"true if P1 starts with P2"),
  row(""""bookkeeper".startsWith("keep",4)""".c,"true if P2 occurs starting at index P3"),
  row(""""bookmark".endsWith("ark")""".c,"true if P1 ends with P2")
),
"Searching".h3,
"""Note: All "indexOf" methods return -1 if the string/char is not found. Indexes are all zero base.""".p,
table(
  row(""""rerender".contains("ren")""".c,"True if P2 can be found in P1."),
  row(""""rerender".indexOf("nd")""".c,"index of the first occurrence of String P2 in P1."),
  row(""""rerender".indexOf("er",5)""".c,"index of String P2 at or after position P3 in P1."),
  row(""""rerender".indexOf('r')""".c,"index of the first occurrence of char P2 in P1."),
  row(""""rerender".indexOf('r',4)""".c,"index of char P2 at or after position i in P1."),
  row(""""rerender".lastIndexOf('e')""".c,"index of last occurrence of P2 in P1."),
  row(""""rerender".lastIndexOf('e',4)""".c,"index of last occurrence of P2 on or before position P3 in P1."),
  row(""""rerender".lastIndexOf("er")""".c,"index of last occurrence of P2 in P1."),
  row(""""rerender".lastIndexOf("er",5)""".c,"index of last occurrence of P2 on or before position P3 in P1.")
  ),
"Getting parts".h3,
table(
row(""""polarbear".charAt(3)""".c,"char at position P2 in P1."),
row(""""polarbear".substring(5)""".c,"substring from index P2 to the end of P1."),
row(""""polarbear".substring(3,5)""".c,"substring from index P2 to BEFORE index P3 of P1.")
),
"Creating a new string from the original".h3,
table(
  row(""""Toni".toLowerCase""".c,"new String with all chars lowercase"),
  row(""""Toni".toUpperCase""".c,"new String with all chars uppercase"),
  row(""""  Toni   ".trim""".c,"new String with whitespace deleted from front and back"),
  row(""""similar".replace('i','e')""".c,"new String with all P2 characters replaced by character P3."),
  row(""""ToniHanson".replace("on","er")""".c,"new String with all P2 substrings replaced by P3.")
),
"Methods for Converting to String".h3,
table(
  row("String.valueOf(List(1,2,3))".c,"Converts P2 to String, where P2 is any type value (primitive or object).")
)
)
)


pages += Page(
    name = "UL",
    body = tPage("Using Lists",
"Lists".h2,
"Lists provide a common sequence structure that is used for many functional style algorithms. The following functions enable Lists to be manipulated easily and effectively. The first example creates the List that is used for other examples.".p,
 
"Note:  _+_ is a shorthand for an anonymous function x,y=>x+y. Since binary operators are frequently used this is a nice abbreviation. Similarly _.Method is shorthand for v=>v.Method".p,
 
table(
  row("""val lst = "Tempus" :: "fugit" ::
  "irreparabile" :: Nil""".c,"""Creates a new List[String] with the three values "Tempus", "fugit", and "irreparabile" """), 
  row("List()".c,"or use Nil for the empty List"),    
  row("""List("Time", "flys", "irrecoverably")""".c,"""Creates a new List[String] with the three entries "Time", "flys", and "irrecoverably" """),
  row("""List("tick", "tock") ::: List("cuk", "oo")""".c,"Operator that concatenates two lists"),  
  row("lst(2)".c,"Returns the item at 0 based index 2 in lst"),
  row("lst.count(str => str.length == 5)".c,"Counts the string elements in lst that are of length 5"),
  row("""lst.exists(str => str == "irreparabile")""".c,"""Determines whether a string element exists in lst that has the value "irreparabile" """),  
  row("lst.drop(2)".c,"""Returns lst without the first 2 elements (returns List("irreparabile"))"""),
  row("lst.dropRight(2)".c,"""Returns lst without the rightmost 2 elements (returns List("Tempus"))"""),
  row("lst.filter(str => str.length == 5)".c,"Returns a list of all elements, in order, from lst that have length 5"),  
  row("lst.flatMap(_.toList)".c,"Applies the given function f to each element of this list, then concatenates the results"),
  row("""lst.forall(str =>str.endsWith("e"))""".c,"""true if all elements in lst end with the letter "e" else false"""),
  row("lst.foreach(str => print(str))".c,"Executes the print function for each of the strings in the lst"), 
  row("lst.foreach(print)".c,"Same as the previous, but more concise"),  
  row("lst.head".c,"Returns the first item in lst"), 
  row("lst.tail".c,"Returns a list that is lst without its first item"),
  row("lst.init".c,"Returns a list of all but the last element in lst"), 
  row("lst.isEmpty".c,"true if lst is empty"), 
  row("lst.last".c,"Returns the last item in lst"), 
  row("lst.length".c,"Returns the number of items in the lst"), 
  row("""lst.map(str => str + "?")""".c,"""Returns a list created by adding "?" to each string item in lst"""),
  row("""lst.mkString(", ")""".c,"Makes a string with the elements of the list"),  
  row("lst.remove(str => str.length == 4)".c,"Returns a list of all items in lst, in order, excepting any of length 4"),
  row("List(1,6,2,1,6,3).removeDuplicates".c,"Removes redundant elements from the list. Uses the method == to decide. "),  
  row("lst.reverse".c,"Returns a list containing all elements of the lst list in reverse order"), 
  row("lst.sort((str, t) => str.toLowerCase < t.toLowerCase)".c,"Returns a list containing all items of lst in alphabetical order in lowercase.")
),
"Some more useful list operations. First define a list of integers to use.".p,
"val lsti=List(1,7,2,8,5,6,3,9,14,12,4,10)".c,
table(
  row("lsti.foldLeft(0)(_+_)".c,"Combines elements of list using a binary function starting from left, initial one with a 0 in this case."),
  row("lsti.foldRight(0x20)(_|_)".c,"Combines elements of list using a binary function starting from Right, initial one with a hex 20 in this case.")
)
)
)
pages += Page(
    name = "UT",
    body = tPage("Using the Turtle",
	"The Turtle can be moved with a set of commands, many of them are listed below. Just try them out to see what they cause the Turtle to do.".p,
	"The following example defines a function that draws a triangle and will be used in other examples further on so try it first. Notice multiple commands can be created on one line if they are seperated by semi-colon.".p,
  """
  def triangle()={
    forward(100);right(120)
    forward(100);right(120)
    forward(100);right(120)
  }
  clear()
  triangle()""".c,

	table(
  row("forward(100)".c, "Moves the turtle forward a 100 steps."),
  row("back(50)".c,"Moves the turtle back 50 steps."),
  row("setPosition(100, 100)".c, "Sends the turtle to the point (x, y) without drawing a line. The turtle's heading is not changed."),
  row("moveTo(20, 30)".c, "Turns the turtle towards (x, y) and moves the turtle to that point."), 
  row("turn(30)".c, "Turns the turtle through a specified angle. Angles are positive for counter-clockwise turns."),
  row("right()".c, "Turns the turtle 90 degrees right (clockwise)."),
  row("right(60)".c, "Turns the turtle 60 degrees right (clockwise)."),
  row("left()".c, "Turns the turtle 90 degrees left (counter-clockwise)."),
  row("left(-30)".c, "Turns the turtle angle degrees left (counter-clockwise)."), 
  row("towards(40, 60)".c, "Turns the turtle towards the point (x, y)."),
  row("setHeading(30)".c, "Sets the turtle's heading to angle (0 is towards the right side of the screen ('east'), 90 is up ('north'))."),
  row("heading".c, "Queries the turtle's heading (0 is towards the right side of the screen ('east'), 90 is up ('north"),
  row("home()".c, "Moves the turtle to its original location, and makes it point north."),
  row("position".c, "Queries the turtle's position."),

  row("""penUp()
  forward(100)
  penDown()
  forward(100)""".c, "penDown makes the turtle draw lines as it moves while with penUp the Turtle moves without drawing a line."), 
  row("""setPenColor(blue)
  triangle()""".c, "Specifies the color of the pen that the turtle draws with."),
  row("""clear()
  setFillColor(red)
  triangle()
  """.c, "Specifies the fill color of the figures drawn by the turtle."),

  row("""
  clear()
  setPenThickness(10)
  triangle()
  setPenThickness(1)
  """.c, "Specifies the width of the pen that the turtle draws with."),
  row("beamsOn()".c, "Shows crossbeams centered on the turtleto help with solving puzzles."),
  row("beamsOff()".c, "Hides the turtle crossbeams."),
  row("""clear()
  invisible()
  forward(100)
  visible()
  turn(120)
  forward(100)""".c, "invisible hides the turtle while visible makes it visible again."),
  row("""write("hello world")""".c, "Makes the turtle write the specified object as a string at its current location."),
  row("""
  clear()
  forward(-100)
  setAnimationDelay(10)
  turn(120)
  forward(100)""".c, "Sets the turtle's speed. The specified delay is the amount of time (in milliseconds) taken by the turtle to move through a distance of one hundred steps. The default is 1000."),
  row("animationDelay".c, "Queries the turtle's delay setting."),

  row("undo()".c, "Undoes the last turtle command."),
  row("newTurtle(50, 50)".c, "Makes a new turtle located at the point (x, y)."),
  row("turtle0".c, "Gives you a handle to the default turtle."),
  row("clear()".c, "Clears the screen. To bring the turtle to the center of the window after this command, just resize the turtle canvas."),
  row("""
  clear()
  triangle()
  zoom(0.5, 10, 10)
  """.c, "Zooms in by the given factor, and positions (cx, cy) at the center of the turtle canvas."),

  row("gridOn()".c, "Shows a grid on the canvas."),
  row("gridOff()".c, "Hides the grid."),
  row("axesOn()".c, "Shows the X and Y axes on the canvas."),
  row("axesOff()".c, "Hides the X and Y axes.")
  )
)
)
	
pages += Page(
    name = "GAG",
    body = tPage("Graphics and Games",
"Peter Lewerin has contributed 'Staging' to Kojo. Staging gives you some neat graphics and the potential to make games. These Staging features originated in a Java project called Processing and were ported to Kojo by Peter. This capability is worth a whole tutorial in itself. However to give you a taste of what is possible and a starting point for experimentation, here are a couple examples.".p,
"The section on Staging will be expanded in a later version of the Tutorial. You can find a more complete list of Staging features and examples at 'lewerin.se/peter/kojo/staging.html'".p,
"The package contains functions that allow you to draw sophisticated graphics images and a frame loop that allows you to animate the graphics.".p,
"In the first example you can see that a Staging environment is created, reset and the screen cleared. A ball is created then the ball bouncing movement defined in the 'loop'. Staging causes this loop to be executed every 32 milli-seconds giving a frame rate of about 30 frames per second. Using these principles you can create sophisticated animated graphics. Next you will find how this can be turned into a simple game.".p,
  
"""val S = Staging  
S.reset()
clear
gridOn()
val ball=S.circle(-200, -100, 5)

var y = 0 ; var x = 0 // ball position
var dy = 10; var dx = 3 // ball speed
// animation is about 30 frames per second or 32 milliseconds per frame
S.loop{
    ball.setPosition(x,y)
	// update ball position, detect end of bounce area
    dx =  if(x < 0 || x > 200) -dx else dx
    x += dx
    dy =  if(y < 0 || y > 100) -dy else dy
    y += dy  
}
""".c,
"Click on the red circle button to stop the animation. You will get unexpected behavior or may even crash the Staging environment if you do not stop a previous animation before starting a new one.".b, "The game is a single player version of what must be one of the oldest games ever played on computers called 'Pong'. The idea is to hit the ball back with a paddle which you can move with the mouse. Each of your misses will be recorded. Have fun!".p,
"Important note:".b,
"If you use 'import Staging._' in an example or your own code then you must do a 'reimportBuiltins' when you finish running it to be able to use Turtle commands again.".p,
"reimportBuiltins".c,
"The Staging environment overrides some of the Turtle commands.".p,   
"""
import Staging._
val S = Staging  
S.reset()
gridOff()
clear
var x = 0 ; var y = 0  // ball position
var dy = 10 ; var dx = 3 // ball speed
var padx=0.0 ; var pady=0.0 // paddle position
val padl=80 // paddle length
var miss=0
// Court
line(-200,-100,-200,100)
line(-200,-100,200,-100)
line(-200,100,200,100)
// the ball
S.setFillColor(blue)
val ball=S.circle(-200, -100, 5) 
// animation is about 30 frames per second or 32 milliseconds per frame
S.loop{
    S.wipe
    padx=S.mouseX;pady=S.mouseY
    S.line(padx, pady, padx, pady+padl) // the paddle
    // detect a hit
    dx =if((dx>0)&&(padx-x<15)&&(x-padx<15)&&(y>pady)&&(y<pady+padl)) -dx else dx
    ball.setPosition(x,y)
    // update ball position and check for walls
    dx =  if(x+dx < -200) -dx else dx
    if(x+dx>200){x= -200;miss+=1}  // a miss
    x += dx
    dy =  if((y+dy < -100 )|| (y + dy > 100)) -dy else dy
    y += dy
    // Keep Score
    S.text(miss.toString + " missed",0,0)
	}
""".c,
"Now you have the basics try adding more balls, randomising their speed or changing the paddle size. Also see if you can fix the bug. Sometimes the ball appears to pass through the paddle.".p,
"Keyboard Input".h3,
"It is very useful in games to use the keyboard to get player commands. Here is a simple example that allows you to draw using the left/right/up/down arrows to steer the Turtle.".p,

"Now run the demo by click on the following which associates a set of actions to keyboard events. Don't forget to click on the Turtle canvas to give it focus first.".p,
"""
clear;visible
onKeyPress{(k)=>k match {
            case Kc.VK_LEFT => setHeading(180)
            case Kc.VK_RIGHT => setHeading(0)
            case Kc.VK_UP => setHeading(90)
            case Kc.VK_DOWN => setHeading(270)
			case _ => 15
			}
            forward(20)            
		}
""".c,
"You can modify the actions and re-run to see what happens. Type Kc. to find out what other key events can be recognised.".p     	
)
)

	
pages += Page(
    name = "LM",
    body = tPage("Learning More",

"Next Steps".h2,
"Here just a tiny part of the Scala libraries have been explored yet, by now, you are familiar with some essential Scala language features. Already you could write quite sophisticated programs in Scala. If you are already a Java programmer you can no doubt already see how you can use Scala with all the libraries from your existing Java environment too. Scala and Java integrate in a seamless way.".p,

"""A good book is invaluable. Programming in Scala by Martin Odersky, Lex Spoon and Bill Venners is one excellent place to continue  "www.artima.com/shop/programming_in_scala" The Scala community site has a lot of good material and references to other learning materials "www.scala-lang.org". For the professional programmer you can also download Scala and an the Eclipse IDE. Have fun!""".p


)
)

val story = Story(pages: _*)
stClear()
stPlayStory(story)
clearOutput

stAddLinkHandler("example", story) {idx: Int =>
	stSetScript(codeExamples(idx))
    stClickRunButton()
	clearOutput
}
