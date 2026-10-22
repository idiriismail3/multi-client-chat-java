# Multi-Client Chat Application — Java TCP Sockets and JavaFX

A client-server group chat implemented in Java with TCP sockets and JavaFX. The repository contains two Maven applications: a server dashboard and a client GUI. Networking logic is separated from the JavaFX presentation layer.

## Requirements

- JDK 21 or newer (the project compiles with Java 21 language/API target; the server uses virtual threads)
- Maven 3.9+
- Internet access for Maven to download JavaFX dependencies on the first build

Check the tools:

```bash
java -version
mvn -version
```

On macOS, install Maven with `brew install maven` if needed.

## Run on macOS

Extract the project, open Terminal in the `chatproj` root, and run:

```bash
mvn clean package
./run-server.command
```

Leave the server running. Open another Terminal window in the same folder and start a client:

```bash
./run-client.command localhost 3000
```

Run the client command again in another Terminal window to connect additional users. Choose a different username in each window. The launch scripts use Maven's JavaFX launcher so JavaFX dependencies and native libraries are resolved correctly.

You can also launch directly through Maven:

```bash
mvn -f server/pom.xml javafx:run
mvn -f client/pom.xml javafx:run -Djavafx.args="localhost 3000"
```

## Configuration and command-line arguments

- Server defaults: `server/src/main/resources/server.properties`
- Client defaults: `client/src/main/resources/client.properties`
- Client script usage: `./run-client.command <ServerIPAddress> <PortNumber>`
- Example: `./run-client.command 192.168.1.10 3000`
- Server port override: `mvn -f server/pom.xml javafx:run -Dchat.port=3001`

For clients on another computer, use the server computer's LAN IP address instead of `localhost`; ensure both machines can reach each other and the firewall permits the selected TCP port. Avoid exposing the classroom demo server to the public internet.

## Features

- Username-based entry; a blank username enters read-only mode
- Concurrent TCP connections handled using Java 21 virtual threads
- Group broadcasts containing sender identity and timestamp
- `allUsers` command returns active named users to the requesting client
- `end`, `bye`, or the LEAVE button disconnects the client
- Online status indicator, server ListView of connected users, random user-row colors, and activity logs
- Configuration loaded from `.properties` resources

## Project structure

```text
chatproj/
├── pom.xml
├── server/                 # Server Maven module
│   ├── pom.xml
│   └── src/main/java/edu/miniproject/chat/
│       ├── TCPServer.java  # JavaFX server view
│       └── ChatServer.java # TCP server model
├── client/                 # Client Maven module
│   ├── pom.xml
│   └── src/main/java/edu/miniproject/chat/
│       ├── TCPClient.java  # JavaFX client view
│       └── ChatClient.java # TCP client model
├── run-server.command
├── run-client.command
├── docs/
│   ├── class-diagram.puml
│   ├── deployment-diagram.puml
│   └── demo-video-script.md
└── SUBMISSION-CHECKLIST.md
```

## Build

Run from the root:

```bash
mvn clean package
```

The build produces regular Maven JARs at:

- `server/target/chat-server-1.0.0.jar`
- `client/target/chat-client-1.0.0.jar`

**Packaging note:** these JARs are not self-contained executable/fat JARs. The supported run method in this project is the Maven JavaFX launcher scripts above, which resolve JavaFX runtime dependencies. If your instructor requires standalone JAR execution via `java -jar`, create and test a separate runtime/package configuration before claiming that requirement is met.

## UML diagrams

PlantUML source files are provided in `docs/`. They can be opened in a PlantUML-compatible viewer or rendered with a PlantUML extension.

## Demo video

Follow `docs/demo-video-script.md` for a concise three-minute walkthrough covering source architecture, messaging, `allUsers`, guest read-only mode, disconnection, server monitoring, and build output.
