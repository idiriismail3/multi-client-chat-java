# Build artifacts for submission

After running `mvn clean package` from the project root, copy the generated files into this folder if your instructor wants the JARs alongside the source:

- `server/target/chat-server-1.0.0.jar`
- `client/target/chat-client-1.0.0.jar`

These are regular Maven JARs. The current supported launch workflow uses the included Maven JavaFX launch scripts, which resolve JavaFX runtime dependencies. Do not describe the JARs as self-contained/fat JARs unless you separately package and test them with all required JavaFX runtime libraries.
