def square(n: Int) = Picture {
    repeat (4) {
        forward(n)
        right()
    }
}

val NoColor = Color(0, 0, 0, 0)

def leaf(n: Int) = scale(0.45,1) * fillColor(ColorHSB(120, 100, 40)) * penColor(NoColor) -> square(n)
def stem(n: Int) = scale(0.35,1) * fillColor(ColorHSB(31, 100, 19)) * penColor(NoColor) -> square(n)


def leafBunch(n: Int): Picture = {
    def lb = GPics(
        trans(0, -3.5) -> stem(10),
        rot(80) -> leaf(7),
        trans(2.5, 3.5) * rot(-80) -> leaf(7)
    )
    
    if (n == 1) {
        lb
    }
    else {
        GPics(
            lb,
            trans(0, 5) * rot(-7) -> leafBunch(n-1)
        )
    }
}

def branchlet(n: Int): Picture = {
    def lb = GPics(
        trans(0, -4*n) * scale(1, 3) -> stem(2*n),
        rot(80) * scale(n*0.3) -> leafBunch(3),
        trans(n, 0) * rot(-80) * scale(n*0.3) -> leafBunch(3)
    )
    
    if (n <= 2) {
        lb
    }
    else {
        GPics(
            lb,
            trans(0, n*5) -> branchlet(n-1)
        )
    }
}

def tree(n: Int): Picture = {
    if (n==1) {
        branchlet(3)
    }
    else {
        GPics(
            stem(10*n),
            brit(0.1) * trans(n, 10*n) * rot(-30) * scale(1.02) -> tree(n-1),
            sat(-0.1) * trans(0, 8*n) * rot(40) * scale(0.98) -> tree(n-1)
        )
    }
}

clear()
invisible()
draw(tree(6))
