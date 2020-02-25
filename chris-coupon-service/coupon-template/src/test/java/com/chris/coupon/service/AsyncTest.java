package com.chris.coupon.service;

import com.chris.coupon.config.AsyncTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.util.*;
import java.util.concurrent.Future;

@SpringBootTest
@RunWith(SpringRunner.class)
public class AsyncTest {

    @Autowired
    AsyncTask asyncTask;

    @Test
    public void testAsyncTask() throws InterruptedException {
        long star = new Date().getTime();
        System.out.println("任务开始，当前时间" +star );

        Future<String> taskA = asyncTask.execTaskA();
        Future<String> taskB = asyncTask.execTaskB();
        asyncTask.execTaskC();


        //间隔一秒轮询 直到 A B C 全部完成
//        while (true) {
//            if (taskA.isDone() && taskB.isDone()) {
//                break;
//            }
//            Thread.sleep(1000);
//        }
//
//        long end = new Date().getTime();
//        System.out.println("任务结束，当前时间" + end);
//        System.out.println("总耗时："+(end-star));
    }
}

