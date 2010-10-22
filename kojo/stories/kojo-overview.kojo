val pageStyle = "color:#1e1e1e; margin:20px;"
val centerStyle = "text-align:center;"
val headerStyle = "text-align:center;font-size:110%;"
val codeStyle = "font-size:90%;"

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
                <li>Computer programming and Critical thinking</li>
                <li>Math</li>
                <li>Science</li>
                <li>Art and Creative thinking</li>
            </ul>
        </p>
    ),
    Para(
        <p>
            Let's look at these features...
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
            Click on the button below to run the program
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
    ),
    Para(
        <p>
            Now copy the program to the script editor to play with it
            - by clicking on the button below
        </p>,
        code = {
            stAddButton ("Copy Program") {
                stSetScript(squareSample)
            }
        }
    ),
    Para(
        <p>
            To be continued...
        </p>
    )
)


val story = Story(pg1, pg2, pg3)
stClear()
// playMp3InBg("tattoo.mp3")
stPlayStory(story)