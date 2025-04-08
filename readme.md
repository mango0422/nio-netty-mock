
# Nio Netty Mock Framework

This project is a minimal implementation of a Netty-like asynchronous server framework using Java NIO. It includes key components such as channels, event loops, and a pipeline for handling inbound and outbound events. Logging is handled via SLF4J with Logback as the backend.

## Table of Contents

- [Nio Netty Mock Framework](#nio-netty-mock-framework)
  - [Table of Contents](#table-of-contents)
  - [Project Structure](#project-structure)
  - [Dependencies and Build Configuration](#dependencies-and-build-configuration)
    - [Logging Configuration](#logging-configuration)
  - [Implemented Features](#implemented-features)
    - [Core Components](#core-components)
    - [How It Works](#how-it-works)
  - [How to Run the Server](#how-to-run-the-server)
  - [What Can You Do With This Framework?](#what-can-you-do-with-this-framework)
  - [Next Steps for Enhancement](#next-steps-for-enhancement)
  - [License](#license)

## Project Structure

Below is an example directory tree for the project:

```
NioNettyMock/
├── build.gradle
├── settings.gradle
├── README.md
└── src
    ├── main
    │   ├── java
    │   │   └── com
    │   │       └── example
    │   │           └── nionetty
    │   │               ├── bootstrap
    │   │               │   ├── ServerBootstrap.java
    │   │               │   └── ServerBootstrapRunner.java
    │   │               ├── buffer
    │   │               │   └── CustomBuffer.java
    │   │               ├── channel
    │   │               │   ├── AbstractChannel.java
    │   │               │   ├── Channel.java
    │   │               │   ├── ChannelConfig.java
    │   │               │   ├── ChannelException.java
    │   │               │   ├── ChannelFuture.java
    │   │               │   ├── ChannelHandler.java
    │   │               │   ├── ChannelHandlerContext.java
    │   │               │   ├── ChannelId.java
    │   │               │   ├── ChannelInboundHandler.java
    │   │               │   ├── ChannelInboundHandlerAdapter.java
    │   │               │   ├── ChannelInitializer.java
    │   │               │   ├── ChannelOption.java
    │   │               │   ├── ChannelOutboundHandler.java
    │   │               │   ├── ChannelOutboundHandlerAdapter.java
    │   │               │   ├── ChannelPipeline.java
    │   │               │   ├── ChannelPromise.java
    │   │               │   ├── DefaultChannelConfig.java
    │   │               │   ├── DefaultChannelHandlerContext.java
    │   │               │   ├── DefaultChannelId.java
    │   │               │   ├── DefaultChannelPipeline.java
    │   │               │   └── DefaultChannelPromise.java
    │   │               └── channel
    │   │                   └── nio
    │   │                       ├── NioEventLoop.java
    │   │                       ├── NioEventLoopGroup.java
    │   │                       ├── NioServerSocketChannel.java
    │   │                       └── NioSocketChannel.java
    │   └── resources
    │       └── logback.xml  *(or, if you prefer, slf4j2.xml with a corresponding system property)*
    └── test
        └── java
            └── (JUnit test classes)
```

## Dependencies and Build Configuration

The project uses Gradle as the build tool and includes the following key dependencies:

- **SLF4J API:** `org.slf4j:slf4j-api:2.0.13`
- **Logback Classic:** `ch.qos.logback:logback-classic:1.5.6`
- **JUnit 5:** (for testing purposes)

Here is the content of the `build.gradle` file:

```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'com.example.nionetty'
version = '1.0-SNAPSHOT'

repositories {
    mavenCentral()
}

dependencies {
    implementation 'org.slf4j:slf4j-api:2.0.13'
    runtimeOnly 'ch.qos.logback:logback-classic:1.5.6'

    testImplementation platform('org.junit:junit-bom:5.10.2')
    testImplementation 'org.junit.jupiter:junit-jupiter-api'
    testRuntimeOnly 'org.junit.jupiter:junit-jupiter-engine'
}

application {
    mainClass = 'com.example.nionetty.bootstrap.ServerBootstrapRunner'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.named('test') {
    useJUnitPlatform()
}

tasks.withType(JavaCompile) {
    options.encoding = 'UTF-8'
}
```

### Logging Configuration

The Logback configuration file (`logback.xml` or `slf4j2.xml`) is placed in `src/main/resources/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.example.nionetty" level="DEBUG" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

> **Note:**  
> If using a nonstandard configuration file name (e.g. `slf4j2.xml`), make sure to set the system property `-Dlogback.configurationFile=slf4j2.xml`.

## Implemented Features

### Core Components

- **Channel and Related Classes:**  
  - `Channel` interface defines the basic operations (bind, close, write, etc.).  
  - `AbstractChannel` provides common functionality such as managing the channel ID, configuration, pipeline, and a future for channel closing.  
  - Concrete implementations such as `NioServerSocketChannel` and `NioSocketChannel` use Java NIO channels.

- **ChannelFuture and Promise:**  
  Asynchronous operations return a `ChannelFuture`, allowing you to:
  - Check if an operation is done.
  - Block until completion with `sync()`.
  - Attach listeners for completion (further extension possible).

- **Channel Pipeline and Handlers:**  
  The framework supports a pipeline model where inbound and outbound events are handled sequentially.  
  - `ChannelPipeline` manages a list of `ChannelHandler` objects.  
  - Inbound events are processed by implementations of `ChannelInboundHandler` (for example, `EchoServerHandler` which echoes received messages).  
  - Outbound events are managed by `ChannelOutboundHandler` classes.

- **EventLoop and EventLoopGroup:**  
  - `EventLoop` interface defines a loop that monitors registered channels via a NIO `Selector`.  
  - `SingleThreadEventLoop` implements the `EventLoop` interface to handle I/O events on a single thread.  
  - `NioEventLoopGroup` is a simple group that manages multiple `NioEventLoop` instances and distributes work (currently in a round-robin fashion).

### How It Works

1. **Server Bootstrap:**  
   The `ServerBootstrap` class sets up the event loop groups, the channel class, and the channel initializer.  
   When `bind(port)` is called:
   - A new channel is created using reflection.
   - If the channel is of type `NioServerSocketChannel`, the worker group is set.
   - The provided `ChannelInitializer` is added to the pipeline.
   - The channel is bound to the specified port (via an `InetSocketAddress`).

2. **Channel Operations:**  
   - **Binding and Accepting:**  
     `NioServerSocketChannel` opens a Java NIO `ServerSocketChannel`, sets it to non-blocking mode, and binds it to a local address. An acceptor thread is then started to continuously accept incoming client connections.  
   - **Client Channel Registration:**  
     Accepted client connections are wrapped into a `NioSocketChannel` and registered with an event loop from the worker group.
   - **Data Processing:**  
     When data arrives on a client channel, the event loop reads from the socket channel, converts bytes into a string, and passes it along the channel pipeline using `fireChannelRead()` followed by a call to `fireChannelReadComplete()`.

3. **Pipeline Handling:**  
   The `ChannelPipeline` class forwards events to each registered handler sequentially. You can extend this by adding custom `ChannelHandler` implementations to process or transform the data.

## How to Run the Server

Build and run the application using Gradle:

```bash
./gradlew clean build
./gradlew run
```

You should see log messages indicating:
- The server bootstrap has started.
- The server socket channel is bound to port 8080.
- The server remains active (blocking on `channel.closeFuture().sync()`) while accepting and logging incoming connections.

## What Can You Do With This Framework?

With this minimal Netty-style framework you can:
- **Build an asynchronous server:**  
  Create servers that process I/O events asynchronously without blocking threads.

- **Customize event handling:**  
  Write your own `ChannelHandler` implementations and add them to the pipeline for tasks such as protocol parsing, transformation, logging, or custom business logic.

- **Extend the core functionality:**  
  Enhance or add support for additional features such as:
  - More robust error handling.
  - Back-pressure and flow control for write operations.
  - Custom configuration options via `ChannelConfig` and `ChannelOption`.
  - Support for connection pooling or SSL/TLS.

- **Learn non-blocking I/O patterns:**  
  Study how asynchronous network operations can be implemented in Java using NIO and how event loops and pipelines work together.

## Next Steps for Enhancement

While the current implementation covers the basic functionality, you might want to further develop the framework by:
- Implementing a more complete channel pipeline that properly manages handler contexts.
- Supporting different threading models and improving the event loop distribution strategy.
- Adding timeout and heartbeat mechanisms.
- Incorporating comprehensive unit and integration tests.

## License

This project is provided as-is for learning and development purposes. You are free to use and modify the code for your own projects.