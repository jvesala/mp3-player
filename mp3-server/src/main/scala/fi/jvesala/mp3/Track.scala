package fi.jvesala.mp3

import org.apache.commons.lang.builder.{ToStringBuilder, HashCodeBuilder, EqualsBuilder}

class Track(val id: Option[Int], val filename: String, val length: Int, val artist: String, val album: String, val title: String, val trackNumber: String) {

  def lengthInMillis = {
    length * 1000
  }

  override def toString: String = {
    ToStringBuilder.reflectionToString(this)
  }

  override def equals(obj: Any) = {
    EqualsBuilder.reflectionEquals(this, obj);
  }

  override def hashCode = {
    HashCodeBuilder.reflectionHashCode(this);
  }
}
