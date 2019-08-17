package com.example.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BIOClient {

    private static final Logger log = LoggerFactory.getLogger(BIOClient.class);

    public static void main(String[] args) throws Exception {
        log.info("BIOClient启动");
        Socket socket = new Socket("127.0.0.1", 8088);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.println("来自BIOClient的消息:hello");

        String msg = bufferedReader.readLine();
        log.info("来自BIOServer的响应:{}", msg);

        printWriter.close();
        bufferedReader.close();
        socket.close();
    }
}
