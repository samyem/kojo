// Click the Run button in the toolbar above to start the story
// 
// =================================================================
// 
// The source for the story is provided below

val pageStyle = "color:#1e1e1e; margin:15px;"
val centerStyle = "text-align:center;"
val headerStyle = "text-align:center;font-size:110%;color:maroon;"
val codeStyle = "font-size:90%;"
val smallNoteStyle = "color:gray;font-size:95%;"
val sublistStyle = "margin-left:60px;"

def pgHeader(hdr: String) = 
    <p style={headerStyle}>
        {new xml.Unparsed(hdr)}
        <hr/>
    </p>
    

var pages = new collection.mutable.ListBuffer[StoryPage]
var pg: StoryPage = _
var header: xml.Node = _

def link(page: String) = "http://localpage/%s" format(page)
val homeLink = <div style={smallNoteStyle+centerStyle}><a href={link("home#7")}>Start Page</a></div>

pg = Page(
    body = 
        <body style={pageStyle+centerStyle}>
            { for (i <- 1 to 5) yield {
                    <br/>
                }
            }
            <h1>An Overview of Kojo</h1>
            <em>For teachers and parents</em>
            { for (i <- 1 to 7) yield {
                    <br/>
                }
            }
            <p style={smallNoteStyle}>
                Please resize this window to about half your screen width, by dragging 
                its right border. Also make sure that the Turtle Canvas is nice and visible.
            </p>
        </body>
)

pages += pg

header = pgHeader("What is Kojo?")

pg = IncrPage(
    name = "home",
    style=pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                So what exactly is Kojo?
            </p>
        ),
        Para(
            <p>
                Kojo is a <em>Learning Environment</em>. It has many different features
                - that help with the learning and teaching of concepts in the areas of:
            </p>
        ),
        Para(
            <ul>
                <li><a href={link("comp-prog")}>Computer Programming</a> and <a href={link("crit-think")}>Critical Thinking</a>.</li>
            </ul>
        ),
        Para(
            <ul>
                <li><a href={link("math")}>Math</a> and <a href={link("science")}>Science</a>.</li>
            </ul>
        ),
        Para(
            <ul>
                <li><a href={link("creative")}>Art and Creative Thinking</a>.</li>
            </ul>
        ),
        Para(
            <ul>
                <li><a href={link("literacy")}>Computer and Internet Literacy</a>.</li>
            </ul>
        )
    )
)

pages += pg

pg = IncrPage(
    style=pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                You can think of Kojo as:
            </p>
        ),
        Para(
            <ul>
                <li>A gym, where children can exercise their brains.</li>
            </ul>
        ),
        Para(
            <ul>
                <li>A studio, where they can create computer sketches, and 
                    stories/presentations (like this one!).</li>
            </ul>
        ),
        Para(
            <ul>
                <li>A Lab, where they can experiment with mathematical and scientific ideas.</li>
            </ul>
        )
    )
)

pages += pg


header = pgHeader("Kojo and Computer Programming")

pg = IncrPage(
    name = "comp-prog",
    style=pageStyle,
    body = List(
        Para(
            <span>
                {header}
                {homeLink}
            </span>
        ),
        Para(
            <p>
                Computer programming is a very useful skill for children to learn,
                and a very beneficial activity for them to carry out.
            </p>
        ),
        Para(
            <p>
                It teaches them how to think systematically.
            </p>
        ),
        Para(
            <p>
                And helps them to develop the kinds of thinking skills needed by Math and Science.
            </p>
        ),
        Para(
            <p>
                Computer programming is the basis of most things that children do 
                within Kojo, so let us spend a little time looking at how it is 
                supported within Kojo.
            </p>
        )
    )
)

pages += pg

val squareSample = """
    clear()
    repeat (4) {
        forward(100)
        right()
    }
"""



pg = IncrPage(
    style=pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                Children get started with computer programming within Kojo by writing
                simple programs that direct a turtle to make shapes on the screen.
            </p>
        ),
        Para(
            <p>
                Here's an example of such a program:
                <pre style={codeStyle}>
                    {xml.Unparsed(squareSample)}
                </pre>
            </p>
        ),
        Para(
            <p>
                Click the <em>Make Square</em> button below to run the program.
                <p style={smallNoteStyle}>
                    You will see the output of the program within the Turtle Canvas.
                </p>
            </p>,
            code = {
                axesOff()
                gridOff()
                stAddButton ("Make Square") {
                    clear()
                    axesOff()
                    gridOff()
                    repeat (4) {
                        forward(100)
                        right()
                    }
                }
            }
        )
    )
)

