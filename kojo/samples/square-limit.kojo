// based on ideas from:
// http://www.frank-buss.de/lisp/functional.html

val sf = 40.0
val size = 16
val bxf = size/2 * sf 
val byf = size/2 * sf

def above(p1: Picture, p2: Picture) = scale(1, 0.5) -> VPics(
    p2,
    p1
)

def beside(p1: Picture, p2: Picture) = scale(0.5, 1) -> HPics(
    p1,
    p2
)

def rot45(p: Picture) = rotp(45, 0, 140) * trans(0, 140) * scale(1/math.sqrt(2)) * trans(0, -140) -> p

def over(p1: Picture, p2: Picture) = GPics(
    p1,
    p2
)

def quartet(p1: Picture, p2: Picture, p3: Picture, p4: Picture) = {
    above(
        beside(p1, p2),
        beside(p3, p4)
    )
}

def cycle(p: => Picture) = {
    quartet(
        p, 
        rotp(-90, bxf, byf) -> p,
        rotp(-270, bxf, byf) -> p,
        rotp(-180, bxf, byf) -> p
    )
}

def rotx = rotp(90, bxf, byf) 

type PP = ((Int, Int), (Int, Int))

val p0 = List(((4,4),(6,0)), ((0,3),(3,4)), ((3,4),(0,8)), 
              ((0,8),(0,3)), ((4,5),(7,6)), ((7,6),(4,10)), 
              ((4,10),(4,5)), ((11,0),(10,4)), ((10,4),(8,8)),
              ((8,8),(4,13)), ((4,13),(0,16)), ((11,0),(14,2)),
              ((14,2),(16,2)), ((10,4),(13,5)), ((13,5),(16,4)),
              ((9,6),(12,7)), ((12,7),(16,6)), ((8,8),(12,9)),
              ((12,9),(16,8)), ((8,12),(16,10)), ((0,16),(6,15)),
              ((6,15),(8,16)), ((8,16),(12,12)), ((12,12),(16,12)),
              ((10,16),(12,14)), ((12,14),(16,13)), ((12,16),(13,15)),
              ((13,15),(16,14)), ((14,16),(16,15)))

val q0 = List(((2, 0),(4, 5)), ((4, 5),(4, 7)), ((4, 0),(6, 5)),
              ((6, 5),(6, 7)), ((6, 0),(8, 5)), ((8, 5),(8, 8)),
              ((8, 0),(10, 6)), ((10, 6),(10, 9)), ((10, 0),(14, 11)),
              ((12, 0),(13, 4)), ((13, 4),(16, 8)), ((16, 8),(15, 10)),
              ((15, 10),(16, 16)), ((16, 16),(12, 10)), ((12, 10),(6, 7)),
              ((6, 7),(4, 7)), ((4, 7),(0, 8)), ((13, 0),(16, 6)),
              ((14, 0),(16, 4)), ((15, 0),(16, 2)), ((0, 10),(7, 11)),
              ((9, 12),(10, 10)), ((10, 10),(12, 12)), ((12, 12),(9, 12)),
              ((8, 15),(9, 13)), ((9, 13),(11, 15)), ((11, 15),(8, 15)),
              ((0, 12),(3, 13)), ((3, 13),(7, 15)), ((7, 15),(8, 16)),
              ((2, 16),(3, 13)), ((4, 16),(5, 14)), ((6, 16),(7, 15)))


val r0 = List(((0, 12),(1, 14)), ((0, 8),(2, 12)), ((0, 4),(5, 10)),
              ((0, 0),(8, 8)), ((1, 1),(4, 0)), ((2, 2),(8, 0)),
              ((3, 3),(8, 2)), ((8, 2),(12, 0)), ((5, 5),(12, 3)),
              ((12, 3),(16, 0)), ((0, 16),(2, 12)), ((2, 12),(8, 8)),
              ((8, 8),(14, 6)), ((14, 6),(16, 4)), ((6, 16),(11, 10)),
              ((11, 10),(16, 6)), ((11, 16),(12, 12)), ((12, 12),(16, 8)),
              ((12, 12),(16, 16)), ((13, 13),(16, 10)), ((14, 14),(16, 12)),
              ((15, 15),(16, 14)))

val s0 = List(((0, 0),(4, 2)), ((4, 2),(8, 2)), ((8, 2),(16, 0)),
              ((0, 4),(2, 1)), ((0, 6),(7, 4)), ((0, 8),(8, 6)),
              ((0, 10),(7, 8)), ((0, 12),(7, 10)), ((0, 14),(7, 13)),
              ((8, 16),(7, 13)), ((7, 13),(7, 8)), ((7, 8),(8, 6)),
              ((8, 6),(10, 4)), ((10, 4),(16, 0)), ((10, 16),(11, 10)),
              ((10, 6),(12, 4)), ((12, 4),(12, 7)), ((12, 7),(10, 6)),
              ((13, 7),(15, 5)), ((15, 5),(15, 8)), ((15, 8),(13, 7)),
              ((12, 16),(13, 13)), ((13, 13),(15, 9)), ((15, 9),(16, 8)),
              ((13, 13),(16, 14)), ((14, 11),(16, 12)), ((15, 9),(16, 10)))

def line(p1: (Int, Int), p2: (Int, Int)) {
    penUp()
    moveTo(p1._1, p1._2)
    penDown()
    moveTo(p2._1, p2._2)
}

def lp(linePoints: List[PP]) = penWidth(15/sf) * penColor(brown) * scale(sf) -> Picture {
    linePoints.foreach { pp => line(pp._1, pp._2) }
}

def p = lp(p0)
def q = lp(q0)
def r = lp(r0)
def s = lp(s0)

clear()
setBackground(Color(242, 242, 0))
// setBackgroundV(orange, yellow)
invisible()

def t = quartet(p, q, r, s)
def u = cycle(rotx(q))
def side1 = scale(1, 0.5) -> beside(rotx(t), t)
def side2 = quartet(side1, side1, rotx(t), t)
def corner1 = scale(0.5, 0.5) -> u
def corner2 = quartet(trans(size * sf/2, 0) -> corner1, side1, rotx(side1), u)
def pcorner = quartet(corner2, side2, rotx(side2), rotx(t))
def fishes = cycle(pcorner)

draw(trans(-300, -300) -> fishes)
