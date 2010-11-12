val pageStyle = "color:#1e1e1e; margin:15px;"
val centerStyle = "text-align:center;"
val headerStyle = "text-align:center;font-size:110%;color:maroon;"
val codeStyle = "font-size:90%;"
val smallNoteStyle = "color:gray;font-size:95%;"
val sublistStyle = "margin-left:60px;"

val code1_1 = """
    Mw.clear()
"""

val code1_2 = """
    Mw.clear()
    val fig = Mw.figure("f1")
    val p1 = Mw.point(1, 1)
    val p2 = Mw.point(2, 3)
    val p3 = Mw.point(5, 2)
    fig.add(p1, p2, p3)
    fig.show()
"""

val pg1 = Page(
    name = "",
    body =
        <body style={pageStyle}>
            <h1>Getting started with Mathworld</h1>
            <p style={smallNoteStyle}>
                This story tells you how to use Mathworld programatically from within
                your code.
            </p>
            <p>
                The first thing to do is to clear out Mathworld, so that your code
                can draw within it. You do this with:
                <pre style={codeStyle}>
                    {code1_1}
                </pre>
                You need points to do pretty much anything related to Geometry
                within Mathworld. You create a point by using the <code>Mw.point</code>
                function. <br/>
                To make these points visible within Mathworld, you need to:
                <ul>
                    <li>Make a figure using the <code>Mw.figure</code> function</li>
                    <li>Add the point to the figure via the <code>figure.add</code> command</li>
                    <li>Make the figure visible using the <code>figure.show</code> command</li>
                </ul>

                The following code demonstrates the above ideas:
                <pre style={codeStyle}>
                    {code1_2}
                </pre>
                Try the code out by cutting and pasting the text of the code to the
                Script Editor, and then clicking the <em>Run</em> button.
            </p>
        </body>
)

val code2_1 = """
  point(x: Double, y: Double): MwPoint
  point(on: MwLine, x: Double, y: Double): MwPoint
  line(p1: MwPoint, p2: MwPoint): MwLine
  lineSegment(p1: MwPoint, p2: MwPoint): MwLineSegment
  lineSegment(p: MwPoint, len: Double): MwLineSegment
  ray(p1: MwPoint, p2: MwPoint): MwRay
  intersect(l1: MwLine, l2: MwLine): MwPoint
  intersect(l: MwLine, c: MwCircle): Seq[MwPoint]
  intersect(c1: MwCircle, c2: MwCircle): Seq[MwPoint]
  angle(p1: MwPoint, p2: MwPoint, p3: MwPoint): MwAngle
  angle(p1: MwPoint, p2: MwPoint, size: Double): MwAngle
  text(content: String, x: Double, y: Double): MwText
  circle(center: MwPoint, radius: Double): MwCircle
"""
val pg2 = Page(
    name = "",
    body =
        <body style={pageStyle}>
            Mathworld has the following functions for creating and manipulating
            Geometric shapes:
            <pre style={codeStyle}>
                {code2_1}
            </pre>
            Play with these functions to get to know them better.
            <p style={smallNoteStyle}>
                Remember to add
                the shapes returned by these functions to a figure, to see them on the
                screen within Mathworld.
            </p>
            <p>
                To be continued...
            </p>
        </body>
)

val story = Story(pg1, pg2)

stClear()
stPlayStory(story)