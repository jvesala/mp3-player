import sbt._

class Mp3WebProject(info: ProjectInfo) extends DefaultWebProject(info) {
  import BasicScalaProject._
  //override def useMavenConfigurations = true
  //override def packageAction = packageTask(mainClasses +++ mainResources, outputPath, defaultJarName, packageOptions).dependsOn(compile) describedAs PackageDescription

  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided->default"
  val mysql = "mysql" % "mysql-connector-java" % "5.1.10"
  val commons = "commons-lang" % "commons-lang" % "2.4"

  val jettytester = "org.mortbay.jetty" % "jetty-servlet-tester" % "7.0.0pre3" % "test->default"
  val scalatest = "org.scala-tools.testing" % "scalatest" % "0.9.5" % "test->default"

  val specs = "org.scala-tools.testing" % "specs" % "1.6.0" % "test->default"
  val junit = "junit" % "junit" % "4.4" % "test->default"
  val jmock = "org.jmock"  % "jmock" % "2.4.0" % "test->default"
  val cglib = "cglib" % "cglib" % "2.1_3" % "test->default"
  val objenesis = "org.objenesis" % "objenesis" % "1.0" % "test->default"
}