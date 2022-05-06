package rpc_core.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class PropertiesLoader {

    private static Properties properties;

//    private static Map<String, String> propertiesMap = new HashMap<>();

    static {
        properties = new Properties();
        InputStream in = PropertiesLoader.class.getClassLoader().getResourceAsStream("mrpc.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static String getPropertiesStr(String key) {
        if (Objects.isNull(properties))
            return null;

        if (CommonUtils.isEmpty(key))
            return null;

        if(properties.getProperty(key) == null)
            return null;
        return properties.getProperty(key);
    }

    /**
     * 根据键值获取配置属性
     *
     * @param key
     * @return
     */
    public static Integer getPropertiesInteger(String key) {
        if (properties == null)
            return null;

        if (CommonUtils.isEmpty(key))
            return null;

        if(properties.getProperty(key) == null)
            return null;
        return Integer.valueOf(properties.getProperty(key));
    }
}