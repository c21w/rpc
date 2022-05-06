package rpc_core.registration_cent.circulate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import rpc_core.config.PoolConfig;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class URL {
    // 服务名称
    private String application;

    // 服务类路径
    private String serviceName;

    // ip
    private String ip;

    // port
    private Integer port;

    // 权重
    private Integer weight;

    public URL(String str){
        String[] split = str.split("/");
        serviceName = split[2];
        ip = split[4].split(":")[0];
        port = Integer.valueOf(split[4].split(":")[1]);
        String[] split1 = split[5].split(";");
        application = split1[1];
        weight = Integer.valueOf(split1[0]);
    }

    // 把url对象格式化为 节点名称
    public String buildZKPathName(){
        return new StringBuilder().append(PoolConfig.ZK_ROOT).append("/").append(serviceName).append("/provider/")
                .append(ip).append(":").append(port).toString();
    }

    // 把url对象格式化为 节点值
    public String buildZKPathData(){
        return new StringBuilder().append(weight).append(";").append(application).toString();
    }
}