pages += pg


pg = IncrPage(
    style=pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                How about playing a little with the program that you just ran?
                Copy the program to the <em>Script Editor</em>
                - by clicking the <em>Copy Program</em> button below.
            </p>,
            code = {
                stAddButton ("Copy Program") {
                    stSetScript(squareSample)
                }
            }
        ),
        Para(
            <p>
                Did the copy succeed?
                <br/>
                Make sure that you see the square making program within the
                <em>Script Editor</em> before proceeding.
            </p>
        ),
        Para(
            <p>
                Now run the program that you just copied - by clicking the <em>Run</em>
                button in the <em>Script Editor</em> toolbar.
            </p>
        )
    )
)

pages += pg

pg = IncrPage(
    name = "",
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                Done?
                <br/>
                <br/>
                What is the length of the square that you just made?
                <br/>
                <br/>
                Input your answer below and click the <em>Check Answer</em> button.
                <div style={smallNoteStyle}>Watch for a response in the status bar right at the bottom</div>
            </p>,
            code = {
                stAddField("Length", "")
                stAddButton ("Check Answer") {
                    val num = stFieldValue("Length", 0)
                    if (num == 100) {
                        stShowStatusMsg("You got that right!")
                    }
                    else {
                        stShowStatusError("Sorry - you got that wrong")
                    }
                }
            }
        ),
        Para(
            <p>
                Can you make a square that is 150 units in length - by slightly
                modifying the program within the <em>Script Editor</em> and then
                running it?
            </p>
        ),
        Para(
            <p>
                Are you starting to see what simple programming in Kojo is about?
            </p>
        ),
        Para(
            <p>
                And the kind of interactivity that is available within Kojo stories
                (like this one!) - which children get to both read and author, 
                as they work with Kojo.
            </p>
        )
    )
)

pages += pg


val treeSample = """
def tree(distance: Double) {
    if (distance > 4) {
        setPenThickness(distance/7)
        setPenColor(color(distance.toInt, 
           Math.abs(255-distance*3).toInt, 125))
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
setAnimationDelay(10)
penUp()
back(200)
penDown()
tree(90)
"""

pg = IncrPage(
    style=pageStyle,
    body = List (
        Para(
            {header}
        ),
        Para(
            <p>
                Let's look at a program that makes a much richer figure.
            </p>
        ),
        Para(
            <span>
                <pre style={codeStyle}>
                    {xml.Unparsed(treeSample)}
                </pre>
                Click on the <em>Make Tree</em> button below to run
                this program.
            </span>,
            code = {
                def tree(distance: Double) {
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
            
                stAddButton ("Make Tree") {
                    clear()
                    axesOff()
                    gridOff()
                    invisible()
                    setAnimationDelay(10)
                    penUp()
                    back(200)
                    penDown()
                    tree(90)
                }
            }
        ),
        Para(
            <p>
                That was meant to show you the richness of figures that can be 
                drawn, with the help of small computer programs, within Kojo.
            </p>
        )
    )
)

pages += pg

pg = IncrPage(
    style=pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                To program within Kojo, children use:
            </p>
        ),
        Para(
            <ul>
                <li>Scala, arguably the 21st century successor to Java.</li>
                <div style={smallNoteStyle+"margin-left:40px;"}>
                    <a href="http://macstrac.blogspot.com/2009/04/scala-as-long-term-replacement-for.html">This page on the web</a> 
                    has a detailed discussion of the merits of Scala, including links to endorsements for Scala by creators of other
                    languages like Java, JRuby, and Groovy.
                </div>
            </ul>
        ),
        Para(
            <ul>
                <li>HTML, the core language of the World-wide-web.</li>
            </ul>
        ),
        Para(
            <p>
                This gives children good exposure to modern, widely used, software
                technology.
            </p>
        ),
        Para(
            <p style={smallNoteStyle}>
                This story itself has been written (right within Kojo) using 
                Scala and HTML!
            </p>
        )
    )
)

pages += pg


header = pgHeader("Kojo and Critical Thinking")

