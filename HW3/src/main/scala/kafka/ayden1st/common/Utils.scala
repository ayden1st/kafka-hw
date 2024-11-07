package main.scala.kafka.ayden1st.common

object Utils {
  // Function to generate random string
  def randomString(length: Int): String = {
    val r = new scala.util.Random
    val sb = new StringBuilder
    for (i <- 1 to length) {
      sb.append(r.nextPrintableChar)
    }
    sb.toString
  }
  // Function to generate random int
  def randomInt(length: Int): Int = {
    val r = new scala.util.Random
    r.nextInt(length)
  }
}
