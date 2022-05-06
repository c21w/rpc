package rpc_core.client.watch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import rpc_core.registration_cent.zk.ZKClient;

import java.util.List;


public class ZKWatch implements Watcher {

    private static ZKClient client;

    static {
        client = ZKClient.firing();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        //监听字节的变动事件，并处理
        if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
//            getChildren();
        }
        if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
            String path = watchedEvent.getPath();
//            getChildrenData(path);
        }
    }
//
//    /**
//     * 获取子节点列表
//     */
//    public void getChildren() {
//        try {
//            List<String> nodes = client.getCuratorFramework().getChildren()
//                    .usingWatcher(new ZKWatch()).forPath(basePath);
//            System.out.println("服务器节点列表：" + nodes);
//            //把上线的服务放入map
//            for (String node : nodes) {
//                if (!serverMap.containsKey(node)) {
//                    Host host = new Host().getHost(node);
//
//                    byte[] bytes = client.getData().usingWatcher(new ZKWatch())
//                            .forPath(basePath + "/" + node);
//                    if (bytes != null && bytes.length > 0) {
//                        String[] data = new String(bytes).split("#");
//                        if (data.length == 2) {
//                            host.setResponseTime(Long.parseLong(data[0]));
//                            host.setLastTime(Long.parseLong(data[1]));
//                        } else {
//                            host.setResponseTime(0);
//                            host.setLastTime(0);
//                        }
//                    }
//                    serverMap.put(node, host);
//                }
//            }
//
//            //删除下线的服务
//            Iterator<String> iterator = serverMap.keySet().iterator();
//            while (iterator.hasNext()) {
//                if (!nodes.contains(iterator.next())) {
//                    iterator.remove();
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 获取服务器节点内容
//     *
//     * @param path
//     */
//    public void getChildrenData(String path) {
//        try {
////            /rpc-base/127.0.0.1:8901
//            byte[] bytes = client.getData().usingWatcher(new ZKWatch()).forPath(path);
//            String node = path.substring(path.lastIndexOf("/") + 1);
//            Host host = serverMap.get(node);
//            if (host != null) {
//                String[] data = new String(bytes).split("#");
//                if (data.length == 2) {
//                    host.setResponseTime(Long.parseLong(data[0]));
//                    host.setLastTime(Long.parseLong(data[1]));
//                } else {
//                    host.setResponseTime(0);
//                    host.setLastTime(0);
//                }
//            }
//            System.out.println(String.format("节点：%s,内容变更：%s", node, new String(bytes)));
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 将响应信息上报
//     *
//     * @param childNode
//     * @param time
//     */
//    public void setResponseTimeToNodeData(String childNode, long time) {
//        try {
//            //上报【请求耗时|系统时间】rpc-base/127.0.0.1:8901
//            client.setData().forPath(basePath + "/" + childNode,
//                    (time + "#" + System.currentTimeMillis()).getBytes(CharsetUtil.UTF_8));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 获取当前最优的服务器
//     *
//     * @return
//     */
//    public String getBestServer() {
//        long bestTime = -1;
//        String key = null;
//        for (Map.Entry<String, Host> entry : serverMap.entrySet()) {
//            Host host = entry.getValue();
//            if (host.getResponseTime() == 0) {
//                key = entry.getKey();
//                break;
//            }
//            long responseTime = host.getResponseTime();
//            if (bestTime == -1 || bestTime > responseTime) {
//                key = entry.getKey();
//                bestTime = responseTime;
//            }
//        }
//        return key;
//    }
//
//    public Map<String, Host> getServerMap() {
//        if (serverMap.size() == 0) {
//            getClient();
//            getChildren();
//        }
//        return serverMap;
//    }
//
//    /**
//     * 定时任务每5秒执行一次，把最后一次请求超过5秒的服务清0
//     *
//     * @throws Exception
//     */
//    @Scheduled(cron = "0/5 * * * * ?")
//    public void scheduled() throws Exception {
//        List<String> nodes = getClient().getChildren().forPath(basePath);
//        for (String node : nodes) {
//            byte[] bytes = getClient().getData().forPath(basePath + "/" + node);
//            //responsetime#lasttime
//            String[] data = new String(bytes).split("#");
//            if (data.length == 2) {
//                if (System.currentTimeMillis() - Long.parseLong(data[1]) > 5000) {
//                    getClient().setData().forPath(basePath + "/" + node, "0".getBytes());
//                    System.out.println(String.format("---定时任务执行---修改节点：%s, 内容为：%s",
//                            node, 0));
//                }
//            }
//        }
//    }

}
