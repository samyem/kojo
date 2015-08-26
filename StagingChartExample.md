# Chart #

This example shows a labeled dataset as a scatter plot, a bar chart, or a line chart.

# Script #

```
val S = Staging

S.reset()
S.screenSize(560, 460)

object Chart {
  val displayColor = S.namedColor("lightsteelblue")

  var size: Int = _
  var labels: Array[String] = _
  var values: Array[Double] = _
  var scale: Double = _
  var width: Double = _
  def apply(data: Array[(String, Double)]) = {
    size = data.size
    labels = data map (_._1)
    values = data map (_._2)
    scale = S.screenHeight / values.max.ceil
    width = S.screenWidth / data.size
    S.stroke(black)
    S.strokeWidth(1.5)
    S.line(S.O, S.screenExt.onX)
    S.line(S.O, S.screenExt.onY)
    this
  }

  def displayLabel(label: String, x: Double) {
    S.withStyle(null, black, .5) {
      S.text(label, x + 10, -5)
      S.line(x + width, 0, x + width, S.screenHeight)
    }
  }

  def scatterPlot {
    for (i <- 0 until size ; x = i * width) {
      S.withStyle(displayColor, black, .5) {
        S.circle(x + width / 2, values(i) * scale, 1.5)
        displayLabel(labels(i), x)
      }
    }
  }

  def barChart {
    for (i <- 0 until size ; x = i * width) {
      S.withStyle(displayColor, black, .5) {
        S.rectangle(x + width / 4, 0, width / 2, values(i) * scale)
        displayLabel(labels(i), x)
      }
    }
  }

  def lineChart {
    var xy: Option[(Double, Double)] = None
    
    for (i <- 0 until size ; x = i * width) {
      S.withStyle(black, displayColor, 2.5) {
        val (x1, y1) = (x + width / 2, values(i) * scale)
        xy match {
          case Some((x0, y0)) =>
            S.line(x0, y0, x1, y1)
          case None =>
            S.dot(x1, y1)
        }
        xy = Some((x1, y1))
      }
      displayLabel(labels(i), x)
    }
  }
}

Chart(Array(
  "Foo" -> 4., 
  "Bar" -> 5.6, 
  "Baz" -> 3.8, 
  "Qux" -> 6.2, 
  "Din" -> 5.8
)).scatterPlot
```