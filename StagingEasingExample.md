The following are two versions of the _Easing_ example from Processing, which features a ball following the mouse pointer. The first version is a direct port, and creates a new ball within every iteration of the animation loop. The second version is written in a more natural style for Staging - it translates a pre-created ball within the animation loop, instead of creating a new one for every iteration.

The second version is more efficient that the first version - as can be seen from the following example: with the easing factor set to 1.0, the ball lags behind the mouse pointer a wee bit for the first version, while it stays with the mouse pointer for the second version.

The above discussion relates to the fact that Processing is based on an immediate mode Graphics API, while Staging is based on a retained mode Graphics API. This means that the immediate mode style of programming (draw new graphics objects within the animation loop), while supported by Staging, is slightly less efficient that the retained mode style of programming (transform existing graphics objects).

### Version 1 ###
```
val S = Staging
var x = 0.0;
var y = 0.0;
val easing = 0.05;
S.reset
S.screenSize(200, 200)
S.background(color(100, 100, 100));
S.noStroke
S.fill(white)


S.loop { 
    S.wipe
    val dx = S.mouseX - x;
    if(math.abs(dx) > 1) {
        x += dx * easing;
    }
  
    val dy = S.mouseY - y;
    if(math.abs(dy) > 1) {
        y += dy * easing;
    }
  
    S.ellipse(x, y, 15, 15);
}
```


### Version 2 ###
```
val S = Staging
val easing = 0.05;
S.reset
S.screenSize(200, 200)
S.background(color(100, 100, 100));
S.noStroke
S.fill(white)
val ell = S.ellipse(0, 0, 15, 15);


S.loop { 
    val dx = S.mouseX - ell.offset.x;
    val dy = S.mouseY - ell.offset.y;
    ell.translate(S.point(dx * easing, dy * easing))
}
```