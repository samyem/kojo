# Introduction #

The Staging Module is currently being developed by Peter Lewerin.
The original impetus came from a desire to run Processing-style code in Kojo.

At this point, the shape hierarchy is the most complete part, but
utilities for color definition, time keeping etc are being added.

# Examples #

  * StagingHelloKojoExample
  * StagingArrayExample
  * StagingArrayTwoDeeExample
  * StagingChartExample
  * StagingClockExample
  * StagingColorWheelExample
  * StagingCreatingColorsExample
  * StagingDiagramExample
  * StagingDifferenceOfTwoSquaresExample
  * StagingEasingExample
  * StagingHueSaturationBrightnessExample
  * StagingSineOfAnAngleExample

# Overview #

## Points ##

Staging uses `net.kogics.kojo.core.Point` for coordinates.


## User Screen ##

The zoom level and axis orientations can be set using `screenSize`.


## Simple shapes and text ##

Given `Point`s or _x_ and _y_ coordinate values, simple shapes like dots,
lines, rectangles, ellipses, and elliptic arcs can be drawn.  Texts can
also be placed in this way.


## Complex Shapes ##

Given a sequence of `Point`s, a number of complex shapes can be drawn,
including basic polylines and polygons, and patterns of polylines/polygons.


## SVG Shapes ##

Given an SVG element, the corresponding shape can be drawn.


## Color ##

Color values can be created with the method `color`, or using a
_color-maker_.  The methods `fill`, `noFill`,
`stroke`, and `noStroke` set the colors used to draw the insides and edges
of figures.  The method `strokeWidth` doesn't actually affect color but is
typically used together with the color setting methods.  The method
`withStyle` allows the user to set fill color, stroke color, and stroke
width temporarily.



## Timekeeping ##

A number of methods report the current time.


## Math ##

A number of methods perform number processing tasks.

## Control ##

Some methods are used to control scripts, for examply by running and stopping
animations, clearing the screen, and handling mouse movements and clicks.


# Usage #


## Points ##

A point value can be created by calling the method

```
point(xval, yval)
```


The constant `O` (capital o) has the same value of `point(0, 0)`.

Point values can be added, subtracted, or negated

```
point(10, 20) + point(25, 0)
```

is the same as `point(35, 20)`

```
point(35, 20) - point(25, 0)
```

is the same as `point(10, 20)`

```
-point(10, -20)
```

is the same as `point(-10, 20)`

Tuples of `Double`s or `Int`s are implicitly converted to
`Point`s where applicable, if `Staging` has been imported.


## User Screen ##

The current width and height of the user screen is stored in the variables
`screenWidth` and `screenHeight` (both are 0 by default).


The methods `screenMid` and `screenExt` yield the coordinates of the middle
point of the user screen and the coordinates of the upper right corner of
the user screen (the extreme point), respectively.  Both return value
`point(0, 0)` if `screenSize` hasn't been called yet.

The dimensions of the user screen can be set by calling

```
screenSize(width, height)
```

The orientation of either axis can be reversed by negation, e.g.:

```
screenSize(width, -height)
```

makes (0,0) the upper left corner and (width, height) the lower right
corner.

The background color for the user screen area can be set:

```
background(color)
```


## Simple Shapes ##

### Dots ###

A dot is defined by a single coordinate pair, given as _x_, _y_ values
or as a `Point`.

```
dot(x, y)
dot(point)
```

### Lines ###

A line is defined either by
  1. two coordinate pairs, given as _x_, _y_ values, or
  1. two points.

```
line(x1, y1, x2, y2)
line(point1, point2)
```


### Rectangles ###

A rectangle is defined either by
  1. a coordinate pair for the lower left corner, given as _x_, _y_ values or as a `Point`, and a _width_-_height_ pair, or
  1. two points (in lower left / upper right order).

```
rectangle(x, y, width, height)
rectangle(point1, width, height)
rectangle(point1, point2)
```

```
square(x, y, size)
square(point, size)
```


### Rectangles with round corners ###

A rectangle with rounded corners is defined just like a rectangle, with
an additional _x-radius_, _y-radius_ pair or point that defines the
curvature of the corners.

