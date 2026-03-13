# Pack

A lightweight Java networking library for TCP packet-based communication with automatic codec negotiation.

## Requirements

- Java 17+
- Guava (provided at compile time)

## Gradle

```groovy
repositories {
    maven { url "https://repo.willomane.com/releases" }
}

dependencies {
    implementation("me.will0mane.software.pack:api:1.0-SNAPSHOT")
    implementation("com.google.guava:guava:33.4.8-jre")
}
```

## Core Concepts

Pack is built around a few key abstractions:

- **Packet** — A marker interface for all data sent over the wire.
- **PacketFactory** — Serializes and deserializes a specific packet type using `PacketBuffer`.
- **Codec** — A named group of packet factories. Both sides negotiate which codec to use on connect.
- **Peer** — A client-side connection to a server.
- **Server** — Listens for incoming connections and hands you `Client` instances.
- **Pool** — Manages a fixed-size set of reusable peer connections.

## Quick Start

### 1. Define a Packet

```java
public record ChatMessage(String sender, String text) implements Packet {

    public static class Factory implements PacketFactory<ChatMessage> {

        @Override
        public Class<ChatMessage> packet() {
            return ChatMessage.class;
        }

        @Override
        public ChatMessage create(PacketBuffer buffer) {
            return new ChatMessage(buffer.readUTF(), buffer.readUTF());
        }

        @Override
        public void serialize(ChatMessage packet, PacketBuffer buffer) {
            buffer.writeUTF(packet.sender()).writeUTF(packet.text());
        }
    }
}
```

### 2. Create a Codec

A codec bundles all your packet factories under a shared identifier. Both the client and server must register the same codec for communication to work.

```java
public class ChatCodec implements Codec {

    @Override
    public String identifier() {
        return "chat-v1";
    }

    @Override
    public void registerAll(PacketRegistrar registrar) {
        registrar.register(new ChatMessage.Factory());
    }
}
```

### 3. Start a Server

```java
CodecInfo codecInfo = new CodecInfo(new ChatCodec());
CodecRegistry registry = new CodecRegistry(codecInfo);

ServerSocket serverSocket = new ServerSocket(8080);
BaseServer server = new BaseServer(registry, serverSocket);

server.loop(client -> {
    // Called for each new connection
    client.registrar().register(new PacketListener<ChatMessage>() {
        @Override
        public Class<ChatMessage> packetClass() {
            return ChatMessage.class;
        }

        @Override
        public void onPacket(ChatMessage packet) {
            System.out.println(packet.sender() + ": " + packet.text());
        }
    });
});
```

### 4. Connect a Peer (Client-Side)

```java
CodecInfo codecInfo = new CodecInfo(new ChatCodec());
CodecRegistry registry = new CodecRegistry(codecInfo);

Peer peer = Servers.at("localhost", 8080);
peer.connect(registry);

peer.send(new ChatMessage("Alice", "Hello!"));
```

### 5. Close Connections

Both `Peer` and `Server` implement `AutoCloseable`:

```java
peer.close();
server.close(); // also closes all connected clients
```

## PacketBuffer

`PacketBuffer` handles all serialization. Write-mode buffers are created with the no-arg constructor, read-mode buffers are created from a byte array.

```java
// Writing
PacketBuffer buf = new PacketBuffer();
buf.writeUTF("hello").writeInt(42).writeBoolean(true);
byte[] bytes = buf.writeFully();

// Reading
PacketBuffer buf = new PacketBuffer(bytes);
String s = buf.readUTF();   // "hello"
int n    = buf.readInt();    // 42
boolean b = buf.readBoolean(); // true
```

Supported types: `byte`, `short`, `int`, `long`, `float`, `double`, `char`, `boolean`, `String`, `Enum`, and arrays of all primitive/String types.

## Request/Response

Pack supports request/response patterns with automatic correlation and timeouts via `sendRequest`:

```java
CompletableFuture<ResponseMessage> future = peer.sendRequest(
    new MyRequest("data"),
    ResponseMessage.class,
    TimeUnit.SECONDS, 5
);

ResponseMessage response = future.get();
```

On the receiving side, use `PacketListener.withResponse()` to return a response packet:

```java
client.registrar().register(new PacketListener<MyRequest>() {
    @Override
    public Class<MyRequest> packetClass() {
        return MyRequest.class;
    }

    @Override
    public Packet withResponse(MyRequest packet) {
        return new ResponseMessage("ok");
    }

    @Override
    public void onPacket(MyRequest packet) {}
});
```

## Connection Pooling

`FixedSizePool` maintains a set of reusable peer connections:

```java
ConnectionInfo info = new ConnectionInfo("localhost", 8080);
CodecInfo codecInfo = new CodecInfo(new ChatCodec());
FixedSizePool pool = new FixedSizePool(5, info, codecInfo);

// Borrow a peer, use it, and return it automatically
pool.retrieve(peer -> {
    peer.send(new ChatMessage("Bob", "Hi!"));
});

// Borrow a peer and take ownership (not returned to pool)
pool.withdraw(peer -> {
    // peer is yours to manage
});

pool.shutdown(); // closes all pooled connections
```

## Codec Negotiation

On connect, the client and server automatically negotiate a codec:

1. The client sends its preferred codec and list of supported codecs.
2. The server checks for a match, preferring the client's preferred codec.
3. If a match is found, both sides switch to that codec.
4. If no match is found, the connection is rejected with `CodecMismatchException`.

You can support multiple codecs by passing them to `CodecInfo`:

```java
CodecInfo codecInfo = new CodecInfo(
    new ChatCodecV2(),     // preferred
    new ChatCodecV1()      // fallback
);
```

## Debugging

Set the system property `debug-pack` to enable debug logging on the server side:

```
-Ddebug-pack
```

