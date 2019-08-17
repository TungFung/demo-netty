package com.example.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.CountDownLatch;

public class AsynServerHandler implements Runnable {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private int port;

    private CountDownLatch countDownLatch;

    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public AsynServerHandler(int port) {
        this.port = port;
        try{
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
            asynchronousServerSocketChannel.bind(new InetSocketAddress(port));
        }catch (Exception e){
            log.error("绑定端口异常", e);
        }
    }

    @Override
    public void run() {
        countDownLatch = new CountDownLatch(1);
        doAccept();
        try{
            countDownLatch.await();
        }catch (Exception e){
            log.error("CountDownLatch等待异常", e);
        }
    }

    private void doAccept(){
        asynchronousServerSocketChannel.accept(this , new AcceptCompletionHandler());
    }

    public AsynchronousServerSocketChannel getAsynchronousServerSocketChannel() {
        return asynchronousServerSocketChannel;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }
}
