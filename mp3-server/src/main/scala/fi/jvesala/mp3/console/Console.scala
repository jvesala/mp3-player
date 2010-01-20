package fi.jvesala.mp3.console

import fi.jvesala.mp3.{SystemTime, DummyPlayer, Database, Server}

import scala.swing._
import scala.swing.event._
import java.awt.{Color, Font, Dimension}
import javax.swing.text.{AttributeSet, PlainDocument}
import javax.swing.table.{TableColumn, AbstractTableModel}
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
  val currentTableDimension = new Dimension(rightX, 100)
  val currentTableFont = new Font("Arial", Font.PLAIN, 20)
  val currentTableRowHeight = 25
  val currentTableBackground = Color.WHITE;

  val dialogFont = new Font("Arial", Font.PLAIN, 20)
  val dialogColor = Color.WHITE
  val numberFieldBackground = new Color(220, 220, 220)

  val queuePrefix = "Jonossa: "
  val shuffle = "Shuffle: "
}

class HelpText extends Label {
  font = SwingUtils.dialogFont
  foreground = SwingUtils.dialogColor
  preferredSize = new Dimension(SwingUtils.leftX, 30)
}

class RightTextLabel extends HelpText {
  preferredSize = new Dimension(SwingUtils.rightX, 30)
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

class Console(val server: Server) extends Frame {
  title = "SiMiLI MP3"
  size = SwingUtils.fullDimension

  val controlPanel = new ControlJPanel(server)
  val queuePanel = new QueuePanel(server)

  contents = new BoxPanel(Orientation.Horizontal) {
    background = Color.BLACK
    preferredSize = SwingUtils.fullDimension
    contents += new BoxPanel(Orientation.Horizontal) {
      preferredSize = SwingUtils.leftXDimension
      opaque = false
      contents += controlPanel
    }
    contents += new BoxPanel(Orientation.Horizontal) {
      preferredSize = SwingUtils.rightXDimension
      opaque = false
      contents += queuePanel
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
    //size = SwingUtils.leftPanelDimension
    contents += new HelpText {text = "[Enter] Lisää jonoon"}
    contents += new HelpText {text = "[+] Soita numero / seur."}
    contents += new HelpText {text = "[-] Tyhjennä jono"}
    contents += new HelpText {text = "[*] Jatka Lopeta"}
    contents += new HelpText {text = "[/] Shuffle päälle / pois"}
    contents += new HelpText {text = "[Del] Poista numero"}
    contents += new Label {
      font = SwingUtils.dialogFont
      foreground = SwingUtils.dialogColor
      preferredSize = new Dimension(SwingUtils.leftX, 100)
      text = "Kappaleen numero:"
    }
    val input = new TextField {
      font = SwingUtils.dialogFont
      maximumSize = new Dimension(90, 40)
      background = SwingUtils.numberFieldBackground

      //override def createDefaultModel = {new UpperCaseDocument}

      class UpperCaseDocument extends PlainDocument {
        override def insertString(offs: Int, str: String, a: AttributeSet) {
          if (offs <= 4) {
            super.insertString(offs, str, a)
          }
        }
      }
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
}
class QueuePanel(server: Server) extends BoxPanel(Orientation.Vertical) {
  //preferredSize = SwingUtils.rightPanelDimension
  opaque = false
  val currentLabel = new RightTextLabel {text = "Nyt soi"}
  val queueLength = new RightTextLabel {text = SwingUtils.queuePrefix}
  val shuffleStatus = new RightTextLabel {text = SwingUtils.shuffle}

  val currentTable = new Table(4, 2) {
    minimumSize = SwingUtils.currentTableDimension
    preferredSize = SwingUtils.currentTableDimension
    maximumSize = SwingUtils.currentTableDimension
    size = SwingUtils.currentTableDimension
    font = SwingUtils.currentTableFont
    background = SwingUtils.currentTableBackground
    showGrid = false
    rowHeight = SwingUtils.currentTableRowHeight
  }

  def updateTable {
    queueLength.text = SwingUtils.queuePrefix + server.queueLength
    server.shuffle match {
      case true => shuffleStatus.text = SwingUtils.shuffle + "Kyllä"
      case false => shuffleStatus.text = SwingUtils.shuffle + "Ei"
    }
    currentTable.update(0, 0, server.track.id.get.toString)
    currentTable.update(1, 1, server.track.artist)
    currentTable.update(2, 1, server.track.title)
    currentTable.update(3, 0, formatTime(server.elapsedTimeInSeconds))
    currentTable.update(3, 1, formatTime(server.track.length))
  }

  private def formatTime(orig: Long) = {
    def padding(unit: String) = {if (unit.length == 1) "0" + unit else unit}
    val minutes: Int = orig.toInt / 60
    val seconds: Int = orig.toInt - minutes * 60
    padding(minutes.toString) + ":" + padding(seconds.toString)
  }

  //setColumnWidth(currentTable.getColumnModel().getColumn(0), 75)
  //setColumnWidth(currentTable.getColumnModel().getColumn(1), 265)

  def setColumnWidth(col: TableColumn, width: Int) {
    col.setWidth(width)
    col.setPreferredWidth(width)
    col.setMinWidth(width)
    col.setMaxWidth(width)
  }

  contents += currentLabel
  contents += currentTable
  contents += queueLength
  contents += shuffleStatus

  StatusUpdater.panel = this
  StatusUpdater.start
}

object StatusUpdater extends Thread {
  var keepRunning: Boolean = _
  var panel: QueuePanel = _
  keepRunning = true

  override def run {
    while (keepRunning) {
      panel.updateTable
      try {
        Thread.sleep(100)
      } catch {
        case e: InterruptedException => {}
      }
    }
  }
}