package rpc_core.proxy;


import rpc_core.client.RequestProcessor;
import rpc_core.client.RpcClient;
import rpc_core.message.RequestMessage;
import rpc_core.util.StaticCodeUtil;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static rpc_core.config.PoolConfig.RESP_MAP;
import static rpc_core.config.PoolConfig.SEND_QUEUE;

public class JDKProxy implements Proxy{

    @Override
    public<T> T get(Class<T> clazz) throws Throwable {
        return tryGet(clazz,3000);
    }

    @Override
    public <T> T tryGet(Class<T> clazz, long time) throws Throwable {
        return (T)java.lang.reflect.Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},(obj,method,args)->{

            // 开启请求执行器
//            RequestProcessor.startJob();

            // 创建传输对象
            RequestMessage requestMessage = new RequestMessage();

            // 填充属性
            requestMessage.setArgs(args);
            requestMessage.setRequestMethod(method.getName());
            requestMessage.setRequestServiceName(clazz.getName());
            String uid = null;
            while ((uid = UUID.randomUUID().toString()) != null){
                if(!RESP_MAP.containsKey(uid))
                    break;
            }
            requestMessage.setUuid(uid);

            // 结果集打上符号
            RESP_MAP.put(uid, StaticCodeUtil.LOAD);

            // 请求存放阻塞队列
            SEND_QUEUE.add(requestMessage);
            long beginTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - beginTime < time) {
                Object object = RESP_MAP.get(uid);
                if (object instanceof RequestMessage) {
                    RESP_MAP.remove(uid);
                    return ((RequestMessage) object).getResponse();
                }
            }
            RESP_MAP.put(uid, StaticCodeUtil.OVER_TIME);
            throw new TimeoutException("client wait server's response timeout!");
        });
    }

    @Override
    public<T> T getFuture(Class<T> clazz, Consumer success, Consumer error, long time) {
        return (T)java.lang.reflect.Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},(obj,method,args)->{

            // 开启请求执行器
//            RequestProcessor.startJob();

            new Thread(()->{
                RequestMessage requestMessage = new RequestMessage();
                requestMessage.setArgs(args);
                requestMessage.setRequestMethod(method.getName());
                requestMessage.setRequestServiceName(clazz.getName());
                String uid = null;
                while ((uid = UUID.randomUUID().toString()) != null){
                    if(!RESP_MAP.containsKey(uid))
                        break;
                }
                requestMessage.setUuid(uid);
                RESP_MAP.put(uid, StaticCodeUtil.LOAD);
                SEND_QUEUE.add(requestMessage);
                long beginTime = System.currentTimeMillis();
                while (System.currentTimeMillis() - beginTime < time) {
                    Object object = RESP_MAP.get(uid);
                    if (object instanceof RequestMessage) {
                        RESP_MAP.remove(uid);
                        Object response = ((RequestMessage) object).getResponse();
                        if(response instanceof Exception)
                            if(error != null)
                                error.accept(response);
                        else
                            if(success != null)
                                success.accept(response);
                    }
                }
                RESP_MAP.put(uid, StaticCodeUtil.OVER_TIME);
                error.accept(new TimeoutException("client wait server's response timeout!"));
            }).start();
            return null;
        });
    }
}
