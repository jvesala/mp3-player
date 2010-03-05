package fi.jvesala.mp3

import com.thinkminimo.step.Step
import xml.Node
import scala.xml._
import fi.jvesala.mp3.{Track, Database}

class WebServer extends Step {
  val database = new Database

  before {
    contentType = "text/html"
  }

  get("/") {
    Template.page("mp3-web", <p>mp3 search servlet.try tracklist/ or track/:id</p>)
  }

  get("/track/:id") {
    val track = database.getById(params(":id").toInt)
    track match {
      case Some(track: Track) => Template.page("track", trackHtml(track, ""))
      case _ => Template.page("track", <div></div>)
    }
  }

  get("/search/:text") {
    val text = params(":text")
    val skip  = paramsMap.value.get("skip").getOrElse("0")
    val tracks = database.getByText(text).drop(skip.toInt).take(50)
    tracks.length match {
      case 0 => Template.page("tracksearch", <div></div>)
      case _ => Template.page("tracksearch", <div id="search">
        <ul>
          {for (track <- tracks) yield {
          <li>
            {trackHtml(track, text)}
          </li>
        }}
        </ul>
      </div>)
    }
  }

  get("/tracklist") {
    var index = "even"
    val trackRows = for (track <- database.getAllTracks) yield {
      if (index == "odd") index = "even" else index = "odd";
      <li class={index}>
        {trackHtml(track, "")}
      </li>
    }
    Template.page("tracklist", <div id="tracklist">
      <ul>
        {trackRows}
      </ul>
    </div>)
  }

  private def trackHtml(track: Track, search: String) = {
    <div class="track">
      <div class="id">
        {track.id.getOrElse(0)}
      </div>
      <div class="artist">
        {Utils.highlight(track.artist, search)}
      </div>
      <div class="title">
        {Utils.highlight(track.title, search)}
      </div>
    </div>
  }

  object Template {
    def page(title: String, content: Seq[Node]) = {
      "<! DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\" >"
      <html>
        <head>
          <title>
            {title}
          </title>
            <link href="/css/mp3.css" rel="stylesheet" type="text/css"/>
        </head>
        <body>
          {content}
        </body>
      </html>
    }
  }
}