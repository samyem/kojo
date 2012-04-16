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
    right()
    forward(r)
    left()
    penDown()
    
    circle(r)
    
    penUp()
    left()
    forward(r)
    right()
    penDown()
}

def circ = fillColor(ColorHSB(240, 100, 20)) * penColor(NoColor) -> circle0(Siz/2)

def twoshapes(s: Double): Picture = {
    def shape = {
        GPics(
            trans(Siz/2, -Siz/4) -> square,
            scale(0.5) -> circ
        )
    }
    
    if (s < 0.01) {
        shape
    }
    else {
        GPics(
            shape,
            brit(0.2)  * trans(0,Siz * 0.4) * scale(0.8) -> twoshapes(s*0.8)
        )
    }
}

clear()
invisible()
val pic = GPics(
    twoshapes(1),
    opac(-0.7) * fillColor(ColorG(-1.5*Siz, 0, blue, 3*Siz, 0, red, false)) * penColor(NoColor) -> circle0(2*Siz)
)
draw(pic)
