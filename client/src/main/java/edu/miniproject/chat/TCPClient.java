package edu.miniproject.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.Properties;

public class TCPClient extends Application {
    private ChatClient client; private TextArea transcript; private TextField message; private Button sendButton; private Label status; private boolean readOnly = true;
    private String host = "localhost"; private int port = 3000;
    @Override public void start(Stage stage) {
        Properties p = new Properties(); try (InputStream in = getClass().getResourceAsStream("/client.properties")) { if (in != null) p.load(in); } catch (Exception ignored) {}
        host = getParameters().getRaw().size() > 0 ? getParameters().getRaw().get(0) : System.getProperty("chat.host", p.getProperty("server.host", "localhost"));
        port = getParameters().getRaw().size() > 1 ? Integer.parseInt(getParameters().getRaw().get(1)) : Integer.parseInt(System.getProperty("chat.port", p.getProperty("server.port", "3000")));
        showLogin(stage);
    }
    private void showLogin(Stage stage) {
        Label title = new Label("GROUP CHAT"); title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        Label detail = new Label("Connect to " + host + ":" + port); TextField username = new TextField(); username.setPromptText("Username (leave blank for read-only mode)");
        Button connect = new Button("CONNECT"); connect.setDefaultButton(true); Label error = new Label(); error.setStyle("-fx-text-fill: #b91c1c;");
        connect.setOnAction(e -> { try { String name = username.getText().trim(); readOnly = name.isEmpty(); client = new ChatClient(host, port); buildChat(stage, name); } catch (Exception ex) { error.setText(ex.getMessage()); } });
        VBox root = new VBox(14, title, detail, new Label("Choose a username to send messages."), username, connect, error); root.setPadding(new Insets(28)); root.setStyle("-fx-background-color: #f4f7fb; -fx-font-family: Arial;");
        stage.setTitle("TCP Chat Client"); stage.setScene(new Scene(root, 430, 300)); stage.show();
    }
    private void buildChat(Stage stage, String username) {
        Label title = new Label("GROUP CHAT"); title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #172554;");
        status = new Label("● Connecting..."); status.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
        transcript = new TextArea(); transcript.setEditable(false); transcript.setWrapText(true); transcript.setPromptText("Messages will appear here...");
        message = new TextField(); message.setPromptText(readOnly ? "Read-only mode — reconnect with a username to chat" : "Type a message (Enter to send)"); message.setDisable(readOnly);
        sendButton = new Button("SEND"); sendButton.setDisable(readOnly); Button leave = new Button("LEAVE");
        Runnable send = () -> { String text = message.getText().trim(); if (text.isEmpty() || readOnly) return; if (text.equalsIgnoreCase("end") || text.equalsIgnoreCase("bye")) { client.send(text); client.close(); stage.close(); } else { client.send(text); message.clear(); } };
        sendButton.setOnAction(e -> send.run()); message.setOnAction(e -> send.run()); leave.setOnAction(e -> { if (client != null) { client.send("bye"); client.close(); } stage.close(); });
        HBox input = new HBox(8, message, sendButton, leave); HBox.setHgrow(message, Priority.ALWAYS);
        VBox root = new VBox(12, title, status, transcript, input); root.setPadding(new Insets(18)); VBox.setVgrow(transcript, Priority.ALWAYS); root.setStyle("-fx-background-color: #f4f7fb; -fx-font-family: Arial;");
        stage.setTitle("TCP Chat Client — " + (username.isBlank() ? "Guest" : username)); stage.setScene(new Scene(root, 650, 500));
        stage.show();
        try { client.connect(username, this::receive, s -> Platform.runLater(() -> { status.setText("● " + s); status.setStyle(s.equals("Online") ? "-fx-text-fill: #16803c; -fx-font-weight: bold;" : "-fx-text-fill: #b91c1c; -fx-font-weight: bold;"); if (!s.equals("Online")) { message.setDisable(true); sendButton.setDisable(true); } })); }
        catch (Exception ex) { transcript.appendText("Could not connect: " + ex.getMessage() + "\nStart the server and try again.\n"); status.setText("● Offline"); }
        stage.setOnCloseRequest(e -> { if (client != null) { client.send("bye"); client.close(); } });
    }
    private void receive(String line) {
        Platform.runLater(() -> { if (line.startsWith("CHAT|")) { String[] parts = line.split("\\|", 4); if (parts.length == 4) transcript.appendText("[" + parts[1] + "] " + parts[2] + ": " + parts[3] + "\n"); }
            else if (line.startsWith("SYSTEM|")) transcript.appendText("[SYSTEM] " + line.substring(7) + "\n");
            else if (line.startsWith("USERS|")) transcript.appendText("[ACTIVE USERS] " + line.substring(6) + "\n");
            transcript.positionCaret(transcript.getLength()); });
    }
    public static void main(String[] args) { launch(args); }
}