pg = IncrPage(
    name = "crit-think",
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                Kojo gives children a lot of practice in Critical and Logical thinking.
            </p>
        ),
        Para(
            <p>
                Writing any computer program involves Critical and Logical thinking.
                So, children are exposed to these modes of thought with anything that 
                they do within Kojo.
            </p>
        ),
        Para(
            <p>
                They also get to specifically do Critical/Logical thinking exercises.
            </p>
        ),
        Para(
            <p>
                For example, take a look at the two figures that are being drawn 
                within the Turtle Canvas.
                Children are shown a computer program that draws the figure on the left,
                and then asked to slightly modify this program to draw the figure on the right.
            </p>,
            code = {
                clear()
                axesOff()
                gridOff()
                setAnimationDelay(100)
                setPosition(-200, 0)

                setPenColor(green)
                setPenThickness(4)
                setFillColor(yellow)
                repeat (4) {
                    forward(200)
                    right()
                }
                setPenColor(blue)
                setPenThickness(2)
                setFillColor(red)
                repeat (4) {
                    repeat (4) {
                        forward(50)
                        right()
                    }
                    penUp()
                    forward(50)
                    right()
                    forward(50)
                    left()
                    penDown()
                }
            
            
                setPosition(100, 0)
                setPenColor(green)
                setPenThickness(4)
                setFillColor(purple)
                repeat (4) {
                    forward(200)
                    right()
                }
                setPenColor(blue)
                setPenThickness(2)
                setFillColor(orange)
                repeat (3) {
                    repeat (4) {
                        forward(100)
                        right()
                    }
                    penUp
                    forward(50)
                    right()
                    forward(50)
                    left()
                    penDown
                }            
            }
        ),
        Para(
            <p>
                By doing this, they learn important ideas related to computer 
                programming - by studying an existing program.<br/>
                Then they get to test their knowledge and apply some clear and 
                precise thinking to solve the problem given to them.
            </p>
        )
    )
)

pages += pg

header = pgHeader("Kojo and Math")

pg = IncrPage(
    name = "math",
    style = pageStyle,
    body = List(
        Para(
            <span>
                {header}
                {homeLink}
            </span>
        ),
        Para(
            <p>
                Children can use Kojo to play with interesting ideas in Math.
            </p>
        ),
        Para(
            <p>
                Let's look at a couple of examples:
                <ul>
                    <li>Linear equations in two variables</li>
                    <li>The angle sum property of triangles</li>
                </ul>
            </p>
        )
    )
)

pages += pg


pg = IncrPage(
    name = "",
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                A linear equation in two variables has the form:
                {stFormula("""\text{y = mx + c}""")}
            </p>
        ),
        Para(
            <p>
                As you might well know, any equation in two variables can be
                visualized using (2-D) coordinate geometry.
            </p>
        ),
        Para(
            <p>
                Let's visualize this particular equation to see the role of 
                <em>m</em> and <em>c</em>. 
            </p>
        ),
        Para(
            <p>
                Put in different values for <em>m</em> and <em>c</em> below, and 
                then Click the <em>Plot</em> button to see what the equation looks 
                like for these different values (of <em>m</em> and <em>c</em>).
            </p>,
            code = {
                def init() {
                    clear()
                    axesOn()
                    gridOn()
                    setAnimationDelay(10)
                    zoom(0.7, 0, 0)
                }
                init()
                stAddField("m", 1)
                stAddField("c", 70)
                stAddButton ("Plot") {
                    val m = stFieldValue("m", 1.0)
                    val c = stFieldValue("c", 70.0)
                    val dom = 200

                    def y(m: Double, x: Double, c: Double) = m*x + c

                    setPenColor(new Color(random(255), random(255), random(255)))
                    setPosition(-dom, y(m, -dom, c))
                    write("m=%.1f, c=%.1f" format(m, c))
                    for(x <- -dom+10 to dom; if (x % 10 == 0)) {
                        moveTo(x, y(m, x, c))
                    }                
                }
                stAddButton ("Clear") {
                    init()
                }
            }
        )
    )
)

pages += pg

pg = IncrPage(
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                It's important to note that Children, in their work with Kojo, 
                <em>get to actually write computer programs to <strong>make</strong> 
                    interactive screens </em> like the one that you just saw, instead 
                of just viewing (or interacting with) them.
            </p>
        ),
        Para(
            <p>
                This kind of activity is perfect for school projects that supplement
                the prescribed syllabus.
            </p>
        ),
        Para(
            <p style={smallNoteStyle}>
                Viewing pre-made content and interacting with it is, of course, an 
                important learning activity that is available to children within Kojo.<br/>
                But children can go much further with Kojo - by authoring their own 
                interactive, animated content.
            </p>
        )
    )
)

