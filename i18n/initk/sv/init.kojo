//Contributed by Bjorn Regnell 
//Swedish Turtle wrapper for Kojo
def sudda = {clear();clearOutput} 
object minPadda extends Padda { dennaPadda.invisible; dennaPadda = turtle0 } 
import minPadda._  
class Padda {
  var dennaPadda = newTurtle
  var steglangd = 20.0
  def fram(steg:Double) = dennaPadda.forward(steg)
  def fram : Unit = dennaPadda.forward(steglangd)
  def bak(steg:Double) = dennaPadda.back(steg)
  def bak : Unit = dennaPadda.back(steglangd)
  def vänster = dennaPadda.left
  def höger = dennaPadda.right
  def vrid(vinkel:Double) = dennaPadda.turn(vinkel)
  def hoppa(x:Double, y:Double) = dennaPadda.jumpTo(x, y)
  def hoppa(steg:Double) = {pennaUpp; fram(steg); pennaNer}
  def hoppa = {pennaUpp; fram; pennaNer}
  def hem = dennaPadda.home()
  def nos(x:Double, y:Double) = dennaPadda.towards(x, y)
  def nos(vinkel:Double) = dennaPadda.setHeading(vinkel)
  def öst = dennaPadda.setHeading(0)
  def väst = dennaPadda.setHeading(180)
  def norr = dennaPadda.setHeading(90)
  def söder = dennaPadda.setHeading(-90)  
  def fördröjning(n: Long) = dennaPadda.setAnimationDelay(n)
  def skriv(t : Any) = dennaPadda.write(t)
  def synlig = dennaPadda.visible
  def osynlig = dennaPadda.invisible
  def position = dennaPadda.position
  def xPos = dennaPadda.position.x
  def yPos = dennaPadda.position.y
  def vinkel = dennaPadda.heading
  def pennaNer = dennaPadda.penDown
  def pennaUpp = dennaPadda.penUp	
  def färg(c:java.awt.Color) = dennaPadda.setPenColor(c)
  def fyll(c:java.awt.Color) = dennaPadda.setFillColor(c)
  def bredd(n:Double) = dennaPadda.setPenThickness(n)
  def sparaPenna = dennaPadda.saveStyle
  def laddaPenna = dennaPadda.restoreStyle  
}
//blue, red, yellow, green, orange, purple, pink, brown, black, and white
def blå=blue; def röd=red; def gul=yellow; def grön=green; def lila=purple;
def rosa=pink; def brun=brown; def svart=black; def vit=white

def upprepa(n:Int)(op : => Unit){
    for (i <- 1 to n) op
}
def om(villkor : => Boolean)(op : => Unit) {
    if (villkor) op
}    
def omInte(villkor : => Boolean)(op : => Unit){
    if (!villkor) op
}  
def utdata(s:Any){print(s)}
def indata = ctx.readInput("")
def indata(s:String) = ctx.readInput(s)
def räknaTill(n:BigInt) {
    val startTid = System.nanoTime
    var c:BigInt = 1
    print("*** Räknar från 1 till ... ")
    while (c < n) {c = c + 1} //tar tid om n stort
    val stoppTid = System.nanoTime
    println("" + n + " *** KLAR!")
    val tid:BigDecimal = (stoppTid - startTid)/1000000000.toDouble
    print("Det tog ")
    if (tid<0.1) 
        println((1000*tid).round(new java.math.MathContext(2)) + 
                " millisekunder.")
    else println((tid*10).toLong/10.0 + " sekunder.")
}
println("*** Svensk Padda laddad!")

