import fi.jvesala.mp3._
import java.io.File

object TrackInsertTest {
  def main(args: Array[String]) {
    val database = new Database
    val track = new Track(None, "c:\\fullPath\\Black Is The Night.mp3", 320, "scottaltham", "ccMixter", "Black Is The Night", "0")
    database.updateTracks(List(track))
  }
}

object InsertAllTracksFromDirectoryStructure {
  def main(args: Array[String]) {
    val inserter = new Inserter
    //val fullPath = "E:\\mp3-backup"
    //var fullPath = "C:\\Users\\Jussi\\Music"
    val fullPath = "/home/jvesala/Music/Muse/Black_holes_and_revelations"
    inserter.insertTracksFromDirectory(new File(fullPath))
  }
}

object QueryTrackById {
  def main(args: Array[String]) {
    val database = new Database
    println(database.getById(25000))
  }
}


object GetRandomTrack {
  def main(args: Array[String]) {
    val database = new Database
    println("got random: " + database.getRandomTrack)
  }
}


object GetTrackByText {
  def main(args: Array[String]) {
    val database = new Database
    val text = "a"
    val result = database.getByText(text)
    println("got text search result for query(" + text + "), size:" + result.length +  "\n: " + result)
  }
}

object GetAllTracks {
  def main(args: Array[String]) {
    val database = new Database
    println(database.getAllTracks)
  }
}

