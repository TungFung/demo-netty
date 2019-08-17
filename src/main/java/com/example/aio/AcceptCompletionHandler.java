package com.example.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel, AsynServerHandler> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void completed(AsynchronousSocketChannel result, AsynServerHandler attachment) {
        attachment.getAsynchronousServerSocketChannel().accept(attachment, this);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(buffer, buffer, new ReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AsynServerHandler attachment) {
        log.error("", exc);
        attachment.getCountDownLatch().countDown();
    }
}
