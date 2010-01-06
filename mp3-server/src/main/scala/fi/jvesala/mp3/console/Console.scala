package fi.jvesala.mp3.console

import fi.jvesala.mp3.{SystemTime, DummyPlayer, Database, Server}
import java.awt._
import java.awt.event.{ActionEvent, WindowEvent, WindowAdapter}
import javax.swing._
import table.{AbstractTableModel, TableColumn}
import text.{AttributeSet, PlainDocument, Keymap, JTextComponent}

object DummyConsole {
  def main(args: Array[String]): Unit = {
    val server = new Server(new SystemTime, new DummyPlayer, new Database)
    val main = new Console(server)
    main.setVisible(true)
    main.pack();
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

  def getTextLabel(txt: String) = {
    val label = new JLabel
    label.setText(txt)
    label.setFont(SwingUtils.dialogFont)
    label.setForeground(SwingUtils.dialogColor)
    label.setPreferredSize(new Dimension(leftX, 30))
    label
  }

  def getRightTextLabel(txt: String) = {
    val label = getTextLabel(txt)
    label.setPreferredSize(new Dimension(rightX, 30))
    label
  }
}

class Console(val server: Server) extends JFrame {
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  setTitle("SiMiLI MP3");
  setResizable(false);
  setSize(SwingUtils.fullDimension);
  addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      queuePanel.stop
    }
  })

  val controlPanel = new ControlJPanel(server);
  val queuePanel = new QueueJPanel(server);

  val leftPanel = new JPanel
  leftPanel.setLayout(new GridBagLayout())
  leftPanel.setPreferredSize(SwingUtils.leftXDimension)
  leftPanel.setOpaque(false)
  leftPanel.add(controlPanel, new GridBagConstraints())

  val rightPanel = new JPanel
  rightPanel.setLayout(new GridBagLayout())
  rightPanel.setPreferredSize(SwingUtils.rightXDimension)
  rightPanel.setOpaque(false)
  rightPanel.add(queuePanel, new GridBagConstraints())

  val mainPanel = new JPanel
  mainPanel.setBackground(Color.BLACK)
  mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS))
  mainPanel.setPreferredSize(SwingUtils.fullDimension)
  mainPanel.add(leftPanel, null)
  mainPanel.add(rightPanel, null)

  setContentPane(mainPanel);

  //val img = new ImageIcon("mp3_background.jpg").getImage()
  //mainPanel = new JPanel() {
  //  override def paintComponent(g: Graphics) {
  //    g.drawImage(img, 0, 0, null)
  //  }
  //}

  class ControlJPanel(val server: Server) extends JPanel {
    val plusLabel = SwingUtils.getTextLabel("[Enter] Lisää jonoon")
    val enterLabel = SwingUtils.getTextLabel("[+] Soita numero / seur.")
    val minusLabel = SwingUtils.getTextLabel("[-] Tyhjennä jono")
    val starLabel = SwingUtils.getTextLabel("[*] Jatka Lopeta")
    val slashLabel = SwingUtils.getTextLabel("[/] Shuffle päälle / pois")
    val delLabel = SwingUtils.getTextLabel("[Del] Poista numero")
    val numberLabel = new JLabel
    numberLabel.setFont(SwingUtils.dialogFont)
    numberLabel.setForeground(SwingUtils.dialogColor)
    numberLabel.setPreferredSize(new Dimension(SwingUtils.leftX, 100))
    numberLabel.setText("Kappaleen numero:")

    val numberTextField = new NumberJTextField(server)

    setOpaque(false)
    setLayout(new FlowLayout())
    setPreferredSize(SwingUtils.leftPanelDimension)
    add(plusLabel, null)
    add(enterLabel, null)
    add(minusLabel, null)
    add(starLabel, null)
    add(slashLabel, null)
    add(delLabel, null)

    add(numberLabel, null)
    add(numberTextField, null)
  }

  class NumberJTextField(val server: Server) extends JTextField {
    setFont(SwingUtils.dialogFont)
    setPreferredSize(new Dimension(90, 40))
    setBackground(SwingUtils.numberFieldBackground)
    getKeymap().setDefaultAction(new ConsoleAction(findDefaultAction(this)))

    // todo: fix this
    private def findDefaultAction(c: JTextComponent): Action = {
      var kmap = c.getKeymap()
      if (kmap.getDefaultAction != null) {
        return kmap.getDefaultAction
      } else {
        kmap = kmap.getResolveParent
        while (kmap != null) {
          if (kmap.getDefaultAction != null) {
            return kmap.getDefaultAction
          }
          kmap = kmap.getResolveParent
        }
      }
      null
    }

    def handlePlus {
      if (getText.isEmpty) {
        server.play
      } else {
        server.play(getText.toInt)
      }
      setText("")
    }

    def handleEnter {
      if (getText.isEmpty) {
        return
      }
      server.enqueue(getText.toInt)
      setText("")
    }

    def handleStar {
      if (server.paused) {
        server.play
      } else {
        server.pause
      }
    }

    def handleComma {
      if (getText.length > 0) setText(getText.substring(0, getText.length - 1))
    }

    class ConsoleAction(defAction: Action) extends AbstractAction {
      override def actionPerformed(e: ActionEvent) {
        val command = e.getActionCommand()
        if (command != null) {
          command match {
            case "\n" => handleEnter
            case "+" => handlePlus
            case "-" => server.clearQueue
            case "*" => handleStar
            case "/" => server.shuffle = !server.shuffle
            case "," => handleComma
            case _ => defAction.actionPerformed(e)
          }
        }
      }
    }

    override def createDefaultModel = {new UpperCaseDocument}

    class UpperCaseDocument extends PlainDocument {
      override def insertString(offs: Int, str: String, a: AttributeSet) {
        if (offs <= 4) {
          super.insertString(offs, str, a)
        }
      }
    }
  }

  class QueueJPanel(server: Server) extends JPanel {
    val currentLabel = SwingUtils.getRightTextLabel("Nyt soi")
    val queueLength = SwingUtils.getRightTextLabel(SwingUtils.queuePrefix)
    val shuffleStatus = SwingUtils.getRightTextLabel(SwingUtils.shuffle)
    val currentTableModel = new CurrentTableModel
    val currentTable = new JTable(currentTableModel)
    currentTable.setMinimumSize(SwingUtils.currentTableDimension)
    currentTable.setPreferredSize(SwingUtils.currentTableDimension)
    currentTable.setMaximumSize(SwingUtils.currentTableDimension)
    currentTable.setSize(SwingUtils.currentTableDimension)
    setColumnWidth(currentTable.getColumnModel().getColumn(0), 60)
    setColumnWidth(currentTable.getColumnModel().getColumn(1), 280)
    currentTable.setFont(SwingUtils.currentTableFont)
    currentTable.setBackground(SwingUtils.currentTableBackground)
    currentTable.setShowGrid(false)
    currentTable.setRowHeight(SwingUtils.currentTableRowHeight)
    setPreferredSize(SwingUtils.rightPanelDimension)
    setOpaque(false)
    setLayout(new FlowLayout)

    add(currentLabel, null)
    add(currentTable, null)
    add(queueLength, null)
    add(shuffleStatus, null)

    val updater = new StatusUpdaterThread(this)
    updater.start

    def fireStatusChanged {
      queueLength.setText(SwingUtils.queuePrefix + server.queueLength)
      server.shuffle match {
        case true => shuffleStatus.setText(SwingUtils.shuffle + "Kyllä")
        case false => shuffleStatus.setText(SwingUtils.shuffle + "Ei")
      }
      if (!server.paused) currentTableModel.fireTableDataChanged()
    }

    def stop {updater.keepRunning = false}

    def setColumnWidth(col: TableColumn, width: Int) {
      col.setWidth(width)
      col.setPreferredWidth(width)
      col.setMinWidth(width)
      col.setMaxWidth(width)
    }

    class CurrentTableModel extends AbstractTableModel {
      override def getColumnCount = {2}

      override def getRowCount = {4}

      def getValueAt(row: Int, col: Int) = {
        var value = ""
        if (server.track != null || server.playing) {
          row match {
            case 0 => if (col == 0) value = server.track.id.get.toString else value = ""
            case 1 => if (col == 1) value = server.track.artist else ""
            case 2 => if (col == 1) value = server.track.title else ""
            case 3 => if (col == 0) value = formatTime(server.elapsedTimeInSeconds) else value = formatTime(server.track.length)
          }
        }
        value
      }

      private def formatTime(orig: Long) = {
        val minutes: Int = orig.toInt / 60
        val seconds: Int = orig.toInt - minutes * 60
        padding(minutes.toString) + ":" + padding(seconds.toString)
      }

      private def padding(unit: String) = {if (unit.length == 1) "0" + unit else unit}
    }

    class StatusUpdaterThread(val queueJPanel: QueueJPanel) extends Thread {
      val INTERVAL = 100
      var keepRunning: Boolean = _
      keepRunning = true

      override def run {
        while (keepRunning) {
          queueJPanel.fireStatusChanged
          try {
            Thread.sleep(INTERVAL)
          } catch {
            case e: InterruptedException => {}
          }
        }
      }
    }
  }
}