package com.sdu.arrow.middleware.delayer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TaskCommandLiner implements CommandLineRunner {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void run(String... args) {

        SchedulerController schedulerController = applicationContext.getBean(SchedulerController.class);
        schedulerController.publicSchedulerTask();

    }
}
