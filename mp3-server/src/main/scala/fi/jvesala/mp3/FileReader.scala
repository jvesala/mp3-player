package fi.jvesala.mp3

import java.io.File
import org.apache.commons.lang.StringUtils

class FileReader {
  def createTrack(file: File): Option[Track] = {
    val track = tryEntaggedCreation(file)
    track match {
      case Some(track: Track) => Some(track)
      case None => {
        val track2 = tryJaudiotaggerCreation(file)
        track2 match {
          case Some(track: Track) => track2
          case None => {
            println("Warning, no track for filename: "+ file.getAbsolutePath)
            None
          }
        }
      }
    }
  }

  def tryEntaggedCreation(file: File): Option[Track] = {
    import entagged.audioformats.AudioFileIO
    val filename = file.getAbsolutePath
    try {
      val eFile = AudioFileIO.read(file)
      val length = eFile.getLength
      val tag = eFile.getTag
      val artist = tag.getFirstArtist.trim
      val album = tag.getFirstAlbum
      var title = tag.getFirstTitle.trim
      if (StringUtils.isEmpty(title)) {
        title = parseTitleFromFilename(filename)
      }
      val trackNo = tag.getFirstTrack
      Some(new Track(None, filename, length, artist, album, title, trackNo))
    } catch {
      case e => {
        //e.printStackTrace()
        println("Entagged error on file:" + filename)
      }
      None
    }
  }

  def parseTitleFromFilename(filename: String) = {
    val filenameNoEnding = filename.split("\\.").reverse.drop(1).reverse.mkString
    val index = filenameNoEnding.lastIndexOf(File.separator)
    if (index == -1) {
      filenameNoEnding
    }
    filenameNoEnding.substring(index + 1)
  }

  def tryJaudiotaggerCreation(file: File): Option[Track] = {
    import org.jaudiotagger.audio.{AudioFileIO, AudioHeader, AudioFile}
    import org.jaudiotagger.tag.{FieldKey, Tag}

    val filename = file.getAbsolutePath
    try {
      val f = AudioFileIO.read(file)
      val tag = f.getTag
      val length = f.getAudioHeader.getTrackLength
      val artist = tag.getFirst(FieldKey.ARTIST)
      val album = tag.getFirst(FieldKey.ALBUM)
      var title = tag.getFirst(FieldKey.TITLE)
      if (StringUtils.isEmpty(title)) {
        title = parseTitleFromFilename(filename)
      }
      val trackNo = tag.getFirst(FieldKey.TRACK)
      Some(new Track(None, filename, length, artist, album, title, trackNo))
    } catch {
      case e => {
        //e.printStackTrace()
        println("Jaudiotagger error on file:" + filename)
      }
      None
    }
  }
}