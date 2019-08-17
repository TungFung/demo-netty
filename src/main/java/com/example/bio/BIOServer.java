package com.example.bio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * JAVA的旧IO是同步阻塞的，Server端会为每一个客户端创建一条线程处理IO请求
 */
public class BIOServer {

    private static final Logger log = LoggerFactory.getLogger(BIOServer.class);

    public static void main(String[] args) throws Exception {
        log.info("BIOServer启动");
        ServerSocket serverSocket = new ServerSocket(8088);
        Socket socket = serverSocket.accept();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

        try{
            while(true){
                String msg = bufferedReader.readLine();
                if(msg == null){
                    continue;
                }
                log.info("BIOServer接收到数据:{}", msg);

                printWriter.println("来自BIOServer的消息：nice to meet you");
            }
        }catch (Exception e){
            log.error("BIOServer发生异常", e);
        }finally {
            socket.close();
            serverSocket.close();
        }
    }
}
