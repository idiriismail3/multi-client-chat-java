package edu.miniproject.chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.InputStream;
import java.util.*;

public class TCPServer extends Application {
    private final ObservableList<String> users = FXCollections.observableArrayList();
    private final ListView<String> userList = new ListView<>(users);
    private final TextArea logs = new TextArea(); private ChatServer server;
    @Override public void start(Stage stage) {
        Properties p = new Properties(); try (InputStream in = getClass().getResourceAsStream("/server.properties")) { if (in != null) p.load(in); } catch (Exception ignored) {}
        int port = Integer.parseInt(System.getProperty("chat.port", p.getProperty("server.port", "3000")));
        Label heading = new Label("TCP CHAT SERVER"); heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Label state = new Label("● Starting..."); state.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
        userList.setCellFactory(v -> new ListCell<>() { private final Random r = new Random(); @Override protected void updateItem(String item, boolean empty) { super.updateItem(item, empty); if (empty || item == null) { setText(null); setStyle(""); } else { setText("●  " + item); Color c = Color.hsb(r.nextDouble()*360, .24, 1); setStyle(String.format("-fx-background-color: rgba(%.2f,%.2f,%.2f,0.65); -fx-padding: 8px;", c.getRed(),c.getGreen(),c.getBlue())); } } });
        logs.setEditable(false); logs.setWrapText(true); logs.setPromptText("Server activity log...");
        VBox left = new VBox(8, new Label("CONNECTED USERS"), userList); VBox.setVgrow(userList, Priority.ALWAYS); left.setPrefWidth(220);
        VBox right = new VBox(8, new Label("SERVER LOG"), logs); VBox.setVgrow(logs, Priority.ALWAYS);
        HBox content = new HBox(12, left, right); HBox.setHgrow(right, Priority.ALWAYS); VBox root = new VBox(12, heading, state, content); root.setPadding(new Insets(18)); VBox.setVgrow(content, Priority.ALWAYS);
        root.setStyle("-fx-background-color: #f4f7fb; -fx-font-family: 'Arial';");
        stage.setTitle("TCP Chat Server"); stage.setScene(new Scene(root, 760, 500)); stage.show();
        server = new ChatServer(port, s -> Platform.runLater(() -> logs.appendText(s + "\n")), list -> Platform.runLater(() -> users.setAll(list)));
        try { server.start(); state.setText("● Online · port " + port); state.setStyle("-fx-text-fill: #16803c; -fx-font-weight: bold;"); } catch (Exception e) { state.setText("● Failed: " + e.getMessage()); state.setStyle("-fx-text-fill: #b91c1c;"); }
        stage.setOnCloseRequest(e -> { if (server != null) server.stop(); Platform.exit(); });
    }
    public static void main(String[] args) { launch(args); }
}
