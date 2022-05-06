package rpc_core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import rpc_core.config.ServerConfig;
import rpc_core.handler.RpcHandlerCodec;
import rpc_core.handler.ServerHandler;
import rpc_core.registration_cent.circulate.URL;
import rpc_core.registration_cent.server.RegistryService;
import rpc_core.registration_cent.server.impl.RegistryImpl;
import rpc_core.util.PropertiesLoader;
import rpc_core.util.CommonUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static rpc_core.config.PoolConfig.*;

@Slf4j
@Data
public class Server {

    // 创建连接时的 LoopGroup
    private EventLoopGroup bossGroup = null;

    // 处理读写请求的 LoopGroup
    private EventLoopGroup workerGroup = null;

    // 配置信息
    private ServerConfig serverConfig;

    // 注册服务
    private RegistryService registryService;

    // 连接
    private Channel channel;

    // 服务列表
    private static Set<String> localServerSet = new HashSet<>();

    public Server(){
        // 配置服务消息
        initServerConfig();

        // 暴露服务
        exportService();

        // 开启监听
        startApplication();
    }

    // 初始化ServerConfig
    public void initServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setIp(PropertiesLoader.getPropertiesStr("mrpc.server.ip"));
        serverConfig.setPort(PropertiesLoader.getPropertiesInteger("mrpc.server.port"));
        serverConfig.setApplicationName(PropertiesLoader.getPropertiesStr("mrpc.server.name"));
        this.setServerConfig(serverConfig);
        registryService = new RegistryImpl();
    }

    // 暴露服务
    public void exportService(){
        String serverNames = PropertiesLoader.getPropertiesStr("mrpc.server.serverName");
        String serverWeights = PropertiesLoader.getPropertiesStr("mrpc.server.serverWeight");
        String[] split = serverNames.split(";");
        String[] weights = serverWeights.split(";");
        int i = 0;
        for (String s : split) {
            try {
                Class<?> aClass = Class.forName(s);
                Class<?>[] interfaces = aClass.getInterfaces();
                if(interfaces.length == 0 || interfaces.length > 1)
                    throw new Exception("interface count error");

                //默认选择该对象的第一个实现接口
                Class interfaceClass = interfaces[0];

                //把服务放入对象池
                PROVIDER_CLASS_MAP.put(interfaceClass.getName(), aClass.getConstructor().newInstance());

                int finalI = i;
                new Thread(()-> registryService.register(new URL(serverConfig.getApplicationName(),interfaceClass.getName(),serverConfig.getIp(),serverConfig.getPort(),Integer.valueOf(weights[finalI])))).start();
                i ++;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 开启程序
    public void startApplication() {
        try {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);

            // 配置服务器设置
            bootstrap.option(ChannelOption.TCP_NODELAY, true);  //禁用nagle算法，降低延迟
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);   // 连接队列的和，包括半连接(三次握手过程中的连接)和全连接(已完成三次握手的连接)
            bootstrap.option(ChannelOption.SO_SNDBUF, 16 * 1024)    // 接收缓冲区大小
                    .option(ChannelOption.SO_RCVBUF, 16 * 1024)     // 发送缓冲区大小
                    .option(ChannelOption.SO_KEEPALIVE, true);      // 两小时没有数据通信，TCP会自动发送一个活动探测数据报文

            // 给workerGroup线程组的关联的连接配置读写处理器，如果要配置bossGroup则要使用 bootstrap.handler()方法，客户端只有handler()方法，因为客户端只有workerGroup线程组
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {    // 当连接被创建时会执行此方法
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,2,4,0,0));
                    ch.pipeline().addLast(new RpcHandlerCodec());
                    ch.pipeline().addLast(new ServerHandler());
                }
            });

            // 启动，监听 serverConfig.getPort() 端口，sync()会阻塞主线程，直到ServerBootstrap创建完毕
            channel = bootstrap.bind(serverConfig.getPort()).sync().channel();
        }catch (Exception e){
            close();
        }
    }

    // 结束服务
    public void close(){
        localServerSet.stream().map(e -> new URL().setServiceName(e).setIp(serverConfig.getIp()).setPort(serverConfig.getPort()))
                .forEach(e -> registryService.unRegister(e));
        if(channel != null)
            channel.close();
        if(bossGroup != null)
            bossGroup.shutdownGracefully();
        if(workerGroup != null)
            workerGroup.shutdownGracefully();
    }


    /**
     * 暴露服务信息
     *
//     * @param serviceBean
     */
//    public void exportService(Object serviceBean) {
//        if (serviceBean.getClass().getInterfaces().length == 0) {
//            throw new RuntimeException("service must had interfaces!");
//        }
//        Class[] classes = serviceBean.getClass().getInterfaces();
//        if (classes.length > 1) {
//            throw new RuntimeException("service must only had one interfaces!");
//        }
//        if (registryService == null) {
//            registryService = new ZookeeperRegister(serverConfig.getRegisterAddress());
//        }
//        //默认选择该对象的第一个实现接口
//        Class interfaceClass = classes[0];
//
//        //把服务放入对象池
//        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
//
//        URL url = new URL();
//        url.setServiceName(interfaceClass.getName());
//        url.setApplicationName(serverConfig.getApplicationName());
//        url.addParameter("ip", CommonUtils.getIpAddress());   // 获取自身的ip地址
//        url.addParameter("port", String.valueOf(serverConfig.getServerPort()));
//        url.setChannelFuture(channelFuture);
////        PROVIDER_URL_SET.add(url);
//        // 在zookeeper上注册节点
//        new Thread(()-> registryService.register(url)).start();
//    }


    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.initServerConfig();  //初始化配置
//        server.exportService(new DataImpl());   // 暴露服务
        server.startApplication();  // 开启监听

    }
}
