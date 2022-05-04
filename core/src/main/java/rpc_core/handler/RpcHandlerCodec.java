package rpc_core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.ByteToMessageDecoder;
import rpc_core.protocol.RpcProtocol;

import java.util.List;

import static rpc_core.config.PoolConfig.MAGIC_NUMBER;

// 解码器
public class RpcHandlerCodec extends ByteToMessageCodec<RpcProtocol> {

    /**
     * 协议的开头部分的标准长度
     */
    public final int BASE_LENGTH = 2 + 4 ;

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, RpcProtocol msg, ByteBuf out) throws Exception {
        out.writeShort(msg.getMagicNumber());
        out.writeInt(msg.getContentLength());
        out.writeBytes(msg.getContent());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) throws Exception {

        //读取的字节长度应该大于6个字节(因为有协议)
        if (byteBuf.readableBytes() >= BASE_LENGTH) {
            //内容长度大于1KB时拒收，跳过length个字节
            if (byteBuf.readableBytes() > 1024)
                byteBuf.skipBytes(byteBuf.readableBytes());

            // 读取魔数
            short magic = byteBuf.readShort();
            // 不是魔数开头，说明是非法的客户端发来的数据包
            if(magic != MAGIC_NUMBER){
                ctx.close();
                return;
            }

            //获取长度
            int length = byteBuf.readInt();

            //这里其实就是实际的RpcProtocol对象的content字段
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);
            out.add(rpcProtocol);
        }
    }
}