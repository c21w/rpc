package rpc_core.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import static rpc_core.config.PoolConfig.RESP_MAP;

// 请求消息 格式

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestMessage {

    //请求的目标方法，例如 findUser
    private String requestMethod;

    //请求的目标服务名称，例如：com.sise.user.UserService
    private String requestServiceName;

    // 请求参数信息
    private Object[] args;

    // UUID
    private String uuid;

    //接口响应的数据塞入这个字段中（如果是异步调用或者void类型，这里就为空）
    private Object response;

    public static RequestMessage createRequestMessage(String requestMethod, String requestServiceName, Object...args){
        String uuid = UUID.randomUUID().toString();
        RESP_MAP.put(uuid, new Object());
        return new RequestMessage(requestMethod,requestServiceName,args,uuid,null);
    }
}
