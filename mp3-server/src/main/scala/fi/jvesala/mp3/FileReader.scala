package fi.jvesala.mp3

import java.io.File
import org.apache.commons.lang.StringUtils;
import entagged.audioformats.AudioFileIO

class FileReader {
  def createTrack(file: File): Option[Track] = {
    tryEntaggedCreation(file)
  }

  def tryEntaggedCreation(file: File): Option[Track] = {
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
        println("Error on file:" + filename)
      }
      None
    }
  }

  def parseTitleFromFilename(filename: String) = {
    filename.trim.trim.trim.trim

    val index = filename.lastIndexOf(File.separator);
    if (index == -1) {
      filename;
    }
    filename.substring(index + 1);
  }
}