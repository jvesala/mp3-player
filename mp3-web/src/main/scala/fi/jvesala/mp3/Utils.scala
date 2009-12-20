package fi.jvesala.mp3

import org.apache.commons.lang.{StringEscapeUtils, StringUtils}

object Utils {

  def highlight(text: String, search: String) = {
    var result = StringEscapeUtils.escapeXml(text)
    if (search.length > 0) {
      val index = StringUtils.upperCase(text).indexOf(StringUtils.upperCase(search))
      if (index != -1) {
        val start = StringEscapeUtils.escapeXml(text.substring(0, index))
        val hit = StringEscapeUtils.escapeXml(text.substring(index, index + search.length))
        val ending = StringEscapeUtils.escapeXml(text.substring(index + search.length, text.length))
        result = start + "<span class=\"hit\">" + hit + "</span>" + ending
      } 
    }
    result
  }
}