package streaming.util

import java.util.{Date, UUID}

import org.apache.commons.lang3.time.FastDateFormat

import scala.util.Random
import java.util

import com.alibaba.fastjson.JSONObject
/**
 * @Author: ly
 * @Date: 2020/7/4 14:58
 * @Version 1.0  
 *
 */
object MockData {
  def main(args: Array[String]): Unit = {
    val random = new Random()
    val dateFormat = FastDateFormat.getInstance("yyyyMMddmmss")

    for(i <- 0 to 9){
      val time = dateFormat.format(new Date())+""

      val userId = random.nextInt(1000)+""
      val courseId = random.nextInt(500)+""
      val fee = random.nextInt(400)+""
      val res = Array("0", "1")
      val flag = res(random.nextInt(2))+"" // 是否付款了
      val orderId = UUID.randomUUID().toString
      val map = new util.HashMap[String, Object]()
      map.put("time",time)
      map.put("userid",userId)
      map.put("courseid",courseId)
      map.put("fee",fee)
      map.put("flag",flag)
      map.put("orderid",orderId)
      val json = new JSONObject(map)
      println(json)
    }

  }
}
