package manager.cluster.client;

import com.baidu.brpc.client.BrpcProxy;
import com.baidu.brpc.client.RpcClient;
import manager.cluster.service.ClusterProto;
import manager.cluster.service.ExampleService;
import com.googlecode.protobuf.format.JsonFormat;

public class ClusterClient {
    public static void main(String[] args) {

//        String ipPorts = "list://192.168.91.134:8051,192.168.91.134:8052,192.168.91.134:8053";
        String ipport = "list://192.168.91.134:8053";
        String key = "username123";
        String value =null;

        // init rpc client
        RpcClient rpcClient = new RpcClient(ipport);

        ExampleService exampleService = BrpcProxy.getProxy(rpcClient, ExampleService.class);
        final JsonFormat jsonFormat = new JsonFormat();

        // set
        if (value != null) {
            ClusterProto.SetRequest setRequest = ClusterProto.SetRequest.newBuilder()
                    .setKey(key).setValue(value).build();
            ClusterProto.SetResponse setResponse = exampleService.set(setRequest);
            System.out.printf("set request, key=%s value=%s response=%s\n",
                    key, value, jsonFormat.printToString(setResponse));
        } else {
            // get
            ClusterProto.GetRequest getRequest = ClusterProto.GetRequest.newBuilder()
                    .setKey(key).build();
            ClusterProto.GetResponse getResponse = exampleService.get(getRequest);
            System.out.printf("get request, key=%s, response=%s\n",
                    key, jsonFormat.printToString(getResponse));
        }

        rpcClient.stop();
    }
}
