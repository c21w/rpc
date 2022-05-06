package mapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserMapperImpl implements UserMapper{

    static Map<Integer,String> userMap = new ConcurrentHashMap<>();

    static {
        userMap.put(1,"小明");
        userMap.put(2,"小王");
        userMap.put(3,"小程");
        userMap.put(4,"小周");
    }

    @Override
    public String getUser(Integer id) {
        return userMap.get(id);
    }
}