```
roundRectangle(x, y, width, height, radiusx, radiusy)
roundRectangle(point1, width, height, radiusx, radiusy)
roundRectangle(point1, point2, radiusx, radiusy)
roundRectangle(point1, point2, point3)
```


### Ellipses ###

An ellipse is defined by a center point and a curvature.  The center point
can be given as _cx_, _cy_ coordinates or as a `Point`, and the
curvature can be given as _rx_, _ry_ radii or as an absolute `Point`.

(Thus, `ellipse((15, 15), 35, 25)` and `ellipse((15, 15), (50, 40))`
define the same shape.)

```
ellipse(cx, cy, rx, ry)
ellipse(p, rx, ry)
ellipse(p1, p2)
```

```
circle(cx, cy, radius)
circle(p, radius)
```


### Elliptical arcs ###

An elliptical arc is defined just like an ellipsis, with two additional
arguments for _starting angle_ and _extent_.  A starting angle of 0 is
the "three o'clock" direction, and 90 is the "twelve o'clock" direction.
Both angles are given in 1/360 degrees.

```
arc(cx, cy, rx, ry, s, e)
arc(cp, rx, ry, s, e)
arc(p1, p2, s, e)
```


The default arc shape is a "pieslice" / sector shape with two radii
connected by an elliptical segment.  This shape can also be created with
`pieslice`:

```
pieslice(...as above...)
```

Other kinds of arcs are open arcs, created by `openArc` and chords,
created by `chord`:
```
openArc(...as above...)
chord(...as above...)
```


### Vectors ###

A vector is a specialized line with an arrowhead at the endpoint.  An
additional argument specifies the length of the arrowhead.

```
vector(x1, y1, x2, y2, length)
vector(point1, point2, length)
```


### Stars ###

A star is a polygon with _n_ points.  The placement of the star is
specified with a `Point` or _x_, _y_ coordinates for the center. An
_inner_ radius specifies the corners between points, and an _outer_
radius specifies the points.

```
star(cx, cy, inner, outer, n)
star(p, inner, outer, n)
star(p1, p2, p3, n)
```



## Complex Shapes ##

### Polylines ###

```
polyline(points)
```


### Polygons ###

```
polygon(points)
```

```
triangle(point1, point2, point3)
```

```
quad(point1, point2, point3, point4)
```


### Line pattern ###

```
linesShape(points)
```

draws one line for each two points.

### Triangles pattern ###

```
trianglesShape(points)
```

draws one triangle for each three points.

### Triangle strip pattern ###

```
triangleStripShape(points)
```

draws a contiguous pattern of triangles.

### Quads pattern ###

```
quadsShape(points)
```

draws one quad for each four points.

### Quad strip pattern ###

```
quadStripShape(points)
```

draws a contiguous pattern of quads.

### Triangle fan pattern ###

```
triangleFanShape(points)
```

draws a pattern of triangles around a central point.

## SVG Shapes ##

A `rect` element takes a pair of _x_, _y_ coordinates for the lower left
corner and a _width_ and _height_.  Example:

```
svgShape(<rect x="15" y="15" width="25" height="5"/>)
```

A `circle` element takes a pair of _x_, _y_ coordinates for the center,
and a _radius_.  Example:

```
svgShape(<circle cx="15" cy="15" r="25"/>)
```

An `ellipse` element takes a pair of _x_, _y_ coordinates for the center,
a _horizontal radius_, and a _vertical radius_.  Example:

```
svgShape(<ellipse cx="15" cy="15" rx="35" ry="25"/>)
```

A `line` element takes two pairs of _x_, _y_ coordinates.  Example:

```
svgShape(<line x1="15" y1="15" x2="40" y2="20"/>)
```

A `polyline` element takes a _points_ argument, which is a string of
coordinates.  Example:

```
svgShape(<polyline points="15,15 25,35 40,20 45,25 50,10"/>)
```

A `polygon` element takes a _points_ argument, which is a string of
coordinates.  Example:

```
svgShape(<polygon points="15,15 25,35 40,20 45,25 50,10"/>)
```

A `path` element takes a _d_ argument, which is a string of path commands
with following coordinates.  Example:

```
svgShape(<path d="M15,15 40,15 40,20 15,20 z"/>)
```


### Styles for SVG shapes ###