pages += pg

pg = IncrPage(
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                Kojo has a virtual laboratory called <em>Math World</em> - where children
                can <em>experiment</em> with Math.
            </p>
        ),
        Para(
            <p>
                Let's see what an experiment with linear equations might look like 
                within <em>Math World</em>.
            </p>
        ),
        Para(
            <p>
                Click on the <em>Linear Equation Experiment</em> button below to bring up <em>Math World</em>.
                Within <em>Math World</em>, you should see a linear equation represented as a straight line.
                You should also see two sliders representing <em>m</em> and <em>c</em>. 
                Play with these sliders to visualize, in real time, how the values of <em>m</em>
                and <em>c</em> affect the equation.
            </p>,
            code = {
                stAddButton ("Linear Equation Experiment") {
                    Mw.clear()
                    Mw.showAxes()
                    Mw.variable("m", 1, -5, 5, 0.1, 50, 50)
                    Mw.variable("c", 0, -2, 2, 0.1, 50, 80)
                    Mw.evaluate("y = m x + c")
                }
            }
        )
    )
)

pages += pg

pg = IncrPage(
    name = "",
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                Let's now play with some Geometry.
            </p>
        ),
        Para(
            <p>
                You might have heard about the <em>angle sum property</em> of triangles
                - the fact that the sum of the angles of a triangle is 180°.
            </p>
        ),
        Para(
            <p>
                Let's dig deeper into that.
            </p>
        ),
        Para(
            <p>
                But first, let's look at an important property of two parallel lines
                that are cut by a transversal. Click on the <em>Show Lines</em>
                button below to bring up a figure - which will help us in 
                understanding this property.
            </p>,
            code = {
                stAddButton ("Show Lines") {
                    Mw.clear()
                    Mw.hideAxes()
                    // Make first line
                    val P1 = Mw.point("P1", -10,3)
                    val P2 = Mw.point("P2", 20,3)
                    val L1 = Mw.line("L1", P1,P2)

                    // Make second line, parallel to first 
                    val P3 = Mw.point("P3", -10,1)
                    val P4 = Mw.point("P4", 20,1)
                    val L2 = Mw.line("L2", P3,P4)

                    // Make transversal
                    val P5 = Mw.point("P5", 1,0)
                    val P6 = Mw.point("P6", 4,4)
                    val L3 = Mw.line("L3", P5,P6)

                    // Find intersection points of transversal with lines
                    val P7 = Mw.intersect("P7", L1, L3)
                    val P8 = Mw.intersect("P8", L2, L3)

                    // Show Angles that transversal makes with lines
                    val color1 = color(0, 102, 0)

                    val A1 = Mw.angle("A1", P1,P7,P5)
                    A1.setColor(color1)

                    val A3 = Mw.angle("A3", P4,P8,P6)
                    A3.setColor(color1)

                    // Hide intersection points so angles are clearly visible
                    P7.hide()
                    P8.hide()
                }
            }
        ),
        Para(
            <p>
                The property of interest is - <em>when two parallel lines are cut
                    by a transversal, the alternate angles are equal</em>. 
                A pair of alternate angles are marked on the figure. Play with 
                the two marked points on the transversal (by dragging them around) to 
                get a feel for the truth of this property.
            </p>
        )    
    )
)

pages += pg

