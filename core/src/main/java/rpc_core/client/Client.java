package rpc_core.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import rpc_core.config.ClientConfig;
import rpc_core.handler.ClientHandler;
import rpc_core.handler.RpcHandlerCodec;
import rpc_core.message.RPCMessage;
import rpc_core.message.RequestMessage;
import rpc_core.protocol.RpcProtocol;

import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

@Slf4j
public class Client {

    private static EventLoopGroup clientGroup = new NioEventLoopGroup();

    private ClientConfig clientConfig;
    private ArrayBlockingQueue<RequestMessage> SEND_QUEUE = new ArrayBlockingQueue<>(32);

    public ClientConfig getClientConfig() {
        return clientConfig;
    }

    public void setClientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
    }

    public RPCMessage startClientApplication() throws InterruptedException {
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
        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddress(), clientConfig.getPort()).sync();
        log.debug("============ 服务启动 ============");
        this.startClient(channelFuture);
        RPCMessage rpcMessage = new RPCMessage(SEND_QUEUE);
        return rpcMessage;
    }

    /**
     * 开启发送线程，请求服务器端
     * @param channelFuture
     */
    private void startClient(ChannelFuture channelFuture) {
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
        asyncSendJob.start();
    }

    /**
     * 异步发送信息任务
     *
     */
    class AsyncSendJob implements Runnable {

        private ChannelFuture channelFuture;

        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RequestMessage data = SEND_QUEUE.take();
                    //将RpcInvocation封装到RpcProtocol对象中，然后发送给服务端，这里正好对应了上文中的ServerHandler
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());

                    //netty的通道负责发送数据给服务端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    log.error("AsyncSendJob error:[{}]",e);
                }
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        client.setClientConfig(new ClientConfig().setPort(8081).setServerAddress("localhost"));
        RPCMessage rpcMessage = client.startClientApplication();
        rpcMessage.send(RequestMessage.createRequestMessage("testData","test.DataImpl","123","abc"));
    }

}
