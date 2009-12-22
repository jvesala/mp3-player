package fi.jvesala.mp3

import java.net.Socket
import java.io.{PrintWriter, InputStreamReader, BufferedReader}

abstract class Player {
  def play

  def play(track: Track)

  def pause

  def stop
}

class DummyPlayer extends Player {
  override def play(track: Track) {
    println("playing track:" + track)
  }

  override def play {
    println("continue paused playing")
  }

  override def pause {
    println("pause.")
  }

  override def stop {
    println("stopping.")
  }
}

class DerMixDPlayer extends Player {
  val channel = 1
  val socket = new Socket("localhost", 8888)
  val reader = new BufferedReader(new InputStreamReader(socket.getInputStream))
  val writer = new PrintWriter(socket.getOutputStream, true)
  // ignore startup message
  reader.readLine

  private def writeCommand(command: String) {
    writer.write(command + "\n")
    writer.flush
  }

  private def checkResult(command: String) = {
    val result = reader.readLine
    if (result.contains("success") || result.contains("DerMixD")) {
      true
    } else {
      println("Error running dermixd command: " + command + ", server error was: " + result)
      false
    }
  }

  override def pause = {
    val command = "pause " + channel
    writeCommand(command)
    checkResult(command)
  }

  override def play(track: Track) = {
    val command = "play " + channel + " " + track.filename
    writeCommand(command)
    checkResult(command)
  }

  override def play = {
    val command = "start " + channel
    writeCommand(command)
    checkResult(command)
  }

  override def stop = {
    val command = "stop " + channel
    writeCommand(command)
    checkResult(command)
  }
}
