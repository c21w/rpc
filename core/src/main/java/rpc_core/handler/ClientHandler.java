package rpc_core.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import rpc_core.message.RequestMessage;
import rpc_core.protocol.RpcProtocol;
import rpc_core.util.StaticCodeUtil;

import static rpc_core.config.PoolConfig.RESP_MAP;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //客户端和服务端之间的数据都是以RpcProtocol对象作为基本协议进行的交互
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        //这里是传输参数更为详细的RpcInvocation对象字节数组。
        byte[] reqContent = rpcProtocol.getContent();
        String json = new String(reqContent,0,reqContent.length);
        RequestMessage requestMessage = JSON.parseObject(json, RequestMessage.class);

        //将请求的响应结构放入一个Map集合中，集合的key就是uuid，这个uuid在发送请求之前就已经初始化好了，所以只需要起一个线程在后台遍历这个map，查看对应的key是否有相应即可。
        if(RESP_MAP.containsKey(requestMessage.getUuid())){
            if(RESP_MAP.get(requestMessage.getUuid()).equals(StaticCodeUtil.OVER_TIME))
                RESP_MAP.remove(requestMessage.getUuid());
            else
                RESP_MAP.put(requestMessage.getUuid(),requestMessage);
        }
        ReferenceCountUtil.release(msg);
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        super.exceptionCaught(ctx, e);
        Channel channel = ctx.channel();
        if(channel.isActive()){
            ctx.close();
        }
    }
}
