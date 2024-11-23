package kafka.ayden1st.common

import org.apache.kafka.clients.admin._
import kafka.ayden1st.common.Load.loadProperties

import java.util.Collections

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

  // Function to create admin client
  private def adminClient(path: String): AdminClient = {
    AdminClient.create(loadProperties(path))
  }

  // Function to delete all topics
  def deleteAllTopic(path: String): Unit = {
    val client = adminClient(path)
    val list = client.listTopics().names().get()
    val delete = client.deleteTopics(list)
    while (!delete.all().isDone) {
      client.close()
    }
  }

  // Function to create topic
  def createTopic(path: String, topic: String, partitions: Int, replication: Int): Unit = {
    val client = adminClient(path)
    val newTopic = new NewTopic(topic, partitions, replication.toShort)
    val create = client.createTopics(Collections.singleton(newTopic))
    while (!create.all().isDone) {
      client.close()
    }
  }
}
