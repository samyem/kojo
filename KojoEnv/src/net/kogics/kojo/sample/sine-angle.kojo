// This is an early preview of the Canvas module
// This stuff is under development and is likely to change
// Also, code completion for the Canvas API is pretty rough right now

val darkGray = new Color(64,64,64)
def d2r(a: Double) = a * Math.Pi/180

def sineFn(offset: Int, scale: Double) {
    for (i <- 0 to 359) {
        Canvas.point(offset+i, scale * Math.sin(d2r(i)))
    }
    Canvas.setPenColor(darkGray)
    Canvas.setPenThickness(1)
    Canvas.line(offset, 0, offset+359, 0)
}

val radius = 100
var theta = 0
clear()
invisible()
Canvas.setPenColor(green)
Canvas.setPenThickness(3)
Canvas.circle(0, 0, radius)
sineFn(2*radius, radius)


Canvas.setPenColor(darkGray)
Canvas.setPenThickness(2)
Canvas.line(0, 0, radius, 0)

val txt = """
This animation shows how the sine of an angle
varies with the angle.

The angle of interest is made by the two lines
within the circle.

The sine of this angle is shown to the right of
the circle.
"""
Canvas.text(txt, -450, 100)

Canvas.refresh {
    Canvas.fgClear()
    Canvas.setPenColor(red)
    Canvas.setPenThickness(10)
    val p1 = Canvas.point(radius * Math.cos(d2r(theta)), radius * Math.sin(d2r(theta)))
    val p2 = Canvas.point(2*radius + (theta%360), radius * Math.sin(d2r(theta)))

    Canvas.setPenColor(darkGray)
    Canvas.setPenThickness(2)
    Canvas.line(0, 0, radius * Math.cos(d2r(theta)), radius * Math.sin(d2r(theta)))
    Canvas.line(p1, p2)

    Canvas.setPenThickness(1)
    Canvas.arc(0, 0, 15, 0, theta % 360)
    theta += 1
}