package rpc_core.registration_cent.zk;

import lombok.Data;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;
import rpc_core.util.Pair;

import java.util.List;
import java.util.Optional;


// Zookeeper相关操作

@Data
public abstract class AbstractZookeeperClient {

    protected String zkAddress;   // 地址
    protected int baseSleepTimes; // 重连间隔
    protected int maxRetryTimes;  // 最大重试次数

    public AbstractZookeeperClient(String zkAddress) {
        this.zkAddress = zkAddress;
        //默认3000ms
        this.baseSleepTimes = 1000;
        this.maxRetryTimes = 3;
    }

    public AbstractZookeeperClient(String zkAddress, Integer baseSleepTimes, Integer maxRetryTimes) {
        this.zkAddress = zkAddress;
        this.baseSleepTimes = Optional.ofNullable(baseSleepTimes).orElse(3000);
        this.maxRetryTimes = Optional.ofNullable(maxRetryTimes).orElse(3);
    }

    protected RetryPolicy getRetryPolicy(){
        return new ExponentialBackoffRetry(getBaseSleepTimes(), getMaxRetryTimes());
    }

    /**
     * 拉取节点的数据
     *
     * @param path
     * @return
     */
    public abstract String getNodeData(String path);

    /**
     * 获取指定目录下的子节点数据
     *
     * @param path
     * @return
     */
    public abstract List<String> getChildrenNodeData(String path);

    /**
     * 获取指定目录下的子节点路径
     *
     * @param path
     * @return
     */
    public abstract List<String> getChildrenNodeName(String path);

    /**
     * 获取指定目录下的子节点路径和数据
     *
     * @param path
     * @return
     */
    public abstract List<Pair<String,String>> getChildrenNodeMsg(String path);

    /**
     * 创建持久化类型节点数据信息
     *
     * @param address
     * @param data
     */
    public abstract void createPersistentData(String address, String data);

    /**
     * 创建持久化且有序类型节点数据信息
     * @param address
     * @param data
     */
    public abstract void createPersistentWithSeqData(String address, String data);


    /**
     * 创建有序且临时类型节点数据信息
     *
     * @param address
     * @param data
     */
    public abstract void createTemporarySeqData(String address, String data);


    /**
     * 创建临时节点数据类型信息
     *
     * @param address
     * @param data
     */
    public abstract void createTemporaryData(String address, String data);

    /**
     * 设置某个节点的数值
     *
     * @param address
     * @param data
     */
    public abstract void setNodeData(String address, String data);

    /**
     * 断开zk的客户端链接
     */
    public abstract void destroy();

    /**
     * 删除节点下边的数据
     *
     * @param address
     * @return
     */
    public abstract boolean deleteNode(String address);


    /**
     * 判断是否存在其他节点
     *
     * @param address
     * @return
     */
    public abstract boolean existNode(String address);


    /**
     * 监听path路径下某个节点的数据变化
     *
     * @param path
     */
    public abstract void watchNodeData(String path, Watcher watcher);

    /**
     * 监听子节点下的数据变化
     *
     * @param path
     * @param watcher
     */
    public abstract void watchChildNodeData(String path, Watcher watcher);

}