package chatty.gui;

import chatty.Chatty;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

/**
 * Controls the main Chatty window and connects JavaFX events to the chatbot.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox dialogContainer;

    @FXML
    private TextField userInput;

    @FXML
    private Button sendButton;

    /** Chatbot that processes commands submitted through the window. */
    private Chatty chatty;

    /**
     * Configures automatic scrolling and input-dependent button availability.
     */
    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
        sendButton.disableProperty().bind(Bindings
                .createBooleanBinding(() -> userInput.getText().isBlank(), userInput.textProperty()));
    }

    /**
     * Supplies the chatbot used to process user input and displays its greeting.
     *
     * @param chatty chatbot backing this window.
     */
    public void setChatty(Chatty chatty) {
        this.chatty = chatty;
        dialogContainer.getChildren().add(
                DialogBox.createChattyDialog("Hello! I'm Chatty. What can I do for you?"));
    }

    /**
     * Sends non-empty input to Chatty and appends both sides of the exchange.
     */
    @FXML
    private void handleUserInput() {
        String input = userInput.getText().strip();
        if (input.isEmpty()) {
            return;
        }

        String response = chatty.getResponse(input);
        dialogContainer.getChildren().addAll(
                DialogBox.createUserDialog(input),
                DialogBox.createChattyDialog(response));
        userInput.clear();

        if (chatty.shouldExit(input)) {
            Platform.runLater(Platform::exit);
        }
    }
}
