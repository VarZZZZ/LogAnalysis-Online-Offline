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

  private val topic: Any = config.getString("kafka.topic")
  private val groupId: String = config.getString("kafka.group.id")
  private val brokerList: String = config.getString("kafka.broker.list")
  private val redisDB: String = config.getString("redis.db")

  def main(args: Array[String]): Unit = {
    println(ParamsConf.topic)
    println(ParamsConf.groupId)
    println(ParamsConf.redisDB)
    println(brokerList)
  }
}
