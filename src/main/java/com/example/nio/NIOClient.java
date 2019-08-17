package com.example.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class NIOClient {

    private static final Logger log = LoggerFactory.getLogger(NIOClient.class);

    public static void main(String[] args) throws Exception {
        log.info("NIOClient启动");
        Selector selector = Selector.open();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        if(socketChannel.connect(new InetSocketAddress("localhost", 8088))){
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        }else {
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
        }

        while(true){
            selector.select(1000);
            Set<SelectionKey> selectionKeySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeySet.iterator();
            SelectionKey selectionKey;
            while(iterator.hasNext()){
                selectionKey = iterator.next();

                try{
                    handleInput(selectionKey, selector);
                }catch (Exception e){
                    if(selectionKey != null){
                        selectionKey.cancel();
                        if(selectionKey.channel() != null){
                            selectionKey.channel().close();
                        }
                    }
                }

                iterator.remove();
            }
        }
    }

    private static void handleInput(SelectionKey selectionKey, Selector selector) throws Exception {
        if(!selectionKey.isValid()){
            return;
        }

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

        if(selectionKey.isConnectable()){
            if(socketChannel.finishConnect()){
                socketChannel.register(selector, SelectionKey.OP_READ);
                doWrite(socketChannel);
            }else{
                System.exit(1);//连接失败，退出
            }
        }

        if(selectionKey.isReadable()){
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            int readBytes = socketChannel.read(readBuffer);

            if(readBytes > 0){
                readBuffer.flip();
                byte[] bytes = new byte[readBuffer.remaining()];
                readBuffer.get(bytes);
                String msg = new String(bytes, "UTF-8");
                log.info("NIOClient收到消息:{}", msg);
            }else if(readBytes < 0){
                selectionKey.cancel();
                socketChannel.close();
            }else{

            }
        }
    }

    private static void doWrite(SocketChannel socketChannel) throws Exception {
        ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
        writeBuffer.put("来自NIOClient的消息：hello".getBytes("UTF-8"));
        writeBuffer.flip();
        socketChannel.write(writeBuffer);

        if(!writeBuffer.hasRemaining()){
            log.info("NIOClient成功发送消息");
        }else{
            log.info("需要处理写半包问题");
        }
    }
}
