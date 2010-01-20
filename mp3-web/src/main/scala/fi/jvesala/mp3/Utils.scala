package fi.jvesala.mp3

import org.apache.commons.lang.StringUtils

object Utils {
  def highlight(text: String, search: String) = {
    var result = <span>{text}</span>
    if (search.length > 0) {
      val index = StringUtils.upperCase(text).indexOf(StringUtils.upperCase(search))
      if (index != -1) {
        val start = text.substring(0, index)
        val hit = text.substring(index, index + search.length)
        val ending = text.substring(index + search.length, text.length)
        result = <span>{start}<span class="hit">{hit}</span>{ending}</span>
      }
    }
    result
  }
}