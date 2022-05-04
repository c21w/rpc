package rpc_core.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

import static rpc_core.config.PoolConfig.MAGIC_NUMBER;

//自定义协议
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RpcProtocol implements Serializable {

    // 序列码
    private static final long serialVersionUID = 5359096060555795690L;

    // 魔数,第一时间判断是不是合法请求
    private short magicNumber = MAGIC_NUMBER;

    // 内容长度
    private int contentLength;

    // 内容，RequestMessage的byte数组
    private byte[] content;

    public RpcProtocol(byte[] data) {
        content = data;
        contentLength = data.length;
    }
}
