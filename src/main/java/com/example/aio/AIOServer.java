package com.example.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Java新IO中的异步IO
 */
public class AIOServer {

    private static Logger log = LoggerFactory.getLogger(AIOServer.class);

    public static void main(String[] args) throws Exception {
        log.info("AIOServer启动");
        AsynchronousServerSocketChannel channel = AsynchronousServerSocketChannel.open();
        channel.bind(new InetSocketAddress(8088));
        channel.accept(null, new MyCompletionHandler());

        CountDownLatch countDownLatch = new CountDownLatch(1);
        countDownLatch.await();//锁住不让进程退出
    }

    public static class MyCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, Object> {

        @Override
        public void completed(AsynchronousSocketChannel result, Object attachment) {
//            result.
//
//            result.flip();
//            byte[] bytes = new byte[attachment.remaining()];
//            result.get(bytes);
//            log.info("收到消息:{}", new String(bytes, "UTF-8"));
        }

        @Override
        public void failed(Throwable exc, Object attachment) {

        }
    }
}
