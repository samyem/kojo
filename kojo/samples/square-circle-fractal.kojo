val Siz = 100.0
val NoColor = Color(0, 0, 0, 0)
def square = fillColor(ColorHSB(0, 100, 15)) * penColor(NoColor) -> Picture {
    repeat (4) {
        forward(Siz)
        right()
    }
}

def circle0(r: Double) = Picture {
    penUp()
    forward(r)
    penDown()
    repeat (360) {
        forward(2*math.Pi*r/360)
        right(1)
    }
}

def circle = fillColor(ColorHSB(240, 100, 20)) * penColor(NoColor) -> circle0(Siz/2)

def twoshapes(s: Double): Picture = {
    def shape = {
        GPics(
            trans(Siz, 0) -> square,
            scale(0.5) -> circle
        )
    }
    
    if (s < 0.01) {
        shape
    }
    else {
        GPics(
            shape,
            brit(0.2)  * trans(0.05 * Siz, Siz * 0.4) * scale(0.8) -> twoshapes(s*0.8)
        )
    }
}

clear()
invisible()
val pic = GPics(
    twoshapes(1),
    opac(-0.7) * trans(-Siz, -Siz) * fillColor(ColorG(-2*Siz, 0, blue, 5*Siz, 0, red, false)) * penColor(NoColor) -> circle0(2*Siz)
)
draw(pic)
