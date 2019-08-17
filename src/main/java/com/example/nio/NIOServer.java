package com.example.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO严格的来说是非阻塞IO，旧IO是阻塞IO，AIO才是异步非阻塞IO
 * NIO中读写数据都是要经过Buffer缓冲区的，每一种Java基本类型都对应一种缓冲区
 * Buffer
 * ByteBuffer,CharBuffer,DoubleBuffer,FloatBuffer,IntBuffer,LongBuffer,ShortBuffer
 * MappedByteBuffer(内存映射文件)
 *
 * Channel用来读写网络数据，是双向的,可以读写同时进行
 * Channel
 * FileChannel(文件), DatagramChannel(UDP),SocketChannel,ServerSocketChannel(TCP)
 *
 * 多路复用器Selector会不断的轮询注册在其上的channel，如果某个channel上发生了读或写事件，这个channel就处于就绪状态。
 * 被Selector轮询出来，通过SelectionKey获取就绪的channel集合，然后进行IO操作。
 *
 * 首先需要把channel注册到selector上，标记是哪种事件的，这样selector.selectKeys才会取到监听的这些事件
 * 因为selectKeys方法会取出当前所有注册了要监听的事件，所以用的时候，通过isAcceptable,isReadable来判断当前轮询到的
 * SelectionKey是哪种类型的
 */
public class NIOServer {

    private static final Logger log = LoggerFactory.getLogger(NIOServer.class);

    public static void main(String[] args) throws Exception {
        log.info("NIOServer启动");
        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(8088), 1024);
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        try{
            while(true){
                selector.select(1000);//selector每隔1s被唤醒一次
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
        }catch (Exception e){
            log.error("NIOSerer发生异常", e);
        }finally {
            selector.close();//selector关闭后，注册在上面的channel和pipe都会关闭
        }
    }

    private static void handleInput(SelectionKey key, Selector selector) throws Exception {
        if(!key.isValid()){
            return;
        }

        //开始只是绑定端口监听连接有没有进来,连接来了才会acceptable,这里才可以调用accept同意连接（相当于TCP的三次握手）
        if(key.isAcceptable()){
            //key是从selector取出的，selector中对应注册的channel就是最开始的channel，所以这里取到的就是最开始注册的channel
            ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
            SocketChannel sc = ssc.accept();//到这里相当于完成了TCP的三次握手
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ);
        }

        if(key.isReadable()){
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            SocketChannel sc = (SocketChannel) key.channel();
            int readBytes = sc.read(readBuffer);//readBytes表示对到多少字节数据，buffer里面存储的才是真实的数据

            //返回值大于0，读到了字节，对字节进行编解码
            if(readBytes > 0){
                //读消息
                readBuffer.flip();//flip操作的作用是将缓冲区当前的limit设置为position,position设置为0，用于后续对缓冲区的读取操作
                byte[] bytes = new byte[readBuffer.remaining()];
                readBuffer.get(bytes);//用于将缓冲区可读的字节数组复制到新创建的字节数组中
                String msg = new String(bytes, "UTF-8");
                log.info("NIOServer接收到消息:{}", msg);

                //回消息
                ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                writeBuffer.put("来自NIOServer的消息，已收到请求".getBytes("UTF-8"));//把数据复制到缓冲区中
                writeBuffer.flip();
                sc.write(writeBuffer);
                /**
                 * 由于SocketChannel是异步非阻塞的，它并不保证一次能够把需要发送的字节都发送完，此时会出现“写半包”问题。
                 * 需要注册写操作，不断轮询Selector将没发送完的ByteBuffer发送完毕，可以通过ByteBuffer的hasRemain方法
                 * 判断是否发送完成。
                 */

            }
            //返回值为-1，表示链路已经关闭，需要关闭SocketChannel释放资源
            else if(readBytes < 0){
                key.cancel();
                sc.close();
            }
            //返回值等于0，没有读取到字节，属于正常情况，忽略
            else{

            }
        }
    }
}
