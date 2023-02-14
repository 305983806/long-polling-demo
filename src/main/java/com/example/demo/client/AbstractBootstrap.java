package com.example.demo.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2022 YUNTU Inc.All Rights Reserved.
 * FileName:<类名>
 * Description: <类说明>
 * History:
 * 版本号  作者      日期              简要操作以及相关介绍
 * 1.0    CP.Chen  2022/5/25 16:30   Create
 */
public class AbstractBootstrap {
    private final Logger log = LoggerFactory.getLogger(AbstractBootstrap.class);


    private final AtomicLong sequence = new AtomicLong();

    protected void poll() {
        /**
         * Thread.interrupt()方法不会中断一个正在运行的线程。它的作用是，在线程受到阻塞时抛出一个中断信号，这样线程就得以退出阻塞的状态。
         * 更确切的说，如果线程被Object.wait,Thread.join和Thread.sleep三种方法之一阻塞，那么，它将接收到一个中断异常（InterruptedException），从而提早地终结被阻塞状态。
         *
         * 此处循环执行，保证每次 longpolling 结束，再次发起 longpolling
          */
        while (!Thread.interrupted()) {
            URI uri = URI.create("http://127.0.0.1:8080/long-polling?name=tom");
            doPoll(uri);
        }
    }

    protected void doPoll(URI uri) {
        if (log.isInfoEnabled()) {
            log.info("第 {} 次 Long Polling", sequence.incrementAndGet());
        }

        long startTimeMillis = System.currentTimeMillis();

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        try {
            final HttpResponse<String> httpResponse = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (200 == httpResponse.statusCode()) {
                log.info("接收到响应信息：{}", httpResponse.body());
            } else {
                throw new RuntimeException(
                        String.format("unExcepted HTTP status code: %d", httpResponse.statusCode()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            long elapsed = (System.currentTimeMillis() - startTimeMillis) / 1000;
            System.out.println("connection close" + "     " + "elapse " + elapsed + "s");
        }
    }

}
