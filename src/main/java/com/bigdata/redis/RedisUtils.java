package com.bigdata.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils {
    private static JedisPool jedisPool = null;
    private static final String host = "master";
    private static  final int port = 6379;

    public static Jedis getJedis(){
        if(null == jedisPool){
            synchronized (RedisUtils.class){
                if(null==jedisPool){
                    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
                    jedisPoolConfig.setMaxIdle(10); // 最大空闲连接数
                    jedisPoolConfig.setMaxTotal(100); // 最大总和连接数
                    jedisPoolConfig.setMaxWaitMillis(1000);
                    jedisPoolConfig.setTestOnBorrow(true);
                    jedisPool = new JedisPool(jedisPoolConfig, host, port);
                }
            }
        }
        return jedisPool.getResource();
    }
}
