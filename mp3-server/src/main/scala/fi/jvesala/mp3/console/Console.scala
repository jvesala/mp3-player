package fi.jvesala.mp3.console

import fi.jvesala.mp3.{SystemTime, DummyPlayer, Database, Server}

import scala.swing._
import scala.swing.event._
import java.awt.{Color, Font, Dimension}
import util.parsing.combinator.RegexParsers

object DummyConsole {
  def main(args: Array[String]): Unit = {
    val server = new Server(new SystemTime, new DummyPlayer, new Database)
    val main = new Console(server)
    main.visible = true
    main.pack
  }
}

object SwingUtils {
  val leftX = 250
  val rightX = 350
  val fullDimension = new Dimension(640, 600)
  val leftXDimension = new Dimension(leftX, 600)
  val rightXDimension = new Dimension(rightX, 600)
  val leftPanelDimension = new Dimension(leftX, 550)
  val rightPanelDimension = new Dimension(rightX, 550)

  val dialogFont = new Font("Arial", Font.PLAIN, 20)
  val dialogColor = Color.WHITE
}

class HelpText extends Label {
  font = SwingUtils.dialogFont
  foreground = SwingUtils.dialogColor
  preferredSize = new Dimension(SwingUtils.leftX, 30)
}

class RightTextLabel extends Label {
  font = SwingUtils.dialogFont
  foreground = SwingUtils.dialogColor
  preferredSize = new Dimension(SwingUtils.rightX, 30)
  minimumSize = new Dimension(SwingUtils.rightX, 30)
}

object NumberParser extends RegexParsers {
  val ident: Parser[String] = """[0-9]\d*""".r

  def isNumber(string: String) = {
    val result = parseAll(ident, string)
    result match {
      case e: Success[_] => true
      case _ => false
    }
  }
}

class Console(val server: Server) extends MainFrame {
  title = "SiMiLi MP3"
  contents = new FlowPanel(FlowPanel.Alignment.Left) {
    background = Color.BLACK
    preferredSize = SwingUtils.fullDimension
    contents += new ControlJPanel(server)
    contents += new QueuePanel(server)
  }
}
//val img = new ImageIcon("mp3_background.jpg").getImage()
//mainPanel = new JPanel() {
//  override def paintComponent(g: Graphics) {
//    g.drawImage(img, 0, 0, null)
//  }
//}

class ControlJPanel(val server: Server) extends BoxPanel(Orientation.Vertical) {
  opaque = false;
  preferredSize = SwingUtils.leftPanelDimension
  contents += new HelpText {text = "[Enter] Lisää jonoon"}
  contents += new HelpText {text = "[+] Soita numero / seur."}
  contents += new HelpText {text = "[-] Tyhjennä jono"}
  contents += new HelpText {text = "[*] Jatka / Lopeta"}
  contents += new HelpText {text = "[/] Shuffle päälle / pois"}
  contents += new HelpText {text = "[Del] Poista numero"}
  contents += new Label {
    preferredSize = new Dimension(SwingUtils.leftX, 40)
    font = SwingUtils.dialogFont
    foreground = SwingUtils.dialogColor
    text = "Kappaleen numero:"
  }
  val input = new TextField {
    font = SwingUtils.dialogFont
    maximumSize = new Dimension(100, 30)
    background = new Color(220, 220, 220)
  }
  contents += input

  listenTo(input)
  reactions += {
    case EditDone(`input`) => handleEnter
    case ValueChanged(`input`) => {
      if (!NumberParser.isNumber(input.text)) {
        handleCommand
      }
    }
  }

  def handleCommand {
    try {
      val command: String = input.text.reverse.take(1)
      command match {
        case "+" => handlePlus
        case "-" => server.clearQueue; setInput("")
        case "*" => handleStar
        case "/" => server.shuffle = !server.shuffle; setInput("")
        case "," => handleComma
        case _ =>
      }
    } catch {
      case e => {
        setInput("")
      }
    }
  }

  def handleEnter {
    try {
      server.enqueue(input.text.toInt)
    } catch {case _ =>}
    setInput("")
  }

  def setInput(text: String) {
    invokeLater({input.text = text})
  }

  def invokeLater[X](exp: => X) {
    import javax.swing.SwingUtilities
    SwingUtilities.invokeLater(new Runnable() {
      def run = exp
    })
  }

  def handlePlus {
    if (input.text.drop(1).isEmpty) {
      server.play
    } else {
      server.play(input.text.reverse.drop(1).reverse.toInt)
    }
    setInput("")
  }

  def handleStar {
    if (server.paused) {
      server.play
    } else {
      server.pause
    }
    setInput("")
  }

  def handleComma {
    if (input.text.length > 0) setInput(input.text.reverse.drop(2).reverse) else setInput(input.text.reverse.drop(1).reverse)
  }
}

class QueuePanel(server: Server) extends BoxPanel(Orientation.Vertical) {
  opaque = false
  preferredSize = SwingUtils.rightPanelDimension
  val queuePrefix = "Jonossa: "
  val shuffle = "Shuffle: "
  val queueLength = new RightTextLabel {text = queuePrefix}
  val shuffleStatus = new RightTextLabel {text = shuffle}

  val currentTable = new Table(4, 2) {
    preferredSize = new Dimension(350, 100)
    font = new Font("Arial", Font.PLAIN, 20)
    background = new Color(220, 220, 220)
    showGrid = false
    rowHeight = 25
    peer.getColumnModel.getColumn(0).setPreferredWidth(75)
    peer.getColumnModel.getColumn(1).setPreferredWidth(265)
  }

  //contents += new RightTextLabel {text = "Nyt soi"}
  contents += currentTable
  contents += queueLength
  contents += shuffleStatus

  StatusUpdater.panel = this
  StatusUpdater.start

  def updateTable {
    def formatTime(orig: Long) = {
      def padding(unit: String) = {if (unit.length == 1) "0" + unit else unit}
      val minutes: Int = orig.toInt / 60
      val seconds: Int = orig.toInt - minutes * 60
      padding(minutes.toString) + ":" + padding(seconds.toString)
    }
    queueLength.text = queuePrefix + server.queueLength
    server.shuffle match {
      case true => shuffleStatus.text = shuffle + "Kyllä"
      case false => shuffleStatus.text = shuffle + "Ei"
    }
    currentTable.update(0, 0, server.track.id.get.toString)
    currentTable.update(1, 1, server.track.artist)
    currentTable.update(2, 1, server.track.title)
    currentTable.update(3, 0, formatTime(server.elapsedTimeInSeconds))
    currentTable.update(3, 1, formatTime(server.track.length))
  }
}

object StatusUpdater extends Thread {
  var keepRunning: Boolean = _
  var panel: QueuePanel = _

  override def run {
    while (true) {
      panel.updateTable
      try {
        Thread.sleep(100)
      } catch {
        case e: InterruptedException => {}
      }
    }
  }
}