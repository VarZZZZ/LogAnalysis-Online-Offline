package streaming.util

import java.lang

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.kafka.common.serialization.StringDeserializer


/**
 * @Author: ly
 * @Date: 2020/7/3 22:53
 * @Version 1.0
 *
 *项目参数配置
 * */
object ParamsConf {

  private val config: Config = ConfigFactory.load()

  val topic= config.getString("kafka.topic").split(",")
  val groupId: String = config.getString("kafka.group.id")
  val brokers: String = config.getString("kafka.broker.list")
  val redisDB: String = config.getString("redis.db")

  val kafkaParams: Map[String, Object] = Map[String, Object](
    "bootstrap.servers" -> "master:9092,slave1:9092,slave2:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> groupId,
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: lang.Boolean)
  )

  def main(args: Array[String]): Unit = {
    println(ParamsConf.topic)
    println(ParamsConf.groupId)
    println(ParamsConf.redisDB)
    println(ParamsConf.brokers)
  }
}
