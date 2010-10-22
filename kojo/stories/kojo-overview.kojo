val pageStyle = "color:#1e1e1e; margin:20px;"
val centerStyle = "text-align:center;"
val headerStyle = "text-align:center;font-size:110%;"
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
                <li>Computer programming and Critical thinking</li>
                <li>Math</li>
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
            Click on the <em>Run Program</em> button below to run the program.
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
            - by clicking on the <em>Copy Program</em> button below.
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
            Script editor before proceeding.
        </p>
    )
)

val pg4 = IncrPage(
    style=pageStyle,
    Para(
        <p>
            {headerKcp}
            <br/>
            Now run the program that you just copied - by clicking the <em>Run</em>
            button in the <em>Script Editor</em> toolbar.
        </p>
    ),
    Para(
        <p>
            Done?
            <br/>
            What is the length of the square that you just made?
            <br/>
            Input your answer below and click the <em>Check Answer</em> button
            (watch for a response in the status bar right at the bottom).
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
    ),
    tbcPara
)

val story = Story(pg1, pg2, pg3, pg4)
stClear()
playMp3InBg("bg1.mp3")
stPlayStory(story)
