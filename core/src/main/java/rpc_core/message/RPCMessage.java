package rpc_core.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ArrayBlockingQueue;

@Data
@NoArgsConstructor
public class RPCMessage {

    private ArrayBlockingQueue<RequestMessage> queue;

    public RPCMessage(ArrayBlockingQueue<RequestMessage> queue) {
        this.queue = queue;
    }

    public void send(RequestMessage message){
        queue.add(message);
    }
}
