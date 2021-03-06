package fi.jvesala.mp3

import org.specs._
import org.specs.runner._
import java.io.File

object FileReaderSpecification extends Specification with JUnit {
  "FileReader" should {
    "create track from file" in {
      val reader = new FileReader
      val fullPath = "/opt/mp3/mp3-player/mp3-server/src/test/resources/scottaltham_-_Black_Is_The_Night.mp3"
      //val fullPath = "C:\\idea-workspace\\mp3\\src\\test\\resources\\scottaltham_-_Black_Is_The_Night.mp3"
      val file = new File(fullPath)
      val track = reader.createTrack(file)
      val expected = new Track(None, fullPath, 320, "scottaltham", "ccMixter", "Black Is The Night", "0")
      track.get mustEqual expected
    }

    "parse filename correctly from track" in {
      val reader = new FileReader
      val filename = "test-mp3-song.mp3"
      val expected = "test-mp3-song"
      reader.parseTitleFromFilename(filename) mustEqual expected
    }

    "parse filename correctly from track with path" in {
      val reader = new FileReader
      val filename = "/path/to/track/test-mp3-song-with-path.mp3"
      val expected = "test-mp3-song-with-path"
      reader.parseTitleFromFilename(filename) mustEqual expected
    }

  }
}
