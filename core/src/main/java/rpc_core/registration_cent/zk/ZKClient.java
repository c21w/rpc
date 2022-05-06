package rpc_core.registration_cent.zk;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import rpc_core.client.RequestProcessor;
import rpc_core.config.PoolConfig;
import rpc_core.registration_cent.circulate.URL;
import rpc_core.util.CommonUtils;
import rpc_core.util.Pair;
import rpc_core.util.PropertiesLoader;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;


@Slf4j
public class ZKClient extends AbstractZookeeperClient{

    private static ZKClient zkClient;

    private CuratorFramework client;

    private ZKClient(String address) throws InterruptedException {
        this(address,null,null);
    }

    private ZKClient(String address,Integer baseSleepTimes, Integer maxRetryTimes) throws InterruptedException {
        super(address,baseSleepTimes,maxRetryTimes);
        client = CuratorFrameworkFactory.newClient(zkAddress, getRetryPolicy());
        new Thread(()->{
            client.getConnectionStateListenable().addListener((client, newState) -> {
                if (newState == ConnectionState.CONNECTED) {
                    //获取服务
                    RequestProcessor.loadService();
                    //监听
                    listener(PoolConfig.ZK_ROOT);
                    PoolConfig.OPEN_APPLICATION_START = true;
                }
            });
        }).start();
        client.start();
    }

    // 初始化
    static{
        String ipt = PropertiesLoader.getPropertiesStr("mrpc.zookeeper.ipt");
        Integer baseSleepTimes = PropertiesLoader.getPropertiesInteger("mrpc.zookeeper.baseSleepTimes");
        Integer maxRetryTimes = PropertiesLoader.getPropertiesInteger("mrpc.zookeeper.maxRetryTimes");
        try {
            zkClient = new ZKClient(ipt,baseSleepTimes,maxRetryTimes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    private static void listener(String path) {
        // 监听本节点和所有子节点
        CuratorCache cache = CuratorCache.build(ZKClient.firing().getCuratorFramework(), path);
        cache.listenable().addListener((type, before, after) -> {
            Map<String, Set<URL>> connectMap = PoolConfig.CONNECT_MAP;
            String[] split = null;
            switch (type.name()) {
                case "NODE_CREATED": // 监听器第一次执行时节点存在也会触发次事件
                    split = after.getPath().split("/");
                    if(split.length != 5)
                        break;
                    String serverName = split[2];
                    Set<URL> urls = connectMap.get(serverName);
                    if(CommonUtils.isEmptyList(urls)){
                        synchronized (ZKClient.class){
                            if(CommonUtils.isEmptyList(urls))
                                urls = new HashSet<>();
                        }
                    }
                    URL url = new URL(after.getPath() + "/" + new String(after.getData(), StandardCharsets.UTF_8));
                    if(!urls.contains(url))
                        urls.add(url);
                    break;
                case "NODE_CHANGED": // 节点更新
                    split = after.getPath().split("/");
                    serverName = split[2];
                    urls = connectMap.get(serverName);
                    urls.remove(new URL(before.getPath() + "/" + new String(before.getData() == null ? "".getBytes() : before.getData(),StandardCharsets.UTF_8)));
                    urls.add(new URL(after.getPath() + "/" + new String(after.getData() == null ? "".getBytes() : after.getData(),StandardCharsets.UTF_8)));
                    break;
                case "NODE_DELETED": // 节点删除
                    serverName = before.getPath().split("/")[2];
                    urls = connectMap.get(serverName);
                    urls.remove(new URL(before.getPath() + "/" + new String(before.getData() == null ? "".getBytes() : before.getData(),StandardCharsets.UTF_8)));
                    break;
                default:
                    break;
            }
        });
        // 开启监听
        cache.start();
        log.info("服务启动成功");
    }

    public static ZKClient firing(){
        return zkClient;
    }

    public CuratorFramework getCuratorFramework(){
        return client;
    }

    @Override
    public String getNodeData(String path) {
        try {
            byte[] result = client.getData().forPath(path);
            if (result != null) {
                return new String(result);
            }
        } catch (KeeperException.NoNodeException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> getChildrenNodeData(String path) {
        try {
            List<String> list = getChildrenNodeName(path);
            GetDataBuilder data = client.getData();
            List<String> arr = new ArrayList<>();
            for (String s : list) {
                byte[] bytes = data.forPath(path + "/" + s);
                arr.add(new String(bytes, StandardCharsets.UTF_8));
            }
            return arr;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Pair<String, String>> getChildrenNodeMsg(String path) {
        try {
            List<String> childrenNodeName = getChildrenNodeName(path);
            List<Pair<String,String>> list = new ArrayList<>(path.length());
            GetDataBuilder data = client.getData();
            for (String s : childrenNodeName) {
                byte[] bytes = data.forPath(path + "/" + s);
                String s1 = new String(bytes, StandardCharsets.UTF_8);
                list.add(new Pair<>(s,s1));
            }
            return list;
        }catch (Exception e){
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getChildrenNodeName(String path) {
        try {
            List<String> list = client.getChildren().forPath(path);
            return list;
        }catch (Exception e){
            return Collections.emptyList();
        }
    }

    @Override
    public void createPersistentData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createPersistentWithSeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemporarySeqData(String address, String data) {
        try {
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(address, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTemporaryData(String address, String data) {
        try {
            if(existNode(address))
                deleteNode(address);
            client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(address, data.getBytes());
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            setNodeData(address,data);
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    @Override
    public void setNodeData(String address, String data) {
        try {
            client.setData().forPath(address, data.getBytes());
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }

    @Override
    public void destroy() {
        client.close();
    }

    @Override
    public boolean deleteNode(String address) {
        try {
            client.delete().forPath(address);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean existNode(String address) {
        try {
            Stat stat = client.checkExists().forPath(address);
            return stat != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void watchNodeData(String path, Watcher watcher) {
        try {
            client.getData().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void watchChildNodeData(String path, Watcher watcher) {
        try {
            client.getChildren().usingWatcher(watcher).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
