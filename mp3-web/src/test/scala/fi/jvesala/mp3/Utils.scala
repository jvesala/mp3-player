package fi.jvesala.mp3

import org.specs._
import org.specs.runner._

object UtilsSpecification extends Specification with JUnit {
  "Utils" should {
    "return original with empty search" in {
      val original = "muse"
      val expected = <span>muse</span>
      Utils.highlight(original, "") mustEqual expected
    }

    "highlight simple text" in {
      val original = "muse"
      val expected = <span>m<span class="hit">us</span>e</span>
      Utils.highlight(original, "us") mustEqual expected
    }

    "return original with empty search and escape ampersand" in {
      val original = "muse & muse"
      val expected = <span>muse &amp; muse</span>
      Utils.highlight(original, "").text mustEqual expected.text
    }

    "highlight simple text and work with ampersand" in {
      val original = "muse & moby"
      val expected = <span>m<span class="hit">us</span>e &amp; moby</span>
      Utils.highlight(original, "us").text mustEqual expected.text
    }

    "highlight text with variable case" in {
      val original = "Muse"
      val expected = <span><span class="hit">Mu</span>se</span>
      Utils.highlight(original, "mu").text mustEqual expected.text
    }
  }
}
