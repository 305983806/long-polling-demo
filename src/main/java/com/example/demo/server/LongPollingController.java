package com.example.demo.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Copyright (C) 2022 YUNTU Inc.All Rights Reserved.
 * FileName:<类名>
 * Description: <类说明>
 * History:
 * 版本号  作者      日期              简要操作以及相关介绍
 * 1.0    CP.Chen  2022/5/25 16:30   Create
 */
@RestController
public class LongPollingController {
    private final Logger log = LoggerFactory.getLogger(LongPollingController.class);

    private Random random = new Random();

    private final AtomicLong sequence = new AtomicLong();
    private final AtomicLong value = new AtomicLong();

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(100,200,5000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(100));

    @GetMapping("/long-polling")
    @ResponseBody
    public void longPolling(HttpServletRequest request, HttpServletResponse response) {
        String name = request.getParameter("name");

        final long currentSequence = sequence.incrementAndGet();
        if (log.isInfoEnabled()) {
            log.info("第 {} 次 long polling async.", currentSequence);
        }

        // 开启异步
        AsyncContext asyncContext = request.startAsync(request, response);

        asyncContext.setTimeout(51000);
        asyncContext.addListener(new AsyncListener() {
            @Override
            public void onComplete(AsyncEvent event) throws IOException {

            }

            // 超时处理，注意 asyncContext.complete();，表示请求处理完成
            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                AsyncContext asyncContext = event.getAsyncContext();
                asyncContext.complete();
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {

            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {

            }
        });

        // 提交线程池异步写入结果
        // 具体场景中可以有具体的策略进行操作
        executor.submit(new HandlePollingTask(asyncContext, currentSequence));

    }

    class HandlePollingTask implements Runnable {
        private final Logger log = LoggerFactory.getLogger(HandlePollingTask.class);

        private AsyncContext asyncContext;
        private long sequense;

        public HandlePollingTask(AsyncContext asyncContext, long sequense) {
            this.asyncContext = asyncContext;
            this.sequense = sequense;
        }

        @Override
        public void run() {
            try {
                // 通过 asyncContext 拿到 response
                PrintWriter out = asyncContext.getResponse().getWriter();
                int sleepSeconds = random.nextInt(100);

                if (log.isInfoEnabled()) {
                    log.info("{} wait {} second.", sequence, sleepSeconds);
                }

                try {
                    TimeUnit.SECONDS.sleep(sleepSeconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                long result = value.getAndIncrement();

                out.write(Long.toString(result));

            } catch (IOException e) {
                System.out.println(sequense + "handle polling failed");
            } finally {
                //数据写回客户端
                asyncContext.complete();
            }
        }
    }

}
