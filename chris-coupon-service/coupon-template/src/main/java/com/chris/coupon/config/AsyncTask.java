package com.chris.coupon.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;
import java.util.*;

@Component
@Slf4j
public class AsyncTask {

    public final StringRedisTemplate redisTemplate;

    @Autowired
    public AsyncTask(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Async
    public Future<String> execTaskA() throws InterruptedException{
        log.info("***************TaskA start**************");
        long star = new Date().getTime();

        Thread.sleep(5000);

        long end = new Date().getTime();
        System.out.println("TaskA结束，耗时毫秒数：" + (end - star));

        return new AsyncResult<>("TaskA结束");

    }

    @Async
    public Future<String> execTaskB() throws InterruptedException {

        System.out.println("TaskB开始");
        long star = new Date().getTime();

        Thread.sleep(3000);

        long end = new Date().getTime();
        System.out.println("TaskB结束，耗时毫秒数：" + (end - star));
        return new AsyncResult<>("TaskB结束");
    }


    @Async
    public void execTaskC() throws InterruptedException{
        System.out.println("TaskC Started!!!!!!!!!");
        Thread.sleep(3000);
        redisTemplate.opsForValue().set("test_for_async", "Lebron James");

    }
}
