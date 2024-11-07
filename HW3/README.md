## HW3: Разработка приложения с транзакциями

### Scala: Producer
Импортируются библиотеки
```scala
import main.scala.kafka.ayden1st.common.{Load, Log}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import java.util.Properties
import scala.util.Try
```
Настройки приложения хранятся в файле resources/producer.properties и загружаются при помощи функции loadProperties, создается экземпляр producer
```scala
val props: Properties = Load.loadProperties("/producer.properties")
val producer = new KafkaProducer[String,String](props)
```
Инициализируется трансакции
```scala
producer.initTransactions()
```
Отсылаются по пять сообщений в топики `topic1`, `topic2` и подвержается трансакция
```scala
producer.beginTransaction()
for (i <- 1 to 5) {
  producer.send(new ProducerRecord[String, String](topic1, i.toString, topic1 + "-message-" + i))
  producer.send(new ProducerRecord[String, String](topic2, i.toString, topic2 + "-message-" + i))
}
producer.commitTransaction()
```
Отсылаются по два сообщения в топики `topic1`, `topic2` и прерывается трансакция
```scala
producer.beginTransaction()
for (i <- 6 to 7) {
  producer.send(new ProducerRecord[String, String](topic1, i.toString, topic1 + "-message-" + i))
  producer.send(new ProducerRecord[String, String](topic2, i.toString, topic2 + "-message-" + i))
}
producer.abortTransaction()
```
Producer закрывается

### Scala: Consumer
Импортируются библиотеки
```scala
import main.scala.kafka.ayden1st.common.{Load, Log}
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import java.time.Duration
import java.util.Properties
import scala.jdk.CollectionConverters.IterableHasAsJava
import scala.util.Try
```
Настройки приложения хранятся в файле resources/consumer.properties и загружаются при помощи функции loadProperties, создается экземпляр consumer
```scala
val props: Properties = Load.loadProperties("/consumer.properties")
val consumer = new KafkaConsumer[String, String](props)
```
Consumer подписыватеся на топики `topic1`, `topic2`
```scala
consumer.subscribe(List(topic1, topic2).asJavaCollection)
```
В бесконечно цикле выводятся полученые сообщения из топиков
```scala
while (true) {
  val records: ConsumerRecords[String, String] = consumer.poll(Duration.ofSeconds(1))
  records.forEach { msg => log.info(s"${msg.partition}\t${msg.offset}\t${msg.key}\t${msg.value}") }
}
```