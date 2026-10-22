package edu.miniproject.chat;

import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/** Socket/business logic only; deliberately independent from JavaFX. */
public final class ChatServer {
    private final int port;
    private final Consumer<String> logger;
    private final Consumer<List<String>> usersListener;
    private final Set<Handler> clients = ConcurrentHashMap.newKeySet();
    private final ExecutorService handlers = Executors.newVirtualThreadPerTaskExecutor();
    private volatile boolean running;
    private ServerSocket serverSocket;

    public ChatServer(int port, Consumer<String> logger, Consumer<List<String>> usersListener) {
        this.port = port; this.logger = logger; this.usersListener = usersListener;
    }
    public void start() throws IOException {
        serverSocket = new ServerSocket(); serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(port)); running = true;
        log("Server Started on port " + port);
        handlers.submit(() -> { while (running) { try { Socket s = serverSocket.accept(); log("Waiting for Client / connection accepted from " + s.getRemoteSocketAddress()); handlers.submit(new Handler(s)); } catch (SocketException e) { if (running) log("Server socket error: " + e.getMessage()); } catch (IOException e) { if (running) log("Connection error: " + e.getMessage()); } } });
    }
    public void stop() {
        running = false; try { if (serverSocket != null) serverSocket.close(); } catch (IOException ignored) {}
        for (Handler h : clients) h.close(); handlers.shutdownNow(); log("Server stopped.");
    }
    private void log(String s) { logger.accept("[" + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "] " + s); }
    private List<String> names() { return clients.stream().filter(h -> !h.readOnly && h.username != null).map(h -> h.username).sorted(String.CASE_INSENSITIVE_ORDER).toList(); }
    private void usersChanged() { usersListener.accept(names()); }
    private void broadcast(String text) { for (Handler h : clients) h.send(text); }

    private final class Handler implements Runnable {
        private final Socket socket; private BufferedReader in; private PrintWriter out;
        private String username = "Guest"; private boolean readOnly = true; private volatile boolean open = true;
        Handler(Socket socket) { this.socket = socket; }
        public void run() {
            try (socket) {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), java.nio.charset.StandardCharsets.UTF_8), true);
                String first = in.readLine();
                if (first != null && !first.isBlank()) {
                    String requested = first.trim().replaceAll("[\\r\\n]", "");
                    boolean duplicate = clients.stream().anyMatch(h -> !h.readOnly && h.username.equalsIgnoreCase(requested));
                    if (duplicate) { out.println("SYSTEM|That username is already in use. Please reconnect with another username."); return; }
                    username = requested; readOnly = false;
                }
                clients.add(this); usersChanged();
                if (readOnly) { send("SYSTEM|Connected in READ-ONLY MODE. Enter a username when reconnecting to send messages."); log("Guest connected in READ-ONLY MODE"); }
                else { send("SYSTEM|Welcome " + username + "!"); log("Welcome " + username); broadcast("SYSTEM|" + username + " joined the chat."); }
                String line;
                while (open && (line = in.readLine()) != null) {
                    String msg = line.trim(); if (msg.isEmpty()) continue;
                    if (msg.equalsIgnoreCase("end") || msg.equalsIgnoreCase("bye")) { if (!readOnly) broadcast("SYSTEM|" + username + " left the chat."); break; }
                    if (msg.equalsIgnoreCase("allUsers")) { send("USERS|" + String.join(", ", names())); continue; }
                    if (readOnly) { send("SYSTEM|READ-ONLY MODE: sending messages is disabled."); continue; }
                    String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    broadcast("CHAT|" + timestamp + "|" + username + "|" + msg.replace("\n", " "));
                }
            } catch (IOException e) { if (running && open) log("Client connection ended: " + e.getMessage()); }
            finally { open = false; clients.remove(this); usersChanged(); if (!readOnly) log(username + " disconnected"); }
        }
        synchronized void send(String s) { if (out != null) { out.println(s); if (out.checkError()) close(); } }
        void close() { open = false; try { socket.close(); } catch (IOException ignored) {} }
    }
}
