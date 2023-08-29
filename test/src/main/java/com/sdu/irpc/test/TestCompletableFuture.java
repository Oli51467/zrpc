package com.sdu.irpc.test;

import java.util.concurrent.CompletableFuture;

public class TestCompletableFuture {

    public static void main(String[] args) {
        CompletableFuture<Integer> completableFuture = new CompletableFuture<>();

        new Thread(() -> {
            int i;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            i = 5;
            completableFuture.complete(i);
        }).start();

        Integer res = completableFuture.getNow(5);
        System.out.println(res);
    }
}
