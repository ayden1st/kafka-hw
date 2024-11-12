package main.scala.kafka.ayden1st.common

import java.io.FileNotFoundException
import java.util.Properties

import java.net.URL

object Load {
  // load properties from file
  def loadProperties(filePath: String): Properties = {
    val props = new Properties()
    val resourcePath: URL = getClass.getResource(filePath)

    if (resourcePath != null) {
      props.load(resourcePath.openStream())
    } else {
      throw new FileNotFoundException(filePath)
    }
    props
  }
}
