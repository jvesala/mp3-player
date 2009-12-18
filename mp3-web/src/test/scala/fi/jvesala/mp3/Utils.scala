package fi.jvesala.mp3

import org.specs._
import org.specs.runner._

object UtilsSpecification extends Specification with JUnit {
  "Utils" should {
    "return original with empty search" in {
      val original = "muse"
      val expected = "muse"
      Utils.highlight(original, "") mustEqual expected
    }

    "highlight simple text" in {
      val original = "muse"
      val expected = "m<span class=\"hit\">us</span>e"
      Utils.highlight(original, "us") mustEqual expected
    }

    "highlight text with variable case" in {
      val original = "Muse"
      val expected = "<span class=\"hit\">Mu</span>se"
      Utils.highlight(original, "mu") mustEqual expected
    }
  }
}