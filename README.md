raft-java
A Raft implementation library for Java.
Supported Features
Leader election
Log replication
Snapshot
Dynamic cluster membership changes
Quick Start
To deploy a 3-node Raft cluster locally, run the following script:
cd raft-java-example && sh deploy.sh
This script will deploy three instances — example1, example2, and example3 — under the raft-java-example/env directory.
It will also create a client directory for testing read and write operations on the Raft cluster.
Once deployed successfully, you can test a write operation with:
cd env/client
./bin/run_client.sh "list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" hello world
To test a read operation, run:
./bin/run_client.sh "list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" hello
Usage
This section explains how to use the raft-java dependency to build your own distributed storage system.
Add Dependency
(Not yet published to Maven Central — please install manually to your local repository.)
<dependency>
    <groupId>com.github.raftimpl.raft</groupId>
    <artifactId>raft-java-core</artifactId>
    <version>1.9.0</version>
</dependency>
Define Data Write/Read Interfaces
message SetRequest {
    string key = 1;
    string value = 2;
}
message SetResponse {
    bool success = 1;
}
message GetRequest {
    string key = 1;
}
message GetResponse {
    string value = 1;
}
public interface ExampleService {
    Example.SetResponse set(Example.SetRequest request);
    Example.GetResponse get(Example.GetRequest request);
}
Server-Side Implementation
1. Implement the StateMachine Interface
// This interface is mainly invoked internally by Raft.
public interface StateMachine {
    /**
     * Create a snapshot of the current state machine data.
     * Called periodically by each node.
     * @param snapshotDir output directory for snapshot data
     */
    void writeSnapshot(String snapshotDir);

    /**
     * Load snapshot data into the state machine.
     * Called when the node starts.
     * @param snapshotDir snapshot directory
     */
    void readSnapshot(String snapshotDir);

    /**
     * Apply committed data to the state machine.
     * @param dataBytes serialized data
     */
    void apply(byte[] dataBytes);
}
2. Implement Data Write and Read Logic
// ExampleService implementation should include:
private RaftNode raftNode;
private ExampleStateMachine stateMachine;
Write logic:
byte[] data = request.toByteArray();
// Replicate data synchronously across the Raft cluster
boolean success = raftNode.replicate(data, Raft.EntryType.ENTRY_TYPE_DATA);
Example.SetResponse response = Example.SetResponse.newBuilder()
    .setSuccess(success)
    .build();
Read logic (defined by the state machine):
Example.GetResponse response = stateMachine.get(request);
3. Server Startup Example
// Initialize RPC server
RPCServer server = new RPCServer(localServer.getEndPoint().getPort());

// Initialize application state machine
ExampleStateMachine stateMachine = new ExampleStateMachine();

// Configure Raft options
RaftOptions.snapshotMinLogSize = 10 * 1024;
RaftOptions.snapshotPeriodSeconds = 30;
RaftOptions.maxSegmentFileSize = 1024 * 1024;

// Initialize Raft node
RaftNode raftNode = new RaftNode(serverList, localServer, stateMachine);

// Register Raft internal services
RaftConsensusService raftConsensusService = new RaftConsensusServiceImpl(raftNode);
server.registerService(raftConsensusService);

// Register client-facing Raft services
RaftClientService raftClientService = new RaftClientServiceImpl(raftNode);
server.registerService(raftClientService);

// Register your application service
ExampleService exampleService = new ExampleServiceImpl(raftNode, stateMachine);
server.registerService(exampleService);

// Start RPC server and initialize Raft node
server.start();
raftNode.init();
About
A lightweight and extensible Raft consensus implementation in Java, designed for distributed systems research and production-grade applications.
Languages
Java 96.1%
Shell 3.9%
