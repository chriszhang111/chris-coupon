package com.chris.coupon.runner;

import com.chris.coupon.config.AsyncTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChrisCommanLineRunner{

    @Autowired
    AsyncTask asyncTask;


    public void run(String... args) throws Exception {
        System.out.println("This one runs first");
    }
}
