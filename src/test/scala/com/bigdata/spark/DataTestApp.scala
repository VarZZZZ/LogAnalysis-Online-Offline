package com.bigdata.spark

import batch.LogETLApp

/**
 * @Author: ly
 * @Date: 2020/6/30 17:58
 * @Version 1.0  
 *
 */
object DataTestApp {
  def main(args: Array[String]): Unit = {
    val url = "GET /course/list?c=cb HTTP/1.1"
    val referer = "https://www.imooc.com/course/list?c=data"
    val ip = "110.85.18.234"
    val rowkey = LogETLApp.getRowKey("20190130",url+referer+ip)
    println(rowkey)

  }

}