pg = IncrPage(
    name = "",
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                Now, let's look at the angle sum property of triangles. Click on
                the <em>Show Triangle</em> button below to bring up a figure that
                helps us to see the truth of this property.
            </p>,
            code = {
                stAddButton ("Show Triangle") {
                    Mw.clear()
                    Mw.hideAxes()
                    // Make first line
                    val P1 = Mw.point("P1", -10,3)
                    val P2 = Mw.point("P2", 20,3)
                    val L1 = Mw.line("L1", P1,P2)

                    // Make second line, parallel to first 
                    val P3 = Mw.point("P3", -10,1)
                    val P4 = Mw.point("P4", 20,1)
                    val L2 = Mw.line("L2", P3,P4)

                    // Make Triangle

                    // Make first vertex on the first line
                    val A = Mw.point("A", L1, 2, 3)
                    A.hideLabel()
                    // Make other two vertices on the second line
                    val B = Mw.point("B", L2, 1, 1)
                    B.hideLabel()
                    val C = Mw.point("C", L2, 4, 1)
                    C.hideLabel()

                    val c = Mw.lineSegment("c",A,B)
                    c.hideLabel()
                    val a = Mw.lineSegment("a",B,C)
                    a.hideLabel()
                    val b = Mw.lineSegment("b",C,A)
                    b.hideLabel()

                    val color1 = color(0, 0, 102)
                    val color2 = color(153, 0, 0)

                    val X = Mw.angle("X", B,A,C)
                    X.showNameInLabel()
                    val Y = Mw.angle("Y", C,B,A)
                    Y.setColor(color1)
                    Y.showNameInLabel()
                    val Z = Mw.angle("Z", A,C,B)
                    Z.setColor(color2)
                    Z.showNameInLabel()

                    val Yp = Mw.angle("Y'", P1,A,B)
                    Yp.setColor(color1)
                    Yp.showNameInLabel()
                    val Zp = Mw.angle("Z'", C,A,P2)
                    Zp.setColor(color2)
                    Zp.showNameInLabel()
                }
            }
        ),
        Para(
            <p>
                Play with the vertices of the triangle.
            </p>
        ),
        Para(
            <p>
                Can you spot the alternate angles in the figure?
            </p>
        ),
        Para(
            <p>
                Do you see why the the sum of the angles of a triangle is 180°?
                <div style={smallNoteStyle+"margin-left:30px;margin-right:30px"}>
                    You should know that the sum of the angles that lie on a 
                    straight line is 180°<br/>
                </div>
            </p>
        ),
        Para(
            <p style={smallNoteStyle}>
                Children can write computer programs within Kojo to play with,
                and demonstrate their understanding of, geometric theorems and 
                proofs.
            </p>
        )
    )
)

pages += pg

pg = IncrPage(
    name = "",
    style = pageStyle,
    body = List(
        Para(
            {header}
        ),
        Para(
            <p>
                Kojo has great support for mathematical notation.
            </p>
        ),
        Para(
            <p>
                Here are some examples:
                {stFormula("""ax^2 + bx + c""")}
                {stFormula("""a_1 = a_2^2""")}
                {stFormula("""x = \frac{y-c}{m}""")}
                {stFormula("""\sqrt[n]{x}""")}
                {stFormula("""\displaystyle\sum_{i=1}^{\infty}\frac{1}{i}""")}
                {stFormula("""\binom{n-1}{r-1}""")}
                {stFormula("""\displaystyle\lim_{x\to\infty}\frac{1}{x}""")}
                {stFormula("""\frac{d}{dx}(x^2) = 2x""")}
                {stFormula("""\int 2x\,dx = x^2+C""")}
            </p>
        )
    )
)

pages += pg

header = pgHeader("Kojo and Science")

pg = IncrPage(
    name = "science",
    style = pageStyle,
    body = List(
        Para(
            <span>
                {header}
                {homeLink}
            </span>
        ),
        Para(
            <p>
                Let's play with Newton's <em>second law of motion</em>.
            </p>
        ),
        Para(
            <p>
                Newton's second law relates the acceleration experienced by a body
                to its mass, and the force applied to it, according to the following
                equation:
                {stFormula("""\text{F = m a}""")}
            </p>
        ),
        Para(
            <p>
                The figure on the right shows two bodies. You can specify their
                mass, and the force applied to them, via fields at the bottom 
                of this page. You can then animate the bodies by Clicking on the 
                <em>Apply Newton's Law</em> button - to see how the bodies 
                behave - as per the second law.
                <div style={smallNoteStyle + "margin-left:30px;"}> 
                    Note - Body1 is green. Body2 is blue.
                </div>
            </p>,
            code = {
                val S = Staging
                axesOff()
                gridOff()
                
                def setup() = {
                    S.reset()
                    S.line(-225, 100, -225, -200)
                    val body1 = S.circle(-200, -100, 25)
                    body1.fill = green

                    val body2 = S.circle(-200, 0, 25)
                    body2.fill = blue
                    (body1, body2)
                }
                
                def currTime = System.currentTimeMillis
                def moveBodyTo(x: Double, shape: S.Shape) {
                    val pos = shape.offset
                    shape.translate(S.point(x - pos.x, 0))
                }

                setup()

                stAddField("m1", 5)
                stAddField("m2", 10)
                stAddField("F1", 100)
                stAddField("F2", 100)
                stAddButton ("Apply Newton's Law") {
                    val force1 = stFieldValue("F1", 100)
                    val force2 = stFieldValue("F2", 100)

                    val mass1 = stFieldValue("m1", 5)
                    val mass2 = stFieldValue("m2", 10)

                    val acc1 = force1/mass1
                    val acc2 = force2/mass2

                    val (body1, body2) = setup()
                    val t0 = currTime

                    S.loop {
                        // s = 1/2 * a * t^2
                        val t = (currTime - t0)/1000.0
                        val s1 = 0.5 * acc1 * t * t 
                        val s2 = 0.5 * acc2 * t * t 
    
                        moveBodyTo(s1, body1)
                        moveBodyTo(s2, body2)
                    }
                }
            }
        ),
        Para(
            <p style={smallNoteStyle}>
                Children can write computer programs to create interactive animations 
                like this one within Kojo - to gain a deeper understanding of 
                scientific concepts.
            </p>
        )
    )
)

