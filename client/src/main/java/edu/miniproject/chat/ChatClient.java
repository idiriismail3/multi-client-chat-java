package edu.miniproject.chat;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/** TCP protocol and socket I/O, independent of JavaFX. */
public final class ChatClient {
    private final String host; private final int port; private Socket socket; private BufferedReader in; private PrintWriter out;
    public ChatClient(String host, int port) { this.host = host; this.port = port; }
    public void connect(String username, Consumer<String> receiver, Consumer<String> status) throws IOException {
        socket = new Socket(); socket.connect(new InetSocketAddress(host, port), 5000);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        out.println(username == null ? "" : username.trim());
        Thread.ofVirtual().name("chat-client-reader").start(() -> { try { String line; while ((line = in.readLine()) != null) receiver.accept(line); } catch (IOException e) { if (!socket.isClosed()) receiver.accept("SYSTEM|Connection lost: " + e.getMessage()); } finally { close(); status.accept("Disconnected"); } });
        status.accept("Online");
    }
    public synchronized void send(String message) { if (out != null && !socket.isClosed()) out.println(message); }
    public void close() { try { if (socket != null) socket.close(); } catch (IOException ignored) {} }
}
