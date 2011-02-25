val S = Staging

val pageStyle = "color:white;background:green;margin:15px;"
var n1 = 0
var d1 = 0
var n2 = 0
var d2 = 0
var plcm = 0

def gcd(n1: Int, n2: Int): Int = {
    if (n2 == 0) n1 else gcd(n2, n1 % n2)
}

def lcm(n1: Int, n2: Int) = n1 * n2 / gcd(n1, n2)

val rad = 50

var x = -200
var y = 0

def nextCol() {
    x += rad*2 + 10
}

def nextRow() {
    x = -200
    y -= rad*2 + 40
}

def showFrac(n: Int, d: Int) {
    S.circle(x, y, rad)

    if (n <= d) {
        // proper fraction
        // first show portions not covered by the fraction
        repeati (d-(n+1)) { i =>
            S.arc(x, y, rad, rad, 360.0/d * (n+i), 360.0/d)
        }
        // then show portions covered by the fraction
        S.withStyle (green, blue, 3) {
            repeati (n) { i =>
                S.arc(x, y, rad, rad, 360.0/d * (i-1), 360.0/d)
            }
        }
        S.text("%d/%d" format(n,d), x-10, y-rad-5)
        nextCol()
    }
    else {
        showFrac(d, d)
        showFrac(n-d, d)
    }
}

val pg1 = Page(
    name = "",
    body =
        <body style={pageStyle}>
            <p>
                In the fields below, put in the values of the fractions
                that you want to add. <em>n1</em> is the numerator of the
                first fraction, and <em>d1</em> is its denominator. <em>n2</em>
                and <em>d2</em> are the numerator and the denominator of the second
                fraction. <br/><br/>
                Click on the <em>Proceed </em> button after inputing the fractions.
            </p>
        </body>,
    code = {
        S.clear()
        stAddField("n1", "")
        stAddField("d1", "")
        stAddField("n2", "")
        stAddField("d2", "")
        stAddButton ("Proceed") {
            n1 = stFieldValue("n1", 1)
            d1 = stFieldValue("d1", 1)
            n2 = stFieldValue("n2", 1)
            d2 = stFieldValue("d2", 1)
            plcm = lcm(d1, d2)
            stNext()
        }
    }
)


def init() {
    S.clear()
    S.setPenColor(new Color(128, 128, 128))
    x = -200
    y = 0
}

def fracs1() {
    showFrac(n1, d1)
    showFrac(n2, d2)
}

def fracs2() {
    showFrac(n1*plcm/d1, plcm)
    showFrac(n2*plcm/d2, plcm)
}

val pg2 = Page(
    name = "",
    body =
        <body style={pageStyle}>
            <p>
                You want to calculate:
                {stFormula("""\frac{%s}{%s} + \frac{%s}{%s}""" format(n1, d1, n2, d2))}
                <br/>
                The turtle canvas shows you a visual representation of the
                fractions that you want to add.
            </p>
        </body>,
    code = {
        init()
        zoom(1, -150, 0)
        fracs1()
    }
)

val pg3 = IncrPage(
    name = "",
    style = pageStyle,
    body = List(
        Para(
            <p>
                In adding the two given fractions, the first step is to determine
                the LCM of <em>d1</em> = {d1} and <em>d2</em> = {d2}.
                <br/>
                <br/>
                Calulate the LCM yourself before moving on.
            </p>
        ),
        Para(
            <p>
                The LCM of {d1} and {d2} is {plcm}
            </p>
        )
    )
)

val pg4 = Page(
    name = "",
    body =
        <body style={pageStyle}>
            <p>
                Next, we covert the two given fractions to equivalent fractions,
                with the LCM as the common denominator.
                <br/>
                <br/>
                Do this conversion on your own before moving on.
            </p>
        </body>,
    code = {}
)

val pg5 = Page(
    name = "",
    body =
        <body style={pageStyle}>
            <p>
                The first fraction - {"%s/%s" format(n1, d1)} gets converted to {"%s/%s" format(n1*plcm/d1, plcm)}<br/>
                The second fraction - {"%s/%s" format(n2, d2)} gets converted to {"%s/%s" format(n2*plcm/d2, plcm)}<br/>
                <br/>
                The turtle canvas shows you a visual representation of these two new
                fractions.
            </p>
        </body>,
    code = {
        init()
        zoom(1, -150, -150)
        fracs1()
        nextRow()
        fracs2()
    }
)

val pg6 = Page(
    name = "",
    body =
        <body style={pageStyle}>
            Now, we just add the numerators for the converted fractions to get the answer.
            {stFormula("""\frac{%s}{%s} + \frac{%s}{%s} = \frac{%s}{%s}"""
                       format(n1*plcm/d1, plcm,
                              n2*plcm/d2, plcm,
                              n1*plcm/d1+n2*plcm/d2, plcm))}
            <br/>
            <br/>
            The turtle canvas shows you a visual representation of the answer.
        </body>,
    code = {
        init()
        zoom(1, -150, -300)
        fracs1()
        nextRow()
        fracs2()
        nextRow()
        showFrac(n1*plcm/d1+n2*plcm/d2, plcm)
    }
)

val st = Story(pg1, pg2, pg3, pg4, pg5, pg6)
stClear()
stPlayStory(st)
