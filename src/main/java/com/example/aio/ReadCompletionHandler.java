package com.example.aio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private AsynchronousSocketChannel asynchronousSocketChannel;

    public ReadCompletionHandler(AsynchronousSocketChannel asynchronousSocketChannel) {
        this.asynchronousSocketChannel = asynchronousSocketChannel;
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        try{
            //收消息
            attachment.flip();
            byte[] bytes = new byte[attachment.remaining()];
            attachment.get(bytes);
            log.info("收到消息:{}", new String(bytes, "UTF-8"));

            //回消息
            ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
            writeBuffer.put("服务端收到你的消息了!".getBytes("UTF-8"));
            writeBuffer.flip();
            asynchronousSocketChannel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    //没有发送完，继续发送
                    if(writeBuffer.hasRemaining()){
                        asynchronousSocketChannel.write(writeBuffer, writeBuffer, this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        asynchronousSocketChannel.close();
                    }catch (Exception e){}
                }
            });

        }catch (Exception e){
            log.warn("", e);
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        try {
            asynchronousSocketChannel.close();
        }catch (Exception e){}
    }
}
