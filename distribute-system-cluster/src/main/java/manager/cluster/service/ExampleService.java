package manager.cluster.service;


public interface ExampleService {

    ClusterProto.SetResponse set(ClusterProto.SetRequest request);

    ClusterProto.GetResponse get(ClusterProto.GetRequest request);
}
