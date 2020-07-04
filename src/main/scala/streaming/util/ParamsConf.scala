package streaming.util

import com.typesafe.config.{Config, ConfigFactory}

/**
 * @Author: ly
 * @Date: 2020/7/3 22:53
 * @Version 1.0
 *
 *项目参数配置
 * */
object ParamsConf {

  private val config: Config = ConfigFactory.load()

  val topic: String = config.getString("kafka.topic")
  val groupId: String = config.getString("kafka.group.id")
  val brokers: String = config.getString("kafka.broker.list")
  val redisDB: String = config.getString("redis.db")

  def main(args: Array[String]): Unit = {
    println(ParamsConf.topic)
    println(ParamsConf.groupId)
    println(ParamsConf.redisDB)
    println(ParamsConf.brokers)
  }
}
