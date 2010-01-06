package fi.jvesala.mp3

import org.specs._
import mock.{JMocker, ClassMocker}
import org.specs.runner._

object ServerSpecification extends Specification with JUnit with JMocker with ClassMocker {

  "Server" should {
    "should start playing a track" in {
      val track = new Track(Some(100), "fileName", 60, "cmx", "auraa", "aura", "12/12")
      val time = mock[SystemTime]
      val player = mock[Player]
      val database = mock[Database]
      expect {
        one(database).getById(1) willReturn Some(track)
        one(time).millis willReturn 0
        one(player).play(track)
      }

      val server = new Server(time, player, database)
      server.play(track)
      server.playing must_== true
      server.paused must_== false

      server.stopServer
    }

    "should stop playing a track" in {
      val track = new Track(Some(100), "fileName", 60, "cmx", "auraa", "aura", "12/12")
      val time = mock[SystemTime]
      val player = mock[Player]
      val database = mock[Database]
      expect {
        one(player).stop
        one(database).getById(1) willReturn Some(track)
      }
      val server = new Server(time, player, database)
      server.stop
      server.playing must_== false
      server.paused must_== false

      server.stopServer
    }

  }


}
