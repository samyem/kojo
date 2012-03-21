def leaf(l: Int, t: Double) = Picture {
    setPenColor(ColorHSB(120, 100, 40))
    setPenThickness(t)
    arc(l/4, 190)
    repeat (l/4) {
        forward(1)
        turn(randomDouble(6) - 3)
    }
}

def branch(l: Int, t: Double) = Picture {
    setPenThickness(t)
    repeat (l) {
        forward(1)
        turn(randomDouble(6) - 3)
    }
    moveTo(0, l)
}

def tree(n: Int): Picture = {
    def th(n: Double) = math.max(n/10, 1)
    def branches = {
        GPics(
            branch(n, th(n)),
            trans(0, 3.0*n/4) -> leaf(n, th(n)),
            flipY * trans(0, n/2.0) -> leaf(n, th(n)),
            rot(-30) -> branch(n, th(n)),
            rot(40) -> branch(n, th(n))
        )
    }
    
    if (n < 21) {
        branches
    }
    else {
        GPics(
            branch(n, th(n)),
            trans(0, n) * rot(-15) -> tree(n-10),
            trans(0, n) * rot(20) -> tree(n-10)
        )
    }
}

clear()
setBackgroundV(red, yellow)
invisible()

draw(penColor(C.brown) * trans(0, -50) -> tree(70))
