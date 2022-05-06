package rpc_core.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import rpc_core.config.PoolConfig;
import rpc_core.message.RequestMessage;
import rpc_core.protocol.RpcProtocol;
import rpc_core.proxy.JDKProxy;
import rpc_core.proxy.Proxy;
import rpc_core.registration_cent.circulate.URL;
import rpc_core.registration_cent.zk.ZKClient;
import rpc_core.util.Pair;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static rpc_core.config.PoolConfig.SEND_QUEUE;

@Slf4j
public class RequestProcessor {

    static {
        new Thread(new AsyncSendJob()).start();
    }

    public static Proxy startJob(){
        ZKClient.firing();
        while (!PoolConfig.OPEN_APPLICATION_START){

        }
        return new JDKProxy();
    }

    // 获取一个服务的可用连接
    private static Channel getChannel(String serverName){
        Set<URL> set = PoolConfig.CONNECT_MAP.get(serverName);
        if (set == null || set.size() == 0){
            throw new RuntimeException("无可用服务");
        }

        //todo.. 可做扩展
        URL url = set.stream().findAny().get();

        Channel channel = new RpcClient(url.getIp(), url.getPort()).getChannel();
        return channel;
    }

    public static void loadService() {
        List<String> childrenNodeName = ZKClient.firing().getChildrenNodeName(PoolConfig.ZK_ROOT);
        for (String s : childrenNodeName) {
            Set<URL> set = new HashSet<>();
            List<Pair<String, String>> pairs = ZKClient.firing().getChildrenNodeMsg(PoolConfig.ZK_ROOT + "/" + s + "/provider");
            for (Pair<String, String> pair : pairs) {
                set.add(new URL(PoolConfig.ZK_ROOT+"/"+s + "/provider/"+pair.getKey() + "/" + pair.getValue()));
            }
            PoolConfig.CONNECT_MAP.put(s,set);
        }
    }


    /**
     * 异步发送信息任务
     *
     */
    public static class AsyncSendJob implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    //阻塞模式
                    RequestMessage data = SEND_QUEUE.take();
                    //将RequestMessage封装到RpcProtocol对象中，然后发送给服务端
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());

                    Channel channel = RequestProcessor.getChannel(data.getRequestServiceName());
                    //netty的通道负责发送数据给服务端
                    channel.writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    log.error("AsyncSendJob error:[{}]", e);
                }
            }
        }
    }
}
