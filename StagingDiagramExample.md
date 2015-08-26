# Diagram #

This example shows a labeled dataset as a scatter plot, a bar chart, or a line chart.

**Note:** this example has grown ridiculously long and complicated.  See StagingChartExample for a better version.

# Script #

```
val S = Staging

case class Edges(left: Double, bottom: Double, right: Double, top: Double)

class Bounds(
  val x: Double,
  val y: Double,
  val width: Double,
  val height: Double
) {
  def this() = this(0, 0, S.screenWidth, S.screenHeight)

  def inset(amt: Double) = {
    if (width >= 2 * amt && height >= 2 * amt)
      new Bounds(x + amt, y + amt, width - 2 * amt, height - 2 * amt)
    else
      this
  }

  def hdiv(n: Int) = {
    val w = width / n
    for (i <- 0 until n ; xx = x + i * w) yield
      new Bounds(xx, y, w, height)
  }

  def sliceLeft(amt: Double) = {
    if (width <= amt) (this, new Bounds(x + width, y, 0, height))
    else (new Bounds(x, y, amt, height), new Bounds(x + amt, y, width - amt, height))
  }

  def hlerp(fraction: Double) = S.lerp(x, x + width, fraction)

  def vlerp(fraction: Double) = S.lerp(y, y + height, fraction)

  def draw {
    S.rectangle(x, y, width, height)
  }

  def edges = Edges(x, y, x + width, y + height)

  override def toString = "Bounds(" + x + ", " + y + ", " + width + ", " + height + ")"
}

class ScaleArea(bounds: Bounds) {
  // The scale area knows how to display itself within its given bounds,
  // given a minimum and maximum value.
  def display(min: Int, max: Int) {
    val edges = bounds.edges
    S.withStyle(null, black, 1) {
      (min to max) foreach { v =>
        val mark = S.map(v, min, max, edges.bottom, edges.top)
        S.text("%10d" format v, edges.left, mark + 10)
        S.line(edges.right - 4, mark, edges.right, mark)
      }
      S.strokeWidth(1.5)
      S.line(edges.right, edges.bottom, edges.right, edges.top)
    }
  }
}

class ChartArea(bounds: Bounds) {
  def display(
    labels: Array[String],
    fractions: Array[Double],
    baseline: Double,
    displayValue: (Double, Bounds, Double) => Unit
  ) {
    // To represent the data in the kinds of charts used in this example, each item 
    // in order gets its own column, with the label written beneath the column and 
    // the value indicated by a distance from the column's baseline.

    val columns = bounds hdiv labels.size

    // The y position of the x-axis.
    def base = bounds.vlerp(baseline)

    val labelWidth = 16
    
    for (i <- 0 until columns.size ; c = columns(i) ; f = fractions(i)) {
      displayValue(baseline, c, f)
      
      val edges = c.edges
      val label = if (labels(i).size < labelWidth) {
                    val padding = " " * ((labelWidth - labels(i).size) / 2)
                    padding + labels(i) + padding
                  }
                  else labels(i)
      S.withStyle(null, black, 1) {
        S.text(label, edges.left, base - 5)
        S.line(edges.left, base - 4, edges.left, base)
        S.line(edges.right, base - 4, edges.right, base)
        S.strokeWidth(1.5)
        S.line(edges.left, base, edges.right, base)
      }
    }
  }
}

class Chart(
  val bounds: Bounds,
  val data: Array[(String, Double)],
  val displayValue: (Double, Bounds, Double) => Unit
) {
  // Split the chart area into two parts.
  val areas = bounds sliceLeft 70

  // The first part is where the y-axis and the value scale will be drawn.
  val scaleArea: ScaleArea = new ScaleArea(areas._1)

  // The other part has the x-axis, the column labels and the value symbols.
  val chartArea: ChartArea = new ChartArea(areas._2)

  val labels = data map (_._1)

  // Determine the value scale.
  val values = data map (_._2)
  val yMin = if (values.min < 0) values.min.floor.toInt else 0
  val yMax = values.max.ceil.toInt

  // In order to create the value symbols, the values must be normalized
  // to the value scale.
  def norm(value: Double) = S.norm(value, yMin, yMax)
  val normValues = values map norm

  def display() = {
    scaleArea.display(yMin, yMax)
    chartArea.display(labels, normValues, norm(0), displayValue)
  }
}

object Chart {
  val displayColor = S.namedColor("lightsteelblue")

  def apply(bounds: Bounds, chartMaker: Bounds => Chart) = {
    S.withStyle(color(0xeeeeee), null, 1) {
      bounds.draw
    }
    chartMaker(bounds.inset(S.screenWidth / 8))
  }
}

object ScatterPlot {
  // Returns a function that, given bounds, creates a Chart structure.
  def apply(d: Array[(String, Double)]) = { (b: Bounds) =>
    new Chart(b, d, { (baseline: Double, bounds: Bounds, fraction: Double) =>
      val x = bounds.hlerp(.5)
      val y = bounds.vlerp(fraction)

      S.withStyle(Chart.displayColor, black, .5) {
        S.circle(x, y, 1.5)
      }
    })
  }
}

object BarChart {
  // Returns a function that, given bounds, creates a Chart structure.
  def apply(d: Array[(String, Double)]) = { (b: Bounds) =>
    new Chart(b, d, { (baseline: Double, bounds: Bounds, fraction: Double) =>
      val width = bounds.width / 2.
      val x     = bounds.hlerp(.25)
      val (y, height) = Array(bounds.vlerp(baseline), bounds.vlerp(fraction)) sortWith
        (_<=_) match { case Array(a, b) => (a, b - a) }

      S.withStyle(Chart.displayColor, black, .5) {
        S.rectangle(x, y, width, height)
      }
    })
  }
}

object LineChart {
  var xy: Option[(Double, Double)] = None

  // Returns a function that, given bounds, creates a Chart structure.
  def apply(d: Array[(String, Double)]) = { (b: Bounds) =>
    new Chart(b, d, { (baseline: Double, bounds: Bounds, fraction: Double) =>
      val x = bounds.hlerp(.5)
      val y = bounds.vlerp(fraction)

      S.withStyle(null, Chart.displayColor, 2) {
        xy match {
          case Some((x0, y0)) =>
            S.line(x0, y0, x, y)
          case None =>
            S.dot(x, y)
        }
        xy = Some((x, y))
      }
    })
  }
}

S.reset()
S.screenSize(560, 460)

Chart(new Bounds, ScatterPlot(
  /* or: BarChart( ... ), LineChart( ... ) */
  Array("Foo" -> 4., "Bar" -> 5.6, "Baz" -> 3.8, "Qux" -> 6.2, "Din" -> 5.8)
)).display
```