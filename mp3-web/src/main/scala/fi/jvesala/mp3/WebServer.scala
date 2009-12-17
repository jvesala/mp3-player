package fi.jvesala.mp3

import com.thinkminimo.step.Step
import fi.apy.mp3.Database
import xml.Node
import scala.xml._

class WebServer extends Step {
  val database = new Database

  before {
    contentType = "text/html"
  }

  get("/") {
    Template.page("mp3-web", "",
      "<p>mp3 search servlet.try songlist/ or getsong/:id</p>"
      )
  }

  get("/getsong/:id") {
    <ul>
    <li>Kappaleen numero{params(":id")}</li>
    </ul>
  }

  get("/songlist") {
    Template.page("songlist", songListCss, songList)
  }

  private def songList = {
    val trackRows = for (track <- database.getAllTracks) yield
      "<div class=\"id\">" + track.id.getOrElse(0) + "</div><div class=\"artist\">" + track.artist + "</div><div class=\"title\">" + track.title + "</div>"
    "<ul>" + trackRows.foldLeft("")(_ + "<li>" + _ + "</li>") + "</ul>"
  }


  private def songListCss = {
    """
    <style type="text/css">
      li {
        width: 550px;
        list-style-type:none; 
      }
      ul li div.id {
        float: left;
        width: 50px;
      }
      ul li div.artist {
        float: left;
        width: 200px;
      }
      ul li div.title {
        float: left;    
        width: 300px;
      }
    </style>
    """
  }

  object Template {
    def page(title: String, css: String, content: String) = {
      "<! DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\" >"
      <html>
      <head>
      <title>{title}</title>{XML.loadString(css)}</head>
      <body>{XML.loadString(content)}</body>
      </html>
    }
  }

}