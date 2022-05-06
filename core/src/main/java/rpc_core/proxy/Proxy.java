package rpc_core.proxy;

import java.util.function.Consumer;

public interface Proxy {
    <T> T get(final Class<T> clazz) throws Throwable;

    <T> T tryGet(final Class<T> clazz,long time) throws Throwable;

    <T> T getFuture(final Class<T> clazz, Consumer success, Consumer error, long time);
}
