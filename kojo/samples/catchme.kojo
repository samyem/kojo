// sample game - with characters made out of tangram pieces.

val len = 4
val d = math.sqrt(2*len*len)
val d2 = d/2
val d4 = d/4

def p1 = pict { t =>
    import t._
    forward(len)
    right(135)
    forward(d2)
    right()
    forward(d2)
}

def p2 = p1

def p3 = pict { t =>
    import t._
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
    repeat (4) {
        forward(d4)
        right()
    }
}


def p5 = pict { t =>
    import t._
    right()
    forward(len/2)
    left()
    forward(len/2)
    left(135)
    forward(d2)
}

def p7 = pict { t =>
    import t._
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

def border(size: Double) = Pic { t =>
    import t._
    forward(size)
}

val lostMsg = trans(-20, 0) -> Pic { t =>
    import t._
    write("You Lost!")
}

val wonMsg = trans(-20, 0) -> Pic { t =>
    import t._
    write("You Won!")
}
    
clearWithUL(Cm)
val cb = canvasBounds
val xmax = cb.x.abs
val ymax = cb.y.abs
val goodguy = fillColor(yellow) * trans(xmax/3, 2) * scale(0.3) -> guy
val lostGoodguy = fillColor(orange) * trans(-50, 0) * scale(0.3) -> guy
val badguy = fillColor(black) * scale(0.3) -> guy
val badguy2 = fillColor(black) * trans(-xmax/2, 0) * scale(0.3) -> guy
val badguy3 = fillColor(black) * trans(2*xmax/3, 0) * scale(0.3) -> guy

val sleft = trans(-xmax, -ymax) -> border(cb.height)
val stop = trans(-xmax, ymax) * rot(-90) -> border(cb.width)
val sright = trans(xmax, -ymax) -> border(cb.height)
val sbot = trans(-xmax, -ymax) * rot(-90) -> border(cb.width)

val stage = GPics(
    sleft,
    stop,
    sright,
    sbot
)

playMp3Loop(installDir + "music-loops/Cave.mp3")
invisible()
setBackground(Color(150, 150, 255))
draw(stage, goodguy, badguy, badguy2, badguy3)
drawAndHide(lostGoodguy, lostMsg, wonMsg)

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

val others = List(badguy, badguy2, badguy3)

goodguy.act { me => 
    if (me.collision(others).isDefined) {
        stopAnimation()
        me.invisible()
        lostGoodguy.setPosition(me.position)
        lostGoodguy.setHeading(me.heading)
        lostGoodguy.visible()
        lostMsg.setPosition(0, 0)
        lostMsg.visible()
    }

    // an example of playing some game event music
    if (goodguy.collidesWith(stage)) {
        if(!isMp3Playing) {
            playMp3(installDir + "music-loops/DrumBeats.mp3")
        }
    }
    else {
        stopMp3()
    }
    
    if (time - startTime > 60 * 1000) {
        stopAnimation()
        wonMsg.setPosition(0, 0)
        wonMsg.visible()
    }
}

activateCanvas()
