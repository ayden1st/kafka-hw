## HW5: Разработка приложения akka streams, alpakka

### Scala: Producer app
Импортируем библиотеки
```scala
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Producer
import akka.kafka.ProducerSettings
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import ch.qos.logback.classic.{Level, Logger}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
```
Создаем переменные akka, ActorSystem, Matrializer, Context
```scala
implicit val system: ActorSystem = ActorSystem("producer-sys")
implicit val mat: Materializer  = ActorMaterializer()
implicit val ec: ExecutionContextExecutor = system.dispatcher
```
Создаем переменные конфигурации продюсера из файла `resources/aplication.conf`
```scala
private val config = ConfigFactory.load()
private val producerConfig = config.getConfig("akka.kafka.producer")
private val producerSettings:ProducerSettings[String, String] = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
```
Запускаем отправку сообщений со значением от 1 до 100 в топик akka
```scala
private val produce: Future[Done] =
    Source(1 to 100)
      .map(value=> new ProducerRecord[String, String]("akka", value.toString))
      .runWith(Producer.plainSink(producerSettings))
```
Ждем заверешния отправки
```scala
  produce onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
```
### Scala: ConsumerApp
Импортируем библиотеки 
```scala
import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, RunnableGraph, Sink, ZipWith}
import akka.stream.{ActorMaterializer, ClosedShape, FlowShape, Graph, KillSwitches, UniqueKillSwitch}
import ch.qos.logback.classic.{Level, Logger}
import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps
```
Создаем переменные конфигурации консьюмера из файла `resources/aplication.conf`
```scala
val config: Config = ConfigFactory.load()
private val consumerConfig = config.getConfig("akka.kafka.consumer")
private val consumerSettings:ConsumerSettings[String, String] = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
```
Подписываемся на топик `akka`
```scala
  private val consume = Consumer
    .plainSource(consumerSettings, Subscriptions.topics("akka"))
```
Создаем переменные akka, ActorSystem, Matrializer, Context
```scala
implicit val system: ActorSystem = ActorSystem("fusion")
implicit val materializer: ActorMaterializer = ActorMaterializer()
implicit val ec: ExecutionContextExecutor = system.dispatcher
```
Создаем GraphDSL:
* input - получает сообщения из консьюмера
* value - преобразовывает значение из сообщения из `String` в `Int`
* mod1 - умножает входящее на 10
* mod2 - умножает входящее на 2
* mod3 - умножает входящее на 3
* output - выводит поток в терминал
* broadcast - разделяет поток на 3
* zip - создает зип для трех потоков, на выходе сумма трех чисел
```scala
private val graph =
  GraphDSL.create(){ implicit builder: GraphDSL.Builder[NotUsed] =>

    import GraphDSL.Implicits._

    val input = builder.add(consume)
    val value = builder.add(Flow[ConsumerRecord[String, String]].map(_.value().toInt))
    val mod1 = builder.add(Flow[Int].map(x=>x*10))
    val mod2 = builder.add(Flow[Int].map(x=>x*2))
    val mod3 = builder.add(Flow[Int].map(x=>x*3))
    val output = builder.add(Sink.foreach(println))

    val broadcast = builder.add(Broadcast[Int](3))
    //      val zip = builder.add(ZipWith[Int, Int, Int, Vector[Int]](Vector(_, _, _)))
    val zip = builder.add(ZipWith[Int, Int, Int, Int](List(_, _, _).sum))

    input ~> value ~> broadcast.in

    broadcast.out(0) ~> mod1 ~> zip.in0
    broadcast.out(1) ~> mod2 ~> zip.in1
    broadcast.out(2) ~> mod3 ~> zip.in2

    zip.out ~> output

    ClosedShape
  }
```
Запускаем GraphDSL
```scala
def main(args: Array[String]) : Unit ={
  RunnableGraph.fromGraph(graph).run()
  Thread.sleep(5000)
  system.terminate()
}
```