package com.imooc.bigdata.utils.readformat

import com.bigdata.utils.UAUtils
import com.bigdata.utils.ip.IPUtils
import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.sql.sources.{BaseRelation, TableScan}
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import scalaj.http.HttpResponse;
/**
 * @Author: ly
 * @Date: 2020/6/29 23:07
 * @Version 1.0
 *
 */
class IPDataSourceRelation(override val sqlContext: SQLContext, path: String, userSchema: StructType) extends BaseRelation with Serializable with TableScan {
  override def schema: StructType = {
    if (userSchema != null) {
      userSchema
    } else {
      StructType(
        StructField("ip", StringType, false) ::
          StructField("country", StringType, false) ::
          StructField("province", StringType, false) ::
          StructField("city", StringType, false) ::
          StructField("time", StringType, false) ::
          StructField("method", StringType, false) ::
          StructField("url", StringType, false) ::
          StructField("protocal", StringType, false) ::
          StructField("status", StringType, false) ::
          StructField("bytessent", StringType, false) ::
          StructField("referer", StringType, false) ::
          StructField("ua", StringType, false) ::
          StructField("browsername", StringType, false) ::
          StructField("browserversion", StringType, false) ::
          StructField("osname", StringType, false) ::
          StructField("osversion", StringType, false) ::
          Nil)
    }
  }

  override def buildScan(): RDD[Row] = {
    println("TableScan: buildScan called...")
    val icode = System.getProperty("icode")

    val fields = schema.fields
    // 读取数据内容，wholeTextFiles读取的是kv对，k是文件名，v是行内容
    val rdd = sqlContext.sparkContext.wholeTextFiles(path).map(f=>f._2)
    val rows = rdd.map(fileContent=>{

      val lines = fileContent.split("\n")
      val data = lines.map(line=>{
        val splits = line.split("\"")
        val split0 = splits(0).split(" ")
        val ip = split0(0).trim
        // java->  String time = (new StringBuilder()).append(split0[3].trim()).append(" ").append(split0[4].trim()).toString();
        val time = (new StringBuilder).append(split0(3).trim).append(" ").append(split0(4).trim).toString
        val info = IPUtils.getInstance.analyseIp(ip)
        val country = info.getCountry
        val province = info.getProvince
        val city = info.getCity
        val split1 = splits(1).split(" ")
        val method = split1(0)
        val url = split1(1)
        val protocal = split1(2)
        val split2 = splits(2).trim.split(" ")
        val status = split2(0).trim
        val bytessent = split2(1).trim
        val referer = splits(5)
        val ua = splits(7)
        val userAgent = UAUtils.getUserAgentInfo(ua)
        val browserName = userAgent.getBrowserName
        val browserVersion = userAgent.getBrowserVersion
        val osName = userAgent.getOsName
        val osVersion = userAgent.getOsVersion

        (new StringBuilder).append(ip).append("###").append(time).append("###").append(info).toString
      })
      data.map(s=>Row.fromSeq(s))
    })
    rows.flatMap(e=>e)
  }
}


