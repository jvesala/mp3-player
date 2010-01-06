import sbt._

class Mp3ServerProject(info: ProjectInfo) extends DefaultProject(info) {
  override def mainClass = Some("fi.jvesala.mp3.ServerRunner")

  val commons = "commons-lang" % "commons-lang" % "2.4"
  val mysql = "mysql" % "mysql-connector-java" % "5.1.10"

  val specs = "org.scala-tools.testing" % "specs" % "1.6.0" % "test->default"
  val junit = "junit" % "junit" % "4.4" % "test->default"
  val jmock = "org.jmock"  % "jmock" % "2.4.0" % "test->default"
  val cglib = "cglib" % "cglib" % "2.1_3" % "test->default"
  val objenesis = "org.objenesis" % "objenesis" % "1.0" % "test->default"
}