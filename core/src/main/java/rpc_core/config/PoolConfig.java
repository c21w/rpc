package rpc_core.config;

import io.netty.channel.ChannelFuture;
import rpc_core.client.RequestProcessor;
import rpc_core.message.RequestMessage;
import rpc_core.registration_cent.circulate.URL;
import rpc_core.util.Pair;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolConfig {
    // 魔数
    public final static short MAGIC_NUMBER = (short) 0xc55c;

    // zookeeper的根
    public final static String ZK_ROOT = "/mrpc";

    // 用于处理服务的类
    public final static Map<String,Object> PROVIDER_CLASS_MAP = new ConcurrentHashMap<>();

    // 请求发送到阻塞队列，然后依次执行
    public static BlockingQueue<RequestMessage> SEND_QUEUE = new ArrayBlockingQueue(100);

    // 结果存放处
    public final static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();

    // 存放远程服务的ip和port
    public static Map<String, Set<URL>> CONNECT_MAP = new ConcurrentHashMap<>();
    // 服务拉取标识
    public static Map<String, Boolean> CREATE_CONNECT_MAP = new ConcurrentHashMap<>();

    // 启动标识
    public static volatile boolean OPEN_APPLICATION_START = false;

    // 是否只做服务器
    public static volatile boolean ONLY_SERVER = false;


    //provider名称 --> 该服务有哪些集群URL
    public static List<String> SUBSCRIBE_SERVICE_LIST = new ArrayList<>(); //客户端
    public static Map<String, List<URL>> URL_MAP = new ConcurrentHashMap<>();
    public static Set<String> SERVER_ADDRESS = new HashSet<>();
    //每次进行远程调用的时候都是从这里面去选择服务提供者



}
