name := "play-jbook"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "com.mashape.unirest" % "unirest-java" % "1.4.9"
  //groupId			 >    artifactId   >   version
  //"com.google.guava" %	  "guava"      %   "12.0.1"
)     

play.Project.playJavaSettings
