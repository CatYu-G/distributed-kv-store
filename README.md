# raft-java

A lightweight and extensible Raft consensus implementation in Java, designed for distributed systems research and production-grade applications.

---

## Features

* Leader Election
* Log Replication
* Snapshotting
* Dynamic Cluster Membership Changes

---

## Quick Start

Deploy a 3-node Raft cluster locally:

```bash
cd raft-java-example
sh deploy.sh
```

This script will:

* Deploy three Raft nodes: `example1`, `example2`, and `example3`
* Create an `env/client` directory for testing client requests
* Initialize the cluster configuration

### Test a Write Operation

```bash
cd env/client

./bin/run_client.sh \
"list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" \
hello world
```

### Test a Read Operation

```bash
./bin/run_client.sh \
"list://127.0.0.1:8051,127.0.0.1:8052,127.0.0.1:8053" \
hello
```

---

## Usage

This section demonstrates how to use `raft-java` to build your own distributed storage system.

### Add Dependency

> Not yet published to Maven Central. Please install manually into your local repository.

```xml
<dependency>
    <groupId>com.github.raftimpl.raft</groupId>
    <artifactId>raft-java-core</artifactId>
    <version>1.9.0</version>
</dependency>
```

---

## Define Read / Write Interfaces

```proto
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
```

```java
public interface ExampleService {

    Example.SetResponse set(
        Example.SetRequest request
    );

    Example.GetResponse get(
        Example.GetRequest request
    );
}
```

---

## Server-Side Implementation

### 1. Implement the State Machine

```java
public interface StateMachine {

    /**
     * Create a snapshot of the current state machine data.
     */
    void writeSnapshot(String snapshotDir);

    /**
     * Load snapshot data when the node starts.
     */
    void readSnapshot(String snapshotDir);

    /**
     * Apply committed log entries.
     */
    void apply(byte[] dataBytes);
}
```

---

### 2. Implement Read / Write Logic

#### Write Path

```java
byte[] data = request.toByteArray();

boolean success =
    raftNode.replicate(
        data,
        Raft.EntryType.ENTRY_TYPE_DATA
    );

Example.SetResponse response =
    Example.SetResponse.newBuilder()
        .setSuccess(success)
        .build();
```

#### Read Path

```java
Example.GetResponse response =
    stateMachine.get(request);
```

---

### 3. Start the Server

```java
// Initialize RPC server
RPCServer server =
    new RPCServer(
        localServer.getEndPoint().getPort()
    );

// Initialize state machine
ExampleStateMachine stateMachine =
    new ExampleStateMachine();

// Configure Raft options
RaftOptions.snapshotMinLogSize =
    10 * 1024;

RaftOptions.snapshotPeriodSeconds =
    30;

RaftOptions.maxSegmentFileSize =
    1024 * 1024;

// Initialize Raft node
RaftNode raftNode =
    new RaftNode(
        serverList,
        localServer,
        stateMachine
    );

// Register internal services
server.registerService(
    new RaftConsensusServiceImpl(
        raftNode
    )
);

// Register client services
server.registerService(
    new RaftClientServiceImpl(
        raftNode
    )
);

// Register application service
server.registerService(
    new ExampleServiceImpl(
        raftNode,
        stateMachine
    )
);

// Start cluster
server.start();
raftNode.init();
```

---

## Architecture

```text
                +----------------+
                |     Client     |
                +--------+-------+
                         |
                         v
                +----------------+
                |  Leader Node   |
                +--------+-------+
                         |
       +-----------------+-----------------+
       |                                   |
       v                                   v
+--------------+                   +--------------+
| Follower #1  |                   | Follower #2  |
+--------------+                   +--------------+

      Raft Consensus Layer
   (Election / Replication /
     Snapshot / Membership)
```

---

## Project Highlights

* Production-oriented Raft implementation in Java
* Persistent WAL-based log storage
* Snapshot support for log compaction
* Dynamic membership changes
* Extensible state machine abstraction
* RPC-based cluster communication
* Suitable for distributed KV stores and storage systems

---

## Tech Stack

* Java
* Protobuf
* RPC
* Raft Consensus
* WAL
* Snapshotting
* Maven
* Shell


