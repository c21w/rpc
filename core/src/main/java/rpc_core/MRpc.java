package rpc_core;

import rpc_core.client.RequestProcessor;
import rpc_core.server.Server;

public class MRpc {

    public static void serverStart(){
        new Server();
    }

    public static void clientStart(){
        RequestProcessor.startJob();
    }

    public static void startServerAndClient(){
        new Server();
        RequestProcessor.startJob();
    }
}
