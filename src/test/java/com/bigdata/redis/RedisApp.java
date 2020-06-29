package com.bigdata.redis;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisApp {

    private String host = "master";
    private int port = 6379;

    private Jedis jedis;

    @Before
    public void setUp(){
        jedis = new Jedis(host,port);
    }

    @Test
    public void test01(){
        jedis.set("info","this is the first jedis test");

        Assert.assertEquals("this is the first jedis test",jedis.get("info"));
    }

    @Test
    public void test02(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(10); // 最大空闲连接数
        jedisPoolConfig.setMaxTotal(100); // 最大总和连接数
        jedisPoolConfig.setMaxWaitMillis(1000);
        jedisPoolConfig.setTestOnBorrow(true);
        JedisPool pool = new JedisPool(jedisPoolConfig, host, port);
        Jedis jedis = pool.getResource();
        Assert.assertEquals("this is the first jedis test",jedis.get("info"));

    }

    @Test
    public void test03(){
        Jedis jedis = RedisUtils.getJedis();
        Assert.assertEquals("this is the first jedis test",jedis.get("info"));
    }


    @After
    public void tearDown(){
        jedis.close();
    }
}
