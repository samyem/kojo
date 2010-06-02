// This is an early preview of the Canvas module
// This stuff is under development and is likely to change
// Also, code completion for the Canvas API is pretty rough right now

val darkGray = color(64,64,64)
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
This animation shows how the magnitude of
an angle and its sine are defined, and how
they relate to each other.

The angle of interest is AOP. Its magnitude
 is defined (in radians) as the ratio of
lengths - AP/OP.
The sine of this angle is the ratio of
lengths - MP/OP

If you consider OP to be equal to one unit
in length, the sine of AOP is equal to MP.
This magnitude is shown by the red dot on
the curve to the right of the circle.
"""
Canvas.text(txt, -450, 100)

Canvas.refresh {
    Canvas.fgClear()
    Canvas.setPenColor(red)
    Canvas.setPenThickness(10)
    val p1 = Canvas.point(radius * Math.cos(d2r(theta)), radius * Math.sin(d2r(theta)))
    val p2 = Canvas.point(2*radius + (theta%360), radius * Math.sin(d2r(theta)))

    Canvas.setPenColor(blue)
    Canvas.setPenThickness(2)
    Canvas.line(0, 0, radius * Math.cos(d2r(theta)), radius * Math.sin(d2r(theta)))
    Canvas.arc(0, 0, radius, 0, theta % 360)
    Canvas.line(p1.x, p1.y, p1.x, 0)
    Canvas.setPenColor(darkGray)
    Canvas.line(0, 0, p1.x, 0)
    Canvas.line(p1, p2)

    Canvas.setPenThickness(1)
    Canvas.arc(0, 0, 15, 0, theta % 360)
//    Canvas.text("Angle = %3d°" format(theta % 360), 0, 0)
    Canvas.text("O", 0, 0)
    Canvas.text("A", radius, 0)
    Canvas.text("P", p1.x + 7, p1.y + 15)
    Canvas.text("M", p1.x + 4, 18)
    Canvas.text("Sine of angle AOP = %.2f" format(Math.sin(d2r(theta))), 2*radius, 0)
    Canvas.text("Angle AOP = %.2f radians = %d degrees" format(d2r(theta % 360), theta % 360), radius+15, -radius/2f)
    theta += 1
}