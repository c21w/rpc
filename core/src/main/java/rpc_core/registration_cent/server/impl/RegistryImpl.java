package rpc_core.registration_cent.server.impl;

import rpc_core.registration_cent.circulate.URL;
import rpc_core.registration_cent.server.RegistryService;
import rpc_core.registration_cent.zk.ZKClient;

public class RegistryImpl implements RegistryService {

    @Override
    public void register(URL url) {
        ZKClient.firing().createTemporaryData(url.buildZKPathName(), url.buildZKPathData());
    }

    @Override
    public void unRegister(URL url) {
        ZKClient.firing().deleteNode(url.buildZKPathName());
    }
}
