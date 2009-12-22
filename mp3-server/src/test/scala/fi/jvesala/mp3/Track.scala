package fi.jvesala.mp3

import org.specs._
import org.specs.runner._

object TrackSpecification extends Specification with JUnit {

  "Track" should {
    "calculate correct length in millis" in {
      val track = new Track(Some(100), "fileName", 60, "cmx", "auraa", "aura", "12/12")
      track.lengthInMillis must_== 60000
    }
  }
}
  