Fill and stroke color, and stroke width, can be set for the element's
shape by adding attributes `fill`, `stroke`, and `stroke-width`  The
first two take a color name or descriptor as argument, while the latter
takes a number.  Example:

```
svgShape(<rect x="15" y="15" stroke-width="4.9" width="250" stroke="navy" height="50" fill="none"/>)
```


## Color ##

A color value can be created by calling one of the methods

```
color(red, green, blue)
color(value)
```

which each yields an instance of `java.awt.Color`.

Another way to specify color is through a color maker.  For instance, to get
a grayscale color, call `grayColors`.  The argument sets a limit to the
number of colors that can be specified.  E.g. `grayColors(100)` allows 101
shades of gray to be specified (0 to 100).  To get one of those colors, call
the color maker with the color number as argument.

```
val cm = grayColors(lim) ; cm(num)
```

creates a grayscale color with a "whiteness" equal to `num` / `lim`.

The color maker can also take a `Double` as argument, in which case the
resulting color has a "whiteness" of `val` (expected to be in the range
0.0 <= val <= 1.0):

```
val cm = grayColors(lim) ; cm(val) // where val is a Double
```

To get a non-opaque grayscale color maker, call `grayColorsWithAlpha` with
two limit values, one for the highest shade number and one for the highest
alpha number.  The color maker takes corresponding numbers for shade and
alpha.

```
val cm = grayColorsWithAlpha(grayLim, alphaLim) ; cm(grayNum, alphaNum)
```

creates a grayscale color with a "whiteness" equal to `grayNum` / `grayLim`
and a opacity equal to `alphaNum` / `alphaLim`.

The color maker can also take `Double` arguments, in which case the
resulting color has a "whiteness" of `grayVal` and opacity of `alphaVal`
(both expected to be in the range 0.0 <= val <= 1.0):

```
val cm = grayColorsWithAlpha(grayLim, alphaLim) ; cm(grayVal, alphaVal)
```

To create a color maker for RGB colors, use `rgbColors` and pass three
limit values to it (for red, green, and blue)

```
val cm = rgbColors(redLim, greenLim, blueLim) ; cm(redNum, greenNum, blueNum)
```

creates a color with a "redness" equal to `redNum` / `redLim`, etc.

The color maker can also take `Double` arguments, in which case the
resulting color has a "redness" of `redVal`, etc (all expected to be in
the range 0.0 <= val <= 1.0):

```
val cm = rgbColors(redLim, greenLim, blueLim) ; cm(redVal, greenVal, blueVal)
```

To create a color maker for RGB colors with transparency, use
`rgbColorsWithAlpha` and pass four limit values to it (for red, green, blue,
and alpha)

```
val cm = rgbColorsWithAlpha(redLim, greenLim, blueLim, alphaLim) ; cm(redNum, greenNum, blueNum, alphaNum)
```

creates a color with a "redness" equal to `redNum` / `redLim`, etc.

The color maker can also take `Double` arguments, in which case the
resulting color has a "redness" of `redVal`, etc (all expected to be in
the range 0.0 <= val <= 1.0):

```
val cm = rgbColorsWithAlpha(redLim, greenLim, blueLim, alphaLim) ; cm(redVal, greenVal, blueVal, alphaVal)
```

To create a color maker for HSB colors, use `hsbColors` and pass three
limit values to it (for hue, saturation, and brightness).

```
val cm = hsbColors(hueLim, saturationLim, brightnessLim) ; cm(hueNum, saturationNum, brightnessNum)
```

creates a color with an effective hue of `hueNum` / `hueLim`, etc.

The color maker can also take `Double` arguments, in which case the
resulting color has an effective hue of `hueVal`, etc (all expected to be in
the range 0.0 <= val <= 1.0):

```
val cm = hsbColors(hueLim, saturationLim, brightnessLim) ; cm(hueVal, saturationVal, brightnessVal)
```

Finally,

```
namedColor(colorName)
```

where _colorName_ is either

  * "none",
  * one of the names in this list: http://www.w3.org/TR/SVG/types.html#ColorKeywords, or
  * a string with the format "#rrggbb" (in hexadecimal)

returns the described color.

Linear interpolation between two colors is done using `lerpColor`:

```
lerpColor(colorFrom, colorTo, amount)
```

When drawing figures, the _fill_ color, which is used for the insides, and
the _stroke_ color, which is used for the edges, can be set and unset.

