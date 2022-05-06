package rpc_core.client;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import rpc_core.handler.ClientHandler;
import rpc_core.handler.RpcHandlerCodec;

@Slf4j
@Data
public class RpcClient {
    private EventLoopGroup group;
    private Channel channel;
    private String ip;
    private Integer port;
    private static ClientHandler handler = new ClientHandler();

    public RpcClient(String ip,Integer port){
        this.ip = ip;
        this.port = port;
        initClient();
    }

    private void initClient() {
        try{
            EventLoopGroup clientGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(clientGroup);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    //管道中初始化一些逻辑，这里包含了上边所说的编解码器和客户端响应类
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,2,4,0,0));
                    //编码和解码
                    ch.pipeline().addLast(new RpcHandlerCodec());
                    ch.pipeline().addLast(new ClientHandler());
                }
            });
            //常规的链接netty服务端
            this.channel = bootstrap.connect(ip, port).sync().channel();
        }catch (Exception e){
            if(channel != null)
                channel.close();
            if(group != null)
                group.shutdownGracefully();
        }
    }
}
