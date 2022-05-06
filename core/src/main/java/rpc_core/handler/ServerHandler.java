package rpc_core.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import rpc_core.message.RequestMessage;
import rpc_core.protocol.RpcProtocol;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static rpc_core.config.PoolConfig.PROVIDER_CLASS_MAP;

// 读适配器处理逻辑
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {

    // 读请求到来时处理
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 将内容转换为 RpcProtocol 对象
        RpcProtocol rpcProtocol = (RpcProtocol) msg;

        // 将 RpcProtocol 中的content 恢复成 requestMessage 对象
        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());
        RequestMessage requestMessage = JSON.parseObject(json, RequestMessage.class);

        // 开线程执行，避免阻塞下面的请求
        new Thread(()-> {
            //这里的PROVIDER_CLASS_MAP就是一开始预先在启动时候存储的Bean集合
            Object aimObject = PROVIDER_CLASS_MAP.get(requestMessage.getRequestServiceName());
            String requestServiceName = requestMessage.getRequestServiceName();
//            Object aimObject = null;
            try {
//                aimObject = Class.forName(requestServiceName).getConstructor().newInstance();
                Method[] methods = aimObject.getClass().getDeclaredMethods();
                Object result = null;
                // 找到合适的方法
                try {
                    for (Method method : methods) {
                        if (method.getName().equals(requestMessage.getRequestMethod())) {
                            // 通过反射找到目标对象，然后执行目标方法并返回对应值
                            if (method.getReturnType().equals(Void.TYPE)) {
                                method.invoke(aimObject, requestMessage.getArgs());
                            } else {
                                result = method.invoke(aimObject, requestMessage.getArgs());
                            }
                            break;
                        }
                    }
                    // 填充消息
                    requestMessage.setResponse(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    // 填充消息
                    requestMessage.setResponse(e);
                }

                // 将requestMessage再次封装RpcProtocol
                RpcProtocol respRpcProtocol = new RpcProtocol(JSON.toJSONString(requestMessage).getBytes());
                // 发送
                ctx.writeAndFlush(respRpcProtocol);
            } catch (Exception e) {
                e.printStackTrace();
                exceptionCaught(ctx,e);
            }
        }).start();
    }

    // 异常发生时的处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e){
        log.error("连接{}, 异常{}",ctx,e);
        Channel channel = ctx.channel();
        // 关闭连接
        if (channel.isActive()) {
            ctx.close();
        }
    }
}