To set the fill color, call `fill`.

```
fill(color)
```

To unset the fill color, call `noFill`, or `fill` with a `null` argument.

```
noFill
fill(null)
```

To set the stroke color, call `stroke`.

```
stroke(color)
```

To unset the stroke color, call `noStroke`, or `stroke` with a `null` argument.

```
noStroke
stroke(null)
```

To set the stroke width, call `strokeWidth`.

```
strokeWidth(value)
```

To set the fill, stroke, and stroke width just for the extent of some lines
of code, use `withStyle`.

```
withStyle(fillColor, strokeColor, strokeWidth) { ...code... }
```

The Color type is 'pimped' with the following accessors:

```
alpha
red
blue
green
hue
saturation
brightness
```


## Math ##

```
constrain(value, min, max)
```

Constrains _value_ to be no less than _min_ and no greater than _max_.

```
norm(value, low, high)
```

Normalizes _value_ to the range 0.0 -- 1.0, such that _value_ == _low_
yields 0.0 and _value_ == _high_ yields 1.0, and values in between yield
the corresponding fraction.  Values outside the range yield results < 0.0 or
> 1.0, scaled to the _low_ / _high_ range.

```
map(value, min1, max1, min2, max2)
```

Maps _value_ from the range _low1_ -- _high1_ to the range
_low2_ -- _high2_.

```
sq(value)
```

Yields _value_ squared.

```
dist(x0, y0, x1, y1)
dist(p1, p2)
```

Yields the distance between two points.

```
mag(x, y)
mag(p)
```

Yields the distance between the given point and the point of origin.

```
lerp(low, high, value)
```

The inverse of `norm`; yields a number in the range _low_ -- _high_
which corresponds to the position of _value_ in the range 0.0 -- 1.0.

### Pi ###

For convenience, some Processing-style pi-related constants:

```
PI
TWO_PI
HALF_PI
QUARTER_PI
```

### Standard functions ###

While all standard mathematic functions can be used in Staging as
`math.`_func_, some frequently used functions are methods in the Staging
API as well (they call their standard-library equivalents).
```
sin(value)
cos(value)
tan(value)
```

### Conversions ###

Angles can be converted from degrees to radians and vice versa.

```
radians(value)
degrees(value)
```


## Timekeeping ##

A number of methods report the current time.


```
millis // milliseconds
second // second of the minute
minute // minute of the hour
hour   // hour of the day
day    // day of the month
month  // month of the year (1..12)
year   // year C.E.
```


## Control ##

```
loop { ...body... }
```

Repeatedly executes the lines in the body.

```
stop
```

Stops a running `loop`.

```
reset
wipe
```

`reset` erases all shapes and makes the turtle invisible.  `wipe` erases
all shapes drawn within `loop`.

```
mouseX
mouseY
```

Within `loop`, gives the _x_ / _y_ coordinates of the mouse pointer.

```
pmouseX
pmouseY
```

Within `loop`, gives the _x_ / _y_ coordinates of the mouse pointer
from the _previous_ iteration of `loop`.

```
mouseButton
```

Holds the number of the last mouse button pressed (0: none, 1: left,
2: center, 3: right).  The constants `LEFT`, `CENTER`, `RIGHT` are
provided.

```
mousePressed
```

Yields `true` if any mouse button is pressed and `false` otherwise.


## Shape methods ##

All drawn shapes have the following methods and accessors.

```
hide
show
```

Makes the shape invisible / visible.

```
rotate(amount)
```

Rotates the shape _amount_ degrees counterclockwise (for positive amounts)
or clockwise (for negative amounts).

```
rotateTo(angle)
```

Rotates the shape to the specified angle; rotating to 90 degrees yields the
original orientation.

```
scale(amount)
```

Scales the shape by _amount_; 2 means doubling of each dimension, 0.5 means
halving them.

```
scaleTo(size)
```

Scales the shape to the specified size; scaling to 1.0 yields the
original size.

```
translate(amount)
```

Translates (moves) the shape by _amount_, which is a `Point`.  The _x_
component of the point specifies the amount of horizontal movement, while
the _y_ component specifies the vertical.

```
offset
```

Yields the _x_ / _y_ distance the shape has traveled.