package fi.jvesala.mp3

import console.Console

class Server(val systemTime: SystemTime, val player: Player, val database: Database) {
  private val playThread = new PlayThread
  playThread.start

  def stopServer {
    playThread.keepRunning = false
  }
  var track: Track = database.getById(1).get
  private var startTimeMillis: Long = _
  private var pauseTimeMillis: Long = _
  var paused: Boolean = _
  var playing: Boolean = _
  var shuffle: Boolean = _
  var repeat: Boolean = _
  var queue: List[Track] = Nil

  def play(id: Int)  {
    database.getById(id) match {
      case Some(track: Track) => play(track)
      case _ => None
    }
  }
  
  def play(track: Track) {
    this.track = track
    player.play(track)
    startTimeMillis = systemTime.millis
    pauseTimeMillis = 0
    playing = true
    paused = false
  }

  def play {
    if (paused) {
      continuePlaying
      return
    }
    if (repeat) {
      play(track)
      return
    }
    if (queue.length > 0) {
      playNextFromQueue
    } else {
      playNextNoQueue
    }
  }

  private def continuePlaying {
    player.play
    startTimeMillis = systemTime.millis - pauseTimeMillis
    playing = true
    paused = false
  }

  private def playNextFromQueue {
    val next = queue.head
    queue = queue.drop(1)
    play(next)
  }

  private def playNextNoQueue {
    if (shuffle) {
      play(database.getRandomTrack)
    } else {
      play(database.getNextTrack(track))
    }
  }

  def pause {
    player.pause
    paused = true
    if (playing) {
      pauseTimeMillis = systemTime.millis - startTimeMillis
      playing = false
    }
  }

  def stop {
    player.stop
    pauseTimeMillis = 0
    playing = false
    paused = false
  }

  def enqueue(id: Int)  {
    database.getById(id) match {
      case Some(track: Track) => enqueue(track)
      case _ => None
    }
  }
  
  def enqueue(track: Track) {
    queue = queue ::: List(track)
  }

  def clearQueue {
    queue = Nil
  }
  
  def queueLength = {
    queue.size
  }

  def elapsedTimeInSeconds = {
    (systemTime.millis - startTimeMillis) / 1000
  }

  private def isTrackEnded = {
    systemTime.millis >= startTimeMillis + track.lengthInMillis
  }

  class PlayThread extends Thread {
    val INTERVAL: Long = 100
    var keepRunning: Boolean = _
    keepRunning = true

    override def run() {
      while (keepRunning) {
        if (playing && isTrackEnded) {
          play
        }
        try {
          Thread.sleep(INTERVAL);
        } catch {
          case e: InterruptedException => {}
        }
      }
    }
  }
}

class SystemTime {
  def millis = {
    System.currentTimeMillis
  }
}

object ServerRunner {
  def main(args: Array[String]): Unit = {
    val server = new Server(new SystemTime, new DerMixDPlayer, new Database)
    val main = new Console(server)
    main.setVisible(true)
    main.pack();
  }
}