val len = 4
val d = math.sqrt(2*len*len)
val d2 = d/2
val d4 = d/4

def p1 = pict { t =>
    import t._
    setAnimationDelay(10)
    invisible()
    forward(len)
    right(135)
    forward(d2)
    right()
    forward(d2)
}

def p2 = p1

def p3 = pict { t =>
    import t._
    invisible()
    setAnimationDelay(10)
    right()
    forward(len/2)
    left(135)
    forward(d4)
    left()
    forward(d4)
}

def p4 = p3

def p6 = pict { t =>
    import t._
    setAnimationDelay(10)
    invisible()
    repeat (4) {
        forward(d4)
        right()
    }
}


def p5 = pict { t =>
    import t._
    setAnimationDelay(10)
    invisible()
    right()
    forward(len/2)
    left()
    forward(len/2)
    left(135)
    forward(d2)
}

def p7 = pict { t =>
    import t._
    setAnimationDelay(10)
    invisible()
    right()
    forward(len/2)
    left(45)
    forward(d4)
    left(135)
    forward(len/2)
    left(45)
    forward(d4)
}

val tgreen = Color(0, 255, 0, 150)

def guy = GPics(
    rot(-120) -> p3,
    rot(150) * trans(0, -3.5) -> p1,
    flipY * rot(120) * trans(1.5, 0) -> p7,
    rot(150) * trans(-1, -4.5) -> p5,
    rot(-165) * trans(-4.47, -3.9) -> p4,
    rot(150) * trans(1, -6.5) -> p2,
    trans(-1.75, 5.4) * rotp(30, d4, 0) -> p6
)

def border(size: Int) = Pic { t =>
    import t._
    setAnimationDelay(0)
    invisible()
    forward(size)
}

val goodguy = fillColor(yellow) * trans(5, 2) * scale(0.3) -> guy
val badguy = fillColor(black) * scale(0.3) -> guy
val badguy2 = fillColor(black) * trans(-10, 0) * scale(0.3) -> guy
val badguy3 = fillColor(black) * trans(10, 0) * scale(0.3) -> guy

val sleft = trans(-18, -7) -> border(14)
val stop = trans(-18, 7) * rot(-90) -> border(36)
val sright = trans(18, -7) -> border(14)
val sbot = trans(-18, -7) * rot(-90) -> border(36)

val stage = GPics(
    sleft,
    stop,
    sright,
    sbot
)

val lostMsg = Pic { t =>
    import t._
    invisible()
    write("You Lost!")
}

val wonMsg = Pic { t =>
    import t._
    invisible()
    write("You Won!")
}
    

clearWithUL(Cm)
playMp3Loop("music-loops/Cave.mp3")
invisible()
setBackground(Color(150, 150, 255))
show(stage)
show(goodguy)
show(badguy)
show(badguy2)
show(badguy3)
lostMsg.translate(-20, 0)
wonMsg.translate(-20, 0)
show(lostMsg)
show(wonMsg)
lostMsg.invisible()
wonMsg.invisible()
lostMsg.translate(20, 0)
wonMsg.translate(20, 0)


val bf = 2
val sf = 1.5
val speed = 0.4
val VEdge = Vector2D(0, 1)
val HEdge = Vector2D(1, 0)
    
def badBehavior(me: Picture, bvec: Vector2D) {
    bvec.rotate(randomDouble(10)-5)
    me.transv(bvec)
    if (me.collidesWith(sright)) {
        val a = bvec.angle(VEdge)
        val a2 = if (a > 90) 180 - a else a
        val d = if (bvec.heading >= 0) 1 else -1
        bvec.rotate(2*d*a2)
        me.transv(bvec * bf)
    }
    else if (me.collidesWith(sleft)) {
        val a = bvec.angle(VEdge)
        val a2 = if (a > 90) 180 - a else a
        val d = if (bvec.heading >= 90 && bvec.heading <= 180) -1 else 1
        bvec.rotate(2*d*a2)
        me.transv(bvec * bf)
    }
    else if (me.collidesWith(stop)) {
        val a = bvec.angle(HEdge)
        val a2 = if (a > 90) 180 - a else a
        val d = if (bvec.heading >= 90) 1 else -1
        bvec.rotate(2*d*a2)
        me.transv(bvec * bf)
    }
    else if (me.collidesWith(sbot)) {
        val a = bvec.angle(HEdge)
        val a2 = if (a > 90) 180 - a else a
        val d = if (bvec.heading <= 0 && bvec.heading >= -90) 1 else -1
        bvec.rotate(2*d*a2)
        me.transv(bvec * bf)
    }
}

val vec = Vector2D(0, speed)
badguy.act { me => 
    badBehavior(me, vec)
}

val vec2 = Vector2D(speed, 0)
badguy2.act { me => 
    badBehavior(me, vec2)
}

val vec3 = Vector2D(-speed, 0)
badguy3.act { me => 
    badBehavior(me, vec3)
}

goodguy.act { me => 
    if (isKeyPressed(Kc.VK_RIGHT)) {
        me.translate(speed * sf, 0)
    }
    if (isKeyPressed(Kc.VK_LEFT)) {
        me.translate(-speed * sf, 0)
    }
    if (isKeyPressed(Kc.VK_UP)) {
        me.translate(0, speed * sf)
    }
    if (isKeyPressed(Kc.VK_DOWN)) {
        me.translate(0, -speed * sf)
    }
}

def time = System.currentTimeMillis
val startTime = time

val others = List(badguy, badguy2, badguy3, stage)

def collidesWith(p: Picture, ps: List[Picture]) = ps.exists(p collidesWith _)

goodguy.act { me => 
    if (collidesWith(me, others)) {
        stopAnimation()
        lostMsg.visible()
    }
    
    if (time - startTime > 60 * 1000) {
        stopAnimation()
        wonMsg.visible()
    }
}

activateCanvas()


