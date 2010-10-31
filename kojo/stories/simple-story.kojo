
val pg1 = Page(
    name = "",
    body =
        <body>
            This is a simple example of a story - to help you
            to get started writing stories. Look at the source code
            of the story within the <em>Script Editor</em>, and relate it
            to what you see in the storyteller window.<br/><br/>
            <strong>This is bold text</strong><br/>
            <em>This is italic text</em><br/>
            <p>
                This is the start of a new para.<br/>
                Press the <em>Make Square</em> button below to make a square.
            </p>
        </body>,
    code = {
        stAddButton ("Make Square") {
            clear()
            repeat (4) {
                forward(100)
                right()
            }
        }
    }
)

val pg2 = Page(
    name = "",
    body =
        <body>
            On this page, we ask the user for the size of the square to be made.
            The user can enter this size in the field below.
        </body>,
    code = {
        stAddField("Size", 100)
        stAddButton ("Make Square") {
            clear()
            val size = stFieldValue("Size", 100)
            repeat (4) {
                forward(size)
                right()
            }
        }
    }
)

val story = Story(pg1, pg2)

stClear()
stPlayStory(story)