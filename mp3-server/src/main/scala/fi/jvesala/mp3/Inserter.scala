package fi.jvesala.mp3

import java.io.File

class Inserter {
  val reader = new FileReader
  val database = new Database

  def insertTracksFromDirectory(dir: File) {
    val directories = dir :: getSubdirs(dir)  
    val files = getMp3Files(directories)
    val tracks = files.map(f => reader.createTrack(f)).filter(s => s.isDefined).map(_.get)
    database.updateTracks(tracks.reverse)
  }

  def getSubdirs(parent: File): List[File] = {
    var children: List[File] = Nil
    for (child <- parent.list.map(s => new File(parent + File.separator + s)) if child.isDirectory) {
      children = getSubdirs(child) ::: child :: children
    }
    children 
  }

  def getMp3Files(directories: List[File]) = {
    var files: List[File] = Nil
    for (child <- directories) {
      files = child.listFiles.toList.filter(_.getAbsolutePath.endsWith(".mp3")) ::: files
    }
    files
  }
}

object InsertRunner {
  def main(args: Array[String]) {
    if (args.length != 1) {
      println("Usage: jar -cp classpath fi.jvesala.mp3.Inserter /path/to/mp3/root/dir")
      return
    }
    val root = new File(args.first)
    if (root == null || !root.isDirectory) {
      println("directory is not valid, exiting!")
      return
    }
    val inserter = new Inserter
    val start = System.currentTimeMillis
    inserter.insertTracksFromDirectory(root);
    val end = System.currentTimeMillis
    println("Insertion took " + (end - start) / 1000 + " seconds. ")
  }
}