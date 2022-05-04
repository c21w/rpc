package rpc_core.config;

import javassist.ClassMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PoolConfig {
    public final static short MAGIC_NUMBER = (short) 0xc55c;

    public final static Map<String,Object> PROVIDER_CLASS_MAP = new ConcurrentHashMap<>();

    public final static Map<String,Object> RESP_MAP = new ConcurrentHashMap<>();
}