pages += pg

pg = IncrPage(
    name = "creative",
    style = pageStyle,
    body = List(
        Para(
            <span>
                {pgHeader("Kojo and Creative Play")}
                {homeLink}
            </span>
        ),
        Para(
            <p>
                Kojo provides children a rich playground for creative play. Some
                of the things that children can do within Kojo include:
            </p>
        ),
        Para(
            <ul>
                <li>Making pretty computer sketches and paintings.
                    <div style={smallNoteStyle}>
                        See the <em>Samples | Turtle Art</em> menu item for many examples.
                    </div>
                </li>
            </ul>
        ),
        Para(
            <ul>
                <li>Making stories about interesting mathematical and scientific 
                    facts. This involves:</li>
            </ul>
        ),
        Para(
            <ul style={sublistStyle}>
                <li>Doing research and indentifying a concept of interest.</li>
            </ul>
        ),
        Para(
            <ul style={sublistStyle}>
                <li>Digging deeper and better understanding the concept.</li>
            </ul>
        ),
        Para(
            <ul style={sublistStyle}>
                <li>Writing programs to demonstrate different aspects of the concept.</li>
            </ul>
        ),
        Para(
            <ul style={sublistStyle}>
                <li>Tying the programs together within a story, along with 
                    text, images, sound, and music.</li>
            </ul>
        ),
        Para(
            <p>
                Imagine the creative possibilities this opens up!
            </p>
        )
    )
)

pages += pg

pg = IncrPage(
    name = "literacy",
    style = pageStyle,
    body = List(
        Para(
            <span>
                {pgHeader("Kojo and Computer Literacy")}
                {homeLink}
            </span>
        ),
        Para(
            <p>
                As children work with Kojo, they learn many different aspects of 
                Computers:
            </p>
        ),
        Para(
            <ul>
                <li>Basic computer proficiency: doing substantial projects and 
                    organising their work within files and folders.</li>
            </ul>
        ),
        Para(
            <ul>
                <li>Programming computers.</li>
            </ul>
        ),
        Para(
            <ul>
                <li>Working with multimedia: text, images, sounds, and music.</li>
            </ul>
        ),
        Para(
            <ul>
                <li>Using the internet for research, and participating on a social
                    network (the <a href="http://www.kogics.net/codeexchange">Kojo Code Exchange</a>) for meaningful discussion.</li>
            </ul>
        ),
        Para(
            <ul>
                <li>Using the computer for creative play and learning, by bringing together 
                    the different features offered by Kojo.</li>
            </ul>
        )
    )
)

pages += pg

pg = IncrPage(
    name = "",
    style = pageStyle,
    body = List(
        Para(
            <span>
                {pgHeader("Conclusion")}
                {homeLink}
            </span>
        ),
        Para(
            <p>
                Hopefully, this story has given you an idea of what children
                can do with Kojo.
            </p>
        ),
        Para(
            <p>
                Actually, we have barely scratched the surface of what is 
                possible with Kojo.
            </p>
        ),
        Para(
            <p>
                You, and the children around you,
                are in for some interesting times as you play with Kojo.
            </p>
        ),
        Para(
            <p>
                Welcome to the world of Kojo!
            </p>
        )
    )
)

pages += pg

val story = Story(pages: _*)
stClear()
playMp3InBg("music-loops/Medieval1.mp3")
stPlayStory(story)