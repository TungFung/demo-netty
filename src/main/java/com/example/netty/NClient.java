package com.example.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NClient {

    private static Logger log = LoggerFactory.getLogger(NClient.class);

    public static void main(String[] args) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NClientHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 8088).sync();
            channelFuture.channel().closeFuture().sync();//等待客户端链路关闭
        }catch (Exception e){
            log.error("", e);
        }finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public static class NClientHandler extends ChannelHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            byte[] bytes = "Hello".getBytes();
            ByteBuf reqByteBuf = Unpooled.buffer(bytes.length);
            reqByteBuf.writeBytes(bytes);
            ctx.writeAndFlush(reqByteBuf);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf reqByteBuf = (ByteBuf) msg;
            byte[] bytes = new byte[reqByteBuf.readableBytes()];
            reqByteBuf.readBytes(bytes);
            log.info("客户端收到消息:{}", new String(bytes, "UTF-8"));
        }
    }
}
