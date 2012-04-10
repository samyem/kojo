//Contributed by Bjorn Regnell 
//Swedish Turtle wrapper for Kojo
class Padda {
  var steglängd = 20.0
  def gå(steg:Double) = thisTurtle.forward(steg)
  def fram() = gå(steglängd)
  def bak() = gå(-steglängd)
  def vänster() = thisTurtle.left()
  def höger() = thisTurtle.right()
  def vrid(vinkel:Double) = thisTurtle.turn(vinkel)
  def vridHöger(vinkel:Double) = thisTurtle.right(vinkel)
  def vridVänster(vinkel:Double) = thisTurtle.left(vinkel) //same as turn
  def hoppaTill(x:Double, y:Double) = thisTurtle.jumpTo(x, y)
  def flyttaTill(x:Double, y:Double) = thisTurtle.moveTo(x, y)
  def hoppa(steg:Double) = saveStateAndDo { gå(steg) }
  def hopp() = saveStateAndDo { fram() }
  def bakåtHopp() = saveStateAndDo { bak() }
  def hem() = thisTurtle.home()
  def mot(x:Double, y:Double) = thisTurtle.towards(x, y)
  def vinkla(vinkel:Double) = thisTurtle.setHeading(vinkel)
  def vinkel = thisTurtle.heading
  def öst() = thisTurtle.setHeading(0)
  def väst() = thisTurtle.setHeading(180)
  def norr() = thisTurtle.setHeading(90)
  def söder() = thisTurtle.setHeading(-90)  
  def dröj(n: Long) = thisTurtle.setAnimationDelay(n)
  def skriv(t : Any) = thisTurtle.write(t)
  def textstorlek(s:Int) = thisTurtle.setPenFontSize(30)
  def båge(radie:Double, vinkel:Double) = thisTurtle.arc(radie, math.round(vinkel).toInt)
  def cirkel(radie:Double) = thisTurtle.circle(radie)
  def synlig() = thisTurtle.visible()
  def osynlig() = thisTurtle.invisible()
  def position = thisTurtle.position
  def pennaNer() = {penIsDown = true ; thisTurtle.penDown()}
  def pennaUpp() = {penIsDown = false ; thisTurtle.penUp()}	
  def ärPennanNere = penIsDown
  def färg(c:java.awt.Color) = thisTurtle.setPenColor(c)
  def fyll(c:java.awt.Color) = thisTurtle.setFillColor(c)
  def bredd(n:Double) = thisTurtle.setPenThickness(n)
  def sparaStil() = thisTurtle.saveStyle()
  def laddaStil() = thisTurtle.restoreStyle()
  def sparaPositionRiktning() = thisTurtle.savePosHe()
  def laddaPositionRiktning() = thisTurtle.restorePosHe()
  def siktePå() = thisTurtle.beamsOn()
  def sikteAv() = thisTurtle.beamsOff()
  def engelska = thisTurtle
  protected var thisTurtle = newTurtle
  private var penIsDown = true
  private def saveStateAndDo(doThis: => Unit) {
    val wasDown = penIsDown
    pennaUpp
    doThis
    if (wasDown) pennaNer
  }
}
object padda extends Padda { 
  //make thisTurtle the same as turtle0 for padda
  thisTurtle.invisible; thisTurtle = turtle0
  //create more swedish turtles using "new Padda"
} 
import padda._  
def sudda() = clear()
def suddaUtdata() = clearOutput()
def blå=blue; def röd=red; def gul=yellow; def grön=green; def lila=purple;
def rosa=pink; def brun=brown; def svart=black; def vit=white; 
def genomskinlig=Color(0, 0, 0, 0)
def bakgrund(färg:Color) = setBackground(färg)
def bakgrund2(färg1:Color, färg2:Color) = setBackgroundV(färg1, färg2)
//loops in Swedish
def upprepa(n:Int)(block : => Unit){
    for (i <- 1 to n) block
}
def räkneslinga(n:Int)(block : Int => Unit){
    for (i <- 1 to n) block(i)
}
def sålänge(villkor : => Boolean)(block : => Unit){
  while (villkor) block
}
//simple IO
def utdata(data:Any) = println(data)
def indata(ledtext:String="") = ctx.readInput(ledtext)
//math functions
def avrunda(tal:Number, antalDecimaler:Int=2):Double = {
  val faktor = math.pow(10, antalDecimaler).toDouble
  math.round(tal.doubleValue * faktor).toLong / faktor
}
//speedTest
def systemTid = BigDecimal(System.nanoTime) / BigDecimal("1000000000") //sekunder
def räknaTill(n:BigInt) { 
    var c:BigInt = 1
    print("*** Räknar från 1 till ... ")
    val startTid = systemTid
    while (c < n) {c = c + 1} //tar tid om n är stort
    val stoppTid = systemTid
    println("" + n + " *** KLAR!")
    val tid = stoppTid - startTid
    print("Det tog ")
    if (tid<0.1) 
        println((tid*1000).round(new java.math.MathContext(2)) + 
                " millisekunder.")
    else println((tid*10).toLong/10.0 + " sekunder.")
}
//code completion
addCodeTemplates(
    "sv", 
    Map(
        "steglängd" -> "steglängd = ${100}",
        "gå" -> "gå(${steg})",
        "bak" -> "bak()",
        "fram" -> "fram()",
        "vänster" -> "vänster()",
        "höger" -> "höger()",
        "vrid" -> "vrid(${vinkel})",
        "vridHöger" -> "vridHöger(${vinkel})",
        "vridVänster" -> "vridVänster(${vinkel})",
        "hoppaTill" -> "hoppaTill(${x},${y})",
        "flyttaTill" -> "flyttaTill(${x},${y})",
        "hoppa" -> "hoppa(${steg})",
        "hopp" -> "hopp()",
        "bakåtHopp" -> "bakåtHopp()",
        "hem" -> "hem()",
        "mot" -> "mot(${x},${y})",
        "vinkla" -> "vinkla(${vinkel})",
        "öst" -> "öst()",
        "väst" -> "väst()",
        "norr" -> "norr()",
        "söder" -> "söder()",
        "dröj" -> "dröj(${fördröjning})",
        "skriv" -> "skriv(${textsträng})",
        "textstorlek" -> "textstorlek(${storlek})",
        "båge" -> "båge(${radie},${vinkel})",
        "cirkel" -> "cirkel(${radie})",
        "synlig" -> "synlig()",
        "osynlig" -> "osynlig()",
        "pennaNer" -> "pennaNer()",
        "pennaUpp" -> "pennaUpp()",
        "färg" -> "färg(${pennfärg})",
        "fyll" -> "fyll(${fyllfärg})",
        "bredd" -> "bredd(${pennbredd})",
        "sparaStil" -> "sparaStil()",
        "laddaStil" -> "laddaStil()",
        "sparaPositionRiktning" -> "sparaPositionRiktning()",
        "laddaPositionRiktning" -> "laddaPositionRiktning()",
        "siktePå" -> "siktePå()",
        "sikteAv" -> "sikteAv()",
        "sudda" -> "sudda()",
        "suddaUtdata" -> "suddaUtdata()",
        "bakgrund" -> "bakgrund(${färg})",
        "bakgrund2" -> "bakgrund2(${färg1},${färg2})",
        "upprepa" -> "upprepa (${antal}) {\n    \n}",
        "räkneslinga" -> "räkneslinga (${antal}) { i => \n    \n}",
        "sålänge" -> "sålänge (${villkor}) {\n    \n}",
        "utdata" -> "utdata(${textsträng})",
        "indata" -> "indata(${ledtext})",
        "avrudna" -> "avrudna(${tal},${antalDecimaler})",
        "räknaTill" -> "räknaTill(${tal})" 
  )
)
//help texts
addHelpContent(
    "sv", 
    Map(
      "fram" -> <div><strong>fram</strong>()<br/>Paddan går framåt i riktningen dit nosen pekar.<br/>Om pennan är nere så ritar paddan när den går.</div>.toString,
      "gå" -> 
      <div>
        <strong>gå</strong>(steg) - Paddan går frammåt det antal steg du anger i riktningen dit nosen pekar. <br/>Om pennan är nere så ritar paddan när den går.
        <br/><em>Exempel:</em> <br/><br/>
        <pre>
sudda     
gå(100)  //paddan går 100 steg och ritar
pennaUpp //paddan lyfter pennan från pappret
gå(200)  //paddan går 200 steg utan att rita
        </pre>
      </div>.toString,
      "bak" -> <div><strong>bak</strong>()<br/>Paddan går bakåt i riktningen dit rumpan pekar.<br/>Om pennan är nere så ritar paddan när den går.</div>.toString,
      "steglängd" -> <div><strong>steglängd = 100</strong><br/>Ändrar hur långt paddan går när den gör fram eller bak till 100.<br/>Från början är steglängd = 20.</div>.toString,
      "vänster" -> <div><strong>vänster</strong>()<br/>Paddan vrider sig ett kvarts varv åt vänster.</div>.toString,
      "höger" -> <div><strong>höger</strong>()<br/>Paddan vrider sig ett kvarts varv åt höger.</div>.toString,
      "vrid" -> <div><strong>vrid</strong>(vinkel)<br/>Paddan vrider sig motsols (moturs, åt vänster) så många grader som vinkeln anger.</div>.toString,
      "vridHöger" -> <div><strong>vridVänster</strong>(vinkel)<br/>Paddan vrider sig medsols (medurs, åt högre) så många grader som vinkeln anger.</div>.toString,
      "vridVänster" -> <div><strong>vridVänster</strong>(vinkel)<br/>Paddan vrider sig motsols (moturs, åt vänster) så många grader som vinkeln anger.</div>.toString,
      "hoppaTill" -> <div><strong>hoppaTill</strong>(x, y)<br/>Paddan hoppar till postionen med koordinaterna (x,y) utan att rita och utan att ändra riktning.</div>.toString,
      "flyttaTill" -> <div><strong>flyttaTill</strong>(x, y)<br/>Paddan vrider sig mot postionen med koordinaterna (x,y) och hoppar dit.<br/>Om pennan är nere så ritar paddan när den går.</div>.toString,
      "hoppa" -> <div><strong>hoppa</strong>(steg)<br/>Paddan hoppar i riktningen dit nosen pekar det antal steg som anges utan att rita.</div>.toString,
      "hopp" -> <div><strong>hopp</strong>()<br/>Paddan tar ett skutt utan att rita i riktningen dit nosen pekar.</div>.toString,
      "bakåtHopp" -> <div><strong>bakåtHopp</strong>()<br/>Paddan tar ett bakåtskutt dit rumpan pekar utan att rita.</div>.toString,
      "hem" ->  <div><strong>hem</strong>()<br/>Paddan går tillbaka till platsen som kallas origo med koordinaterna (0,0) och vrider sig så att nosen pekar uppåt.<br/>Om pennan är nere så ritar paddan när den går.</div>.toString,
      "mot" -> <div><strong>mot</strong>(x, y)<br/>Paddan vrider sig så att nosen pekar mot postionen med koordinaterna (x,y)</div>.toString,
      "vinkla" -> <div><strong>vinkla</strong>(vinkel)<br/>Paddan vrider sig så att nosen pekar dit vinkeln anger.</div>.toString,
      "vinkel" -> <div><strong>vinkel</strong>(vinkel)<br/>Ger värdet på vinkeln dit paddans nos pekar.</div>.toString,
      "öst" -> <div><strong>öst</strong>()<br/>Paddan vrider sig så att nosen pekar mot öst (höger).</div>.toString,
      "väst" -> <div><strong>väst</strong>()<br/>Paddan vrider sig så att nosen pekar mot väst (vänster).</div>.toString,
      "norr" -> <div><strong>norr</strong>()<br/>Paddan vrider sig så att nosen pekar mot norr (upp).</div>.toString,
      "söder" -> <div><strong>söder</strong>()<br/>Paddan vrider sig så att nosen pekar mot söder (ner).</div>.toString,
      "dröj" -> <div><strong>dröj</strong>(fördröjning)<br/>Ju mer fördröjning desto långsammare padda.<br/>Minsta fördröjning är 0<br/>dröj(1000) är ganska långsamt.</div>.toString,
      "skriv" -> <div><strong>skriv</strong>(textsträng)<br/>Paddan skriver texten i textsträng i ritfönstret<br/>En textsträng måste ha dubbelfnuttar i början och slutet. Exempel: skriv("hej")</div>.toString,
      "textstorlek" -> <div><strong>textstorlek</strong>(storlek)<br/>Ändrar storleken på texten som paddan skriver.</div>.toString,
      "båge" -> <div><strong>båge</strong>(radie, vinkel)<br/>Paddan ritar ett cirkelsegment med angiven radie och vinkel.</div>.toString,
      "cirkel" -> <div><strong>cirkel</strong>(radie)<br/>Paddan ritar en cirkel med angiven radie.</div>.toString,
      "synlig" -> <div><strong>synlig</strong>()<br/> Gör så att paddan syns igen om den är osynlig.</div>.toString,
      "osynlig" -> <div><strong>osynlig</strong>()<br/>Gör paddan osynlig.</div>.toString,
//      "position" -> <div><strong>position</strong><br/>Tar reda på paddans position.</div>.toString, //there is already an english version of position (same word in both languages)
      "pennaNer" -> <div><strong>pennaNer</strong>()<br/>Sätter ner paddans penna så att den ritar när paddan går.</div>.toString,
      "pennaUpp" -> <div><strong>pennaUpp</strong>()<br/>Lyfter upp paddans penna så att den inte ritar när paddan går.</div>.toString,
      "ärPennanNere" -> <div><strong>ärPennanNere</strong><br/>Kollar om paddans penna är nere. Ger true om pennan är nere och false om pennan är uppe.</div>.toString,
      "färg" -> <div><strong>färg</strong>(pennfärg)<br/>Gör så att paddans penna ritar med angiven pennfärg.<br/>Du kan anväda dessa färdigblandade färger:<br/>blå, röd, gul, grön, lila, rosa, brun, svart, vit, genomskinlig.<br/>Du kan blanda egna färger med Color </div>.toString,
      "fyll" -> <div><strong>fyll</strong>(fyllfärg)<br/>Gör så att paddan fyller i med angiven fyllfärg när den ritar.<br/>Du kan anväda dessa färdigblandade färger:<br/>blå, röd, gul, grön, lila, rosa, brun, svart, vit, genomskinlig.<br/>Du kan blanda egna färger med Color </div>.toString,
      "bredd" -> <div><strong>bredd</strong>(pennbredd)<br/>Ändrar pennbredden på paddans penna. Ju högre pennbredd desto tjockare streck.</div>.toString,
      "sparaStil" -> <div><strong>sparaStil</strong>()<br/>Sparar undan pennans färg, fyllfärg, bredd och textstorlek.<br/>Du kan få tillbaka den sparade stilen med laddaStil</div>.toString,
      "laddaStil" -> <div><strong>laddaStil</strong>()<br/>Hämtar sparad pennstil och sätter tillbaka pennans färg, fyllfärg, bredd och textstorlek.<br/>Du spara en pennstil med sparaStil</div>.toString,
      "sparaPositionRiktning" -> <div><strong>sparaPositionRiktning</strong>()<br/>Sparar undan pennans position och riktning.<br/>Du kan få tillbaka den sparade pennans position och riktning med laddaPositionRiktning</div>.toString,
      "laddaPositionRiktning" -> <div><strong>laddaPositionRiktning</strong>()<br/>Hämtar sparad position och riktning.<br/>Du spara en pennstil med sparaPositionRiktning</div>.toString,
      "siktePå" -> <div><strong>siktePå</strong>()<br/>Visar vilket håll paddan siktar mot med ett hårkors-sikte.</div>.toString,
      "sikteAv" -> <div><strong>sikteAv</strong>()<br/>Gömmer paddans hårkors-sikte.</div>.toString,
      "engelska" -> <div><strong>engelska</strong><br/>Ger den engelska paddan.<br/>Om du skriver:<br/>padda.eneglska.<br/>kan du se allt som en padda kan göra på engelska.</div>.toString,
      "sudda" -> <div><strong>sudda</strong>()<br/>Suddar allt som ritats i ritfönstret.</div>.toString,
      "suddaUtdata" -> <div><strong>suddaUtdata</strong>()<br/>Suddar allt som skrivits i utdatafönstret.</div>.toString,
      "bakgrund" -> <div><strong>bakgrund</strong>(bakgrundsfärg)<br/>Gör så att bakgrundsfärgen ändras.<br/>Du kan anväda dessa färdigblandade färger:<br/>blå, röd, gul, grön, lila, rosa, brun, svart, vit, genomskinlig.<br/>Du kan blanda egna färger med Color </div>.toString,
      "bakgrund2" -> <div><strong>bakgrund</strong>(färg1,färg2)<br/>Gör så att bakgrundsfärgen blir en övergång från färg1 till färg2.<br/>Du kan anväda dessa färdigblandade färger:<br/>blå, röd, gul, grön, lila, rosa, brun, svart, vit, genomskinlig.<br/>Du kan blanda egna färger med Color </div>.toString,
      "upprepa" -> <div><strong>upprepa</strong>(antal) {{ satser }} - upprepar <em>satser</em> det antal gånger som anges.
        <br/><em>Exempel:</em> <br/><br/>
        <pre>
upprepa(4) {{ 
      fram
      vänster
}}
        </pre>
      </div>.toString,
      "räkneslinga" -> <div><strong>räkneslinga</strong>(antal) {{ i => satser }} - upprepar <em>satser</em> det antal gånger som anges och räknar varje runda i slingan. Räknarens värde finns i värdet <strong>i</strong>
        <br/><em>Exempel:</em> <br/><br/>
        <pre>
räkneslinga(10) {{ i =>
      utdata(i)
}}
        </pre>
      </div>.toString,
      "sålänge" -> <div><strong>sålänge</strong>(villkor) {{  satser }} - upprepar <em>satser</em> så länge <em>villkor</em> är sant. 
        <br/><em>Exempel:</em> <br/><br/>
        <pre>var i = 0
sålänge(i{"<"}10) {{ 
      utdata(i)
      i = i + 1
}}
        </pre>
      </div>.toString,
      "utdata" -> <div><strong>utdata</strong>(textsträng)<br/>Skriver texten i <em>textsträng</em> i utdatafönstret<br/>En textsträng måste ha dubbelfnuttar i början och slutet. Exempel: utdata("hej")</div>.toString,
      "indata" -> <div><strong>indata</strong>(ledtext)<br/>Skriver ut ledtext i utdatafönstret och väntar på inmatning av text tills du trycker Enter.<br/>
        <br/><em>Exempel:</em> <br/><br/>
        <pre>val x = indata("Skriv ditt namn ")
utdata("Hej " + x + "!")
        </pre>
      </div>.toString,
      "avrunda" -> <div><strong>avrunda</strong>(decimaltal, antalDecimaler)<br/>Avrundar decimaltal till angivet antal decimaler<br/>
        <br/><em>Exempel:</em> <br/><br/>
        <pre>val t = avrunda(3.999,1)
utdata(t)
        </pre>
      </div>.toString,
      "systemTid" -> <div><strong>systemTid</strong><br/>Ger systemklockans tid i sekunder. Du kan använda systemTid för att mäta hur lång tid något tar.<br/>
        <br/><em>Exempel:</em> <br/><br/>
        <pre>dröj(1000)
val start = systemTid
utdata("Ha tålamod!")
gå(1000)
val stopp = systemTid
val s = stopp - start
utdata("Du hade tålamod i " + avrunda(s,1) + " sekunder.")
        </pre>
      </div>.toString,
      "räknaTill" -> <div><strong>räknaTill</strong>(tal)<br/>Kollar hur lång tid det tar för datorn att räkna till ett visst tal. DU kan prova med ganska stora tal<br/>
        <br/><em>Exempel:</em> <br/><br/>
        <pre>
räknaTill(5000)
        </pre>
      </div>.toString 
    )
)

utdata("*** Svensk Padda laddad!")
