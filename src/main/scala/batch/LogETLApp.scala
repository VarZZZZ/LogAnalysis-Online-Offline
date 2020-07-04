package batch

import java.util.zip.CRC32
import java.util.{Date, Locale}

import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.{HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.internal.Logging
import org.apache.spark.sql.SparkSession

/**
 * @Author: ly
 * @Date: 2020/6/30 16:46
 * @Version 1.0  
 *
 *          对日志进行ETL操作，将数据从文件系统 清洗 之后最终落地到HBase中
 *
 *          批处理：一天处理一次，需要传给LogETLAPP一个处理时间:yyyyMMdd
 *          HBase表：一天一个,logs_yyyyMMdd
 *          创建表
 *          rowKey的设计
 *          结合业务需求
 *          通常是组合使用：此处 时间作为rowKey的前缀_字段(MD5)
 *          后续分析时，一天一个批次，HBase表(logs_yyyyMMdd)中获取
 *
 */
import scalaj.http._;
object LogETLApp extends Logging {
  def main(args: Array[String]): Unit = {

//        if(args.length != 1){
//          println("Usage:LogETLApp <time> 111")
//          System.exit(-1)
//        }
//
//    val day = args(0)
//    val spark = SparkSession.builder()
//      .config("spark.executor.extraClassPath","/home/hadoop/tool/spark-2.4.4/external_jars/*")
//      .config("spark.driver.extraClassPath","/home/hadoop/tool/spark-2.4.4/external_jars/*")
//      .getOrCreate() // 集群模式运行

    val day = "20190130"

    val spark = SparkSession.builder().config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.hadoop.validateOutputSpecs", value = false)
      .master("local[2]").appName("LogETLApp")
      .getOrCreate()



    val input = s"hdfs://master:9000/access/$day/*"
    System.setProperty("icode", "77A5630C2BD7279E")
    // 用val logDF无法修改
    var logDF = spark.read.format("com.imooc.bigdata.spark.pk").option("path", input)
      .load()


    //logDF.show(false)
    import org.apache.spark.sql.functions._
    def formatTime() = udf((time: String) => {
      FastDateFormat.getInstance("yyyyMMddHHmm").format(FastDateFormat.getInstance("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH)
        .parse(time.substring(time.indexOf("[") + 1, time.lastIndexOf("]"))).getTime)
    })

    logDF = logDF.withColumn("formattime", formatTime()(logDF("time")))

    val hbaseInfoRDD = logDF.rdd.map( // 不是操作sql，一般都转化为rdd
      x => {
        val ip = x.getAs[String]("ip")
        val country = x.getAs[String]("country")
        val province = x.getAs[String]("province")
        val city = x.getAs[String]("city")
        val formattime = x.getAs[String]("formattime")
        val method = x.getAs[String]("method")
        val url = x.getAs[String]("url")
        val protocal = x.getAs[String]("protocal")
        val status = x.getAs[String]("status")
        val bytessent = x.getAs[String]("bytessent")
        val referer = x.getAs[String]("referer")
        val browsername = x.getAs[String]("browsername")
        val browserversion = x.getAs[String]("browserversion")
        val osname = x.getAs[String]("osname")
        val osversion = x.getAs[String]("osversion")
        val ua = x.getAs[String]("ua")

        val columns = scala.collection.mutable.HashMap[String, String]()
        columns.put("ip", ip)
        columns.put("country", country)
        columns.put("province", province)
        columns.put("city", city)
        columns.put("formattime", formattime)
        columns.put("method", method)
        columns.put("url", url)
        columns.put("protocal", protocal)
        columns.put("status", status)
        columns.put("bytessent", bytessent)
        columns.put("referer", referer)
        columns.put("browsername", browsername)
        columns.put("browserversion", browserversion)
        columns.put("osname", osname)
        columns.put("osversion", osversion)

        val rowKey = getRowKey(day,referer+url+ip+ua)
        val put = new Put(Bytes.toBytes(rowKey))

        for ((k, v) <- columns) {
          put.addColumn(Bytes.toBytes("o"), Bytes.toBytes(k.toString), Bytes.toBytes(v.toString))
        }
        put.setDurability(Durability.SKIP_WAL) //禁用WAL

        (new ImmutableBytesWritable(rowKey.getBytes), put)
      }
    )
    val conf = new Configuration()
    conf.set("hbase.rootdir", "hdfs://master:9000/HBase")
    conf.set("hbase.zookeeper.quorum", "master:2181,slave1:2181,slave2:2181")
    val tableName = createTable(day,conf)

    // 设置将数据写入到哪个表中
    conf.set(TableOutputFormat.OUTPUT_TABLE,tableName)

    // 保存数据
    hbaseInfoRDD.saveAsNewAPIHadoopFile(
      "hdfs://master:9000/etl/access/hbase",
      classOf[ImmutableBytesWritable], // rdd中的key类型
      classOf[Put], // rdd中的value类型
      classOf[TableOutputFormat[ImmutableBytesWritable]],
      conf
    )

    flushTable(tableName,conf) // 刷写数据

    logInfo(s"作业执行成功...$day")
    spark.stop()
  }
  def flushTable(table:String,conf:Configuration):Unit={
    var connection: Connection = null
    var admin: Admin = null
    try {
      connection = ConnectionFactory.createConnection(conf)
      admin = connection.getAdmin

      admin.flush(TableName.valueOf(table)) // memstore==>store
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (null != admin) {
        admin.close()
      }
      if (null != connection) {
        connection.close()
      }
    }
  }


  def createTable(day: String, conf: Configuration)= {
    val table = "access_" + day
    var connection: Connection = null
    var admin: Admin = null
    try {
      connection = ConnectionFactory.createConnection(conf)
      admin = connection.getAdmin

      /**
       * 离线处理，每天运行一次，如果中间处理过程有问题，重跑时
       * 先把表数据清空，然后重新写入
       */
      val tableName = TableName.valueOf(table)
      if (admin.tableExists(tableName)) {
        admin.disableTable(tableName)
        admin.deleteTable(tableName)
      }
      val tableDesc = new HTableDescriptor(tableName)
      val colDesc = new HColumnDescriptor("o")
      tableDesc.addFamily(colDesc)
      admin.createTable(tableDesc)
    } catch {
      case e: Exception => e.printStackTrace()
    } finally {
      if (null != admin) {
        admin.close()
      }
      if (null != connection) {
        connection.close()
      }
    }
    table
  }

  def getRowKey(time:String,info:String)={

    val builder = new StringBuilder(time)
    builder.append("_")

    val crc32 = new CRC32()
    crc32.reset()
    if(StringUtils.isNotEmpty(info)){
      crc32.update(Bytes.toBytes(info))
    }
    builder.append(crc32.getValue)
    builder.toString()

  }
}
