package fi.jvesala.mp3

import com.novocode.squery.combinator._
import com.novocode.squery.combinator.Implicit._
import com.novocode.squery.session._
import com.novocode.squery.session.SessionFactory._
import com.novocode.squery.session.TypeMapper._
import com.novocode.squery.simple._
import com.novocode.squery.simple.StaticQueryBase._
import java.sql.PreparedStatement

object DatabaseSettings {
  val dbUrl = "jdbc:mysql://127.0.0.1:3306/mp3?user=mp3&password=mp3"
  val dbDriver = "com.mysql.jdbc.Driver"
}

//grant all on mp3.* to 'mp3'@'%' identified by 'mp3';
//grant all on mp3.* to 'mp3'@'localhost' identified by 'mp3';
//create database mp3;

object InitDatabase {
  def main(args: Array[String]) {
    val sf = new DriverManagerSessionFactory(DatabaseSettings.dbUrl, DatabaseSettings.dbDriver)
    val dropTable = updateNA("DROP TABLE IF EXISTS tracks")
    val createTable = updateNA("CREATE TABLE tracks (id INTEGER NOT NULL AUTO_INCREMENT,filename VARCHAR(1024) NOT NULL,length INTEGER NOT NULL,artist VARCHAR(1024) NOT NULL,album VARCHAR(1024) NOT NULL,title VARCHAR(1024) NOT NULL,trackNumber VARCHAR(1024) NOT NULL, PRIMARY KEY(ID))")
    sf withSession {
      getThreadSession.withTransaction {
        println("Dropping existing table" + dropTable())
        println("Creating user table: " + createTable())
      }
    }
  }
}

object TracksTable extends Table[(Option[Int], String, Int, String, String, String, String)]("tracks") {
  def id = column[Option[Int]]("id", O.AutoInc, O.NotNull)

  def filename = column[String]("filename", O.NotNull)

  def length = column[Int]("length", O.NotNull)

  def artist = column[String]("artist", O.NotNull)

  def album = column[String]("album", O.NotNull)

  def title = column[String]("title", O.NotNull)

  def trackNumber = column[String]("trackNumber", O.NotNull)

  def * = id ~ filename ~ length ~ artist ~ album ~ title ~ trackNumber

  implicit def track2fields(x: Track): (Option[Int], String, Int, String, String, String, String) =
    (x.id, x.filename, x.length, x.artist, x.album, x.title, x.trackNumber)

  //implicit def fieldsToTrack(id: Int, filename: String, length: Int, artist: String, album: String, title: String, trackNumber: String): Track =
  //    new Track(Some(id), filename, length, artist, album, title, trackNumber)
}

class Database {
  private def session = {
    new DriverManagerSessionFactory(DatabaseSettings.dbUrl, DatabaseSettings.dbDriver)
  }

  def updateTracks(tracks: List[Track]) {
    session withSession {
      getThreadSession.withTransaction {
        import TracksTable._
        for (track <- tracks) {
          val existingTracks = findTrackByFilename(track.filename)
          if (existingTracks.length > 0) {
            deleteById(existingTracks.first.id.get)
            val combinedTrack = new Track(existingTracks.first.id, track.filename, track.length, track.artist, track.album, track.title, track.trackNumber)
            println("Updating track to: " + combinedTrack)
            TracksTable.insert(combinedTrack)
          } else {
            println("Inserting track: " + track)
            TracksTable.insert(track)
          }
        }
      }
    }
  }

  def deleteById(id: Int) {
    val delete = TracksTable where {_.id is id}
    delete.delete
  }

  def findTrackByFilename(filename: String) = {
    val query = for (track <- TracksTable where (_.filename is filename.replace("\\", "\\\\"))) yield track
    parseResults(query)
  }

  def getById(id: Int): Option[Track] = {
    session withSession {
      val result = parseResults(TracksTable where {_.id is id})
      result.length match {
        case 0 => None
        case _ => Some(result.head)
      }
    }
  }

  private def parseResults(query: Query[Table[(Option[Int], String, Int, String, String, String, String)]]): List[Track] = {
    query.mapResult {
      case (id, filename, length, artist, album, title, trackNumber) => new Track(id, filename, length, artist, album, title, trackNumber)
    }.list
  }

  def getRandomTrack = {
    var max = 1
    session withSession {
      max = TracksTable.map {u => u}.list.size
    }
    getById((Math.random * max).asInstanceOf[Int]).get
  }

  def getNextTrack(track: Track) = {
    val next: Option[Track] = getById(track.id.get + 1)
    next match {
      case Some(track: Track) => next.get
      case _ => getById(1).get
    }
  }

  def getByText(text: String): List[Track] = {
    val limit: Int = 100
    var tracks: List[Track] = Nil
    session withSession {
      val queryString = "SELECT * FROM tracks WHERE artist LIKE ? OR title LIKE ? LIMIT ?"
      def setQueryParameters(text: String, st: PreparedStatement) = {
        st.setString(1, "%" + text + "%")
        st.setString(2, "%" + text + "%")
        st.setInt(3, limit)
      }
      val q = query[String, Track](queryString)(rsToTrack, setQueryParameters)
      tracks = q.withParameter(text).list
    }
    tracks
  }

  implicit def rsToTrack(rs: PositionedResult) =
    new Track(Some(rs.nextInt()), rs.nextString(), rs.nextInt(), rs.nextString(), rs.nextString(), rs.nextString(), rs.nextString())

  def getAllTracks: List[Track] = {
    var tracks: List[Track] = Nil
    session withSession {
      val queryString = "SELECT * FROM tracks ORDER BY artist, album, trackNumber, title DESC"
      val q = queryNA[Track](queryString)
      tracks = q.list
    }
    tracks
  }
}
