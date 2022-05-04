package rpc_core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import rpc_core.config.ServerConfig;
import rpc_core.handler.RpcHandlerCodec;
import rpc_core.handler.ServerHandler;

import static rpc_core.config.PoolConfig.PROVIDER_CLASS_MAP;

@Slf4j
public class Server {

    // 创建连接时的 LoopGroup
    private static EventLoopGroup bossGroup = null;

    // 处理读写请求的 LoopGroup
    private static EventLoopGroup workerGroup = null;

    // 配置信息
    private ServerConfig serverConfig;

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    // 开启程序
    public void startApplication() throws InterruptedException {
        // 配置两个 LoopGroup
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();

        // 启动服务端启动器
        ServerBootstrap bootstrap = new ServerBootstrap();

        // 配置 组
        bootstrap.group(bossGroup, workerGroup);

        // 选择服务器的 ServerSocketChannel 实现，(其实是NIO注册中填写的附件)
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
                log.debug("初始化Server过程{}",ch);
                // 为每个连接配置读写适配器
                // 配置防粘包，半包
                ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024,2,4,0,0));
                //编码和解码
                ch.pipeline().addLast(new RpcHandlerCodec());
                ch.pipeline().addLast(new ServerHandler());
            }
        });
        // 启动，监听 serverConfig.getPort() 端口，sync()会阻塞主线程，直到ServerBootstrap创建完毕
        bootstrap.bind(serverConfig.getPort()).sync();
    }

    // 注册服务，每个服务必须包含一个接口
    public void registyService(Object serviceBean) {
        Class[] classes = serviceBean.getClass().getInterfaces();
        if (classes.length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        if (classes.length > 1) {
            throw new RuntimeException("service must only had one interfaces!");
        }
        Class interfaceClass = classes[0];
        //需要注册的对象统一放在一个MAP集合中进行管理
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        server.setServerConfig(new ServerConfig().setPort(8081));
        server.startApplication();
    }
}
