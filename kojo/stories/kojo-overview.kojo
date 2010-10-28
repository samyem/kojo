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

def pgHeader(hdr: String) = 
    <p style={headerStyle}>
        {new xml.Unparsed(hdr)}
        <hr/>
    </p>
    

var pages = new collection.mutable.ListBuffer[StoryPage]
var pg: StoryPage = _
var header: xml.Node = _

pg = Page(
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
    style=pageStyle,
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
            <li>Computer Programming and Critical Thinking.</li>
        </ul>
    ),
    Para(
        <ul>
            <li>Art and Creative Thinking.</li>
        </ul>
    ),
    Para(
        <ul>
            <li>Math and Science.</li>
        </ul>
    ),
    Para(
        <ul>
            <li>Computer and Internet Literacy.</li>
        </ul>
    )
)

pages += pg

pg = IncrPage(
    style=pageStyle,
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

pages += pg

header = pgHeader("Kojo and Computer Programming")

pg = IncrPage(
    style=pageStyle,
    Para(
        {header}
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
                repeat (4) {
                    forward(100)
                    right()
                }
            }
        }
    )
)

pages += pg

pg = IncrPage(
    style=pageStyle,
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
    ))

pages += pg

pg = IncrPage(
    style=pageStyle,
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
            (which children get to both read and author, as they work with Kojo).
        </p>
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

pages += pg

pg = IncrPage(
    style=pageStyle,
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
            <div style={smallNoteStyle}>
                This <a href="http://macstrac.blogspot.com/2009/04/scala-as-long-term-replacement-for.html">page on the web</a> 
                has a detailed discussion of the subject, including links to endorsements for Scala by creators of other
                languages like Java, JRuby, and Groovy.
            </div>
        </ul>
    ),
    Para(
        <ul>
            <li>Html, the core language of the World-wide-web.</li>
        </ul>
    ),
    Para(
        <p>
            This gives children good exposure to modern, widely used, software
            technology.
        </p>
    )
)

pages += pg

header = pgHeader("Kojo and Critical Thinking")

pg = IncrPage(
    style=pageStyle,
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
            For example, take a look at the two figures within the Turtle Canvas.
            Children are shown a computer program that draws the figure on the left,
            and then asked to slightly modify this program to draw the figure on the right.
        </p>,
        code = {
            clear()
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
            programming - by studying an existing program. They then get 
            to test their knowledge and apply some clear and precise thinking
            to solve the problem given to them.
        </p>
    )

)

pages += pg

header = pgHeader("Kojo and Math")

pg = IncrPage(
    style=pageStyle,
    Para(
        {header}
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

pages += pg

pg = IncrPage(
    style=pageStyle,
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

pages += pg

pg = IncrPage(
    style=pageStyle,
    Para(
        {header}
    ),
    Para(
        <p>
            It's important to note that Children, in their work with Kojo, 
            <em>get to actually <strong>make</strong> interactive screens </em>
            like the one that you just saw, instead of just viewing (or interacting
            with) them.
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
            But children can go much further within Kojo - by authoring their own 
            interactive, animated content.
        </p>
    )
)

pages += pg

pg = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerMath}
            <br/>
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
            You will see a linear equation represented as a straight line within <em>Math World</em>.
            You will also see two sliders representing <em>m</em> and <em>c</em>. 
            Play with the sliders to see, in real time, how the values of <em>m</em>
            and <em>c</em> affect the equation.
        </p>,
        code = {
            stAddButton ("Linear Equation Experiment") {
                Mw.clear()
                Mw.variable("m", 1, -5, 5, 0.1, 50, 50)
                Mw.variable("c", 0, -2, 2, 0.1, 50, 80)
                Mw.evaluate("y = m x + c")
            }
        }
    )
)

pages += pg

val story = Story(pages: _*)
stClear()
playMp3InBg("bg1.mp3")
stPlayStory(story)