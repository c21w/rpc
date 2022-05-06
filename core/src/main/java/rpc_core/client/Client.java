//package rpc_core.client;
//
//import com.alibaba.fastjson.JSON;
//import io.netty.bootstrap.Bootstrap;
//import io.netty.channel.*;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioSocketChannel;
//import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
//import lombok.extern.slf4j.Slf4j;
//import registration_center.registry.URL;
//import rpc_core.config.ClientConfig;
//import rpc_core.handler.ClientHandler;
//import rpc_core.handler.RpcHandlerCodec;
//import rpc_core.message.RequestMessage;
//import rpc_core.protocol.RpcProtocol;
//import rpc_core.proxy.JDKProxy;
//import rpc_core.proxy.Proxy;
//import test.Data;
//import rpc_core.util.CommonUtils;
//
//import java.util.List;
//
//import static rpc_core.config.PoolConfig.SEND_QUEUE;
//import static rpc_core.config.PoolConfig.SUBSCRIBE_SERVICE_LIST;
//
//@Slf4j
//@lombok.Data
//public class Client {
//
//    private static EventLoopGroup clientGroup = new NioEventLoopGroup();
//
//    private ClientConfig clientConfig;
//
//    private AbstractRegisterV2 abstractRegister;
//
//    private Bootstrap bootstrap;
//
//    private IRpcListenerLoader iRpcListenerLoader;
//
//
//    public Proxy startClientApplication() throws InterruptedException {
////        this.clientConfig = clientConfig;
//        EventLoopGroup clientGroup = new NioEventLoopGroup();
//        bootstrap = new Bootstrap();
//        bootstrap.group(clientGroup);
//        bootstrap.channel(NioSocketChannel.class);
//        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
//            @Override
//            protected void initChannel(SocketChannel ch) throws Exception {
//                //管道中初始化一些逻辑，这里包含了上边所说的编解码器和客户端响应类
//                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,2,4,0,0));
//                //编码和解码
//                ch.pipeline().addLast(new RpcHandlerCodec());
//                ch.pipeline().addLast(new ClientHandler());
//            }
//        });
//        //常规的链接netty服务端
////        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getRegisterAddress(), clientConfig.getPort()).sync();
//        log.debug("============ 服务启动 ============");
//        this.startClient();
//        return new JDKProxy();
//    }
//
//    /**
//     * 启动服务之前需要预先订阅对应的服务
//     *
//     * @param serviceBean
//     */
//    public void doSubscribeService(Class serviceBean) {
//        if (abstractRegister == null) {
//            abstractRegister = new ZookeeperRegister(clientConfig.getRegisterAddress());
//        }
//        URL url = new URL();
//        url.setApplicationName(clientConfig.getApplicationName());
//        url.setServiceName(serviceBean.getName());
//        url.addParameter("host", CommonUtils.getIpAddress());
//        abstractRegister.subscribe(url);
//    }
//
//    /**
//     * 开始和各个provider建立连接
//     */
//    public void doConnectServer() {
//        // 填充启动器
//        ConnectionHandler.setBootstrap(getBootstrap());
//        for (String providerServiceName : SUBSCRIBE_SERVICE_LIST) {
//            List<String> providerIps = abstractRegister.getProviderIps(providerServiceName);
//            for (String providerIp : providerIps) {
//                try {
//                    ConnectionHandler.connect(providerServiceName, providerIp);
//                } catch (InterruptedException e) {
//                    log.error("[doConnectServer] connect fail ", e);
//                }
//            }
//            URL url = new URL();
//            url.setServiceName(providerServiceName);
//            //客户端在此新增一个订阅的功能
//            abstractRegister.doAfterSubscribe(url);
//        }
//    }
//
//    /**
//     * 开启发送线程，请求服务器端
//     */
//    private void startClient() {
//        Thread asyncSendJob = new Thread(new AsyncSendJob());
//        asyncSendJob.start();
//    }
//
//    /**
//     * 异步发送信息任务
//     *
//     */
//    class AsyncSendJob implements Runnable {
//
//        @Override
//        public void run() {
//            while (true) {
//                try {
//                    //阻塞模式
//                    RequestMessage data = SEND_QUEUE.take();
//                    //将RequestMessage封装到RpcProtocol对象中，然后发送给服务端
//                    String json = JSON.toJSONString(data);
//                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
//
//                    ChannelFuture channelFuture = ConnectionHandler.getChannelFuture(data.getRequestServiceName());
//                    //netty的通道负责发送数据给服务端
//                    channelFuture.channel().writeAndFlush(rpcProtocol);
//                } catch (InterruptedException e) {
//                    log.error("AsyncSendJob error:[{}]",e);
//                }
//            }
//        }
//    }
//
//    public static void main(String[] args) throws Throwable {
//        Client client = new Client();
//        Proxy proxy = client.startClientApplication();
//        Data data = proxy.get(Data.class);
//        client.doSubscribeService(Data.class);
//        client.doConnectServer();
//        client.startClient();
//
//    }
////
////    public static void main(String[] args) throws Throwable {
////        Client client = new Client();
////        RPCMessage rpcReference = client.startClientApplication();
////        Data data = rpcReference(Data.class);
////        client.doSubscribeService(Data.class);
////        ConnectionHandler.setBootstrap(client.getBootstrap());
////        client.doConnectServer();
////        client.startClient();
////        for (int i = 0; i < 100; i++) {
////            try {
////                String result = dataService.sendData("test");
////                System.out.println(result);
////                Thread.sleep(1000);
////            }catch (Exception e){
////                e.printStackTrace();
////            }
////        }
////    }
//
//}
