val pageStyle = "color:#1e1e1e; margin:20px;"
val centerStyle = "text-align:center;"
val headerStyle = "text-align:center;font-size:110%;color:maroon;"
val codeStyle = "font-size:90%;"

val tbcPara =
    Para(
        <p>
            To be continued...
        </p>
    )

val pg1 = Page(
    <div style={pageStyle+centerStyle}>
        { for (i <- 1 to 5) yield {
                <br/>
            }
        }
        <h1>An Overview of Kojo</h1>
    </div>
)

val pg2 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            So what exactly is Kojo?
        </p>
    ),
    Para(
        <p>
            Kojo is a <em>Learning Environment</em>. It has many different features
            that help with the teaching and learning of concepts in:
            <ul>
                <li><a href="http://localpage/3">Computer programming and Critical thinking</a></li>
                <li><a href="http://localpage/7">Math</a></li>
                <li>Science</li>
                <li>Art and Creative thinking</li>
            </ul>
        </p>
    ),
    Para(
        <p>
            Let's look at some of these features...
        </p>
    )
)

val headerKcp =
    <span style={headerStyle}>
        Kojo and computer programming
        <hr/>
    </span>

val squareSample = """
    clear()
    repeat (4) {
        forward(100)
        right()
    }
"""

val pg3 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerKcp}
            <br/>
            You get started with computer programming within Kojo by writing
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
            Click the <em>Run Program</em> button below to run the program.
        </p>,
        code = {
            stAddButton ("Run Program") {
                clear()
                repeat (4) {
                    forward(100)
                    right()
                }
            }
        }
    )
)

val pg4 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerKcp}
            <br/>
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

val pg5 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerKcp}
            <br/>
            Done?
            <br/>
            <br/>
            What is the length of the square that you just made?
            <br/>
            <br/>
            Input your answer below and click the <em>Check Answer</em> button
            (<em>watch for a response in the status bar right at the bottom</em>).
        </p>,
        code = {
            stAddField("Length")
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
    )
)

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

val pg6 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerKcp}
            <br/>
            Let's look at a program that makes a much richer figure.
        </p>
    ),
    Para(
        <p>
            <pre style={codeStyle}>
                {xml.Unparsed(treeSample)}
            </pre>
            Click on the <em>Run Program</em> button below to run
            this program.
        </p>,
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
            
            stAddButton ("Run Program") {
                print("Running tree code")
                clear()
                invisible()
                setAnimationDelay(10)
                penUp()
                back(200)
                penDown()
                print("Drawing tree")
                tree(90)
            }
        }
    ),
    Para(
        <p>
            Don't worry if you don't fully understand the program above.
            You will, after you work with Kojo for a little bit...
        </p>
    )
)

val headerMath =
    <span style={headerStyle}>
        Kojo and Math
        <hr/>
    </span>

val pg7 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerMath}
            <br/>
            You can use Kojo to play with interesting ideas in Math.
        </p>
    ),
    Para(
        <p>
            Let's look at a couple:
            <ul>
                <li>Linear Equations in two variables</li>
                <li>Some Geometry proof</li>
            </ul>
        </p>
    )
)

val pg8 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerMath}
            <br/>
            A linear equation in two variables has the form:
            {stFormula("""\text{y = mx + c}""")}
        </p>
    ),
    Para(
        <p>
            Let's visualize this equation to see the role of m and c. Fill in 
            values for <em>m</em> and <em>c</em> below, and then Click the 
            <em>Plot</em> button.
        </p>,
        code = {
            def init() {
                clear()
                axesOn()
                setAnimationDelay(10)
                zoom(0.7, 0, 0)
            }
            init()
            stAddField("m")
            stAddField("c")
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

val story = Story(pg1, pg2, pg3, pg4, pg5, pg6, pg7, pg8)
stClear()
playMp3InBg("bg1.mp3")
stPlayStory(story)