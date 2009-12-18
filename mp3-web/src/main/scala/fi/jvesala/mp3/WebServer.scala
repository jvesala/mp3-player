package fi.jvesala.mp3

import apy.mp3.{Track, Database}
import com.thinkminimo.step.Step
import xml.Node
import scala.xml._

class WebServer extends Step {
  val database = new Database

  before {
    contentType = "text/html"
  }

  get("/") {
    Template.page("mp3-web", "<p>mp3 search servlet.try tracklist/ or track/:id</p>")
  }

  get("/track/:id") {
    val track = database.getById(params(":id").toInt)
    track match {
      case Some(track: Track) => Template.page("track", trackHtml(track, ""))
      case _ => Template.page("track", "<div></div>")
    }
  }

  get("/search/:text") {
    val text = params(":text")
    val tracks = database.getByText(text)
    tracks.length match {
      case 0 => Template.page("tracksearch", "<div></div>")
      case _ => Template.page("tracksearch", "<div id=\"search\">" + trackList(tracks, text) + "</div>")
    }
  }

  get("/tracklist") {
    val tracks = database.getAllTracks
    Template.page("tracklist", "<div id=\"tracklist\">" + trackList(tracks, "") + "</div>")
  }

  private def trackHtml(track: Track, search: String) = {
    "<div class=\"track\"><div class=\"id\">" + track.id.getOrElse(0) + "</div><div class=\"artist\">" + highlight(track.artist, search) + "</div><div class=\"title\">" + highlight(track.title, search) + "</div></div>"
  }

  private def highlight(text: String, search: String) = {
    if(search.length > 0) text.replaceFirst(search, "<span class=\"hit\">" + search + "</span>") else text
  }

  private def trackList(tracks: List[Track], search: String) = {
    val trackRows = for (track <- tracks) yield trackHtml(track, search)
    "<ul>" + trackRows.foldLeft("")(_ + "<li>" + _ + "</li>") + "</ul>"
  }                                         

  object Template {
    def page(title: String, content: String) = {
      "<! DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\" >"
      <html>
      <head>
      <title>{title}</title>
      <link href="/mp3.css" rel="stylesheet" type="text/css"/>
      </head>
      <body>{XML.loadString(content)}</body>
      </html>
    }
  }
}