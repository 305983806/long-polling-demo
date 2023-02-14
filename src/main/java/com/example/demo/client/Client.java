package com.example.demo.client;

import java.io.IOException;

/**
 * Copyright (C) 2022 YUNTU Inc.All Rights Reserved.
 * FileName:<类名>
 * Description: <类说明>
 * History:
 * 版本号  作者      日期              简要操作以及相关介绍
 * 1.0    CP.Chen  2022/5/25 16:30   Create
 */
public class Client extends AbstractBootstrap {

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.poll();
        System.in.read();
    }

}
