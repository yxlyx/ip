import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * Tests the released application through its real JavaFX controls without changing its JAR.
 * A Java agent starts the driver alongside the ordinary Launcher entry point.
 */
public final class GuiSmokeAgent {
    /** Main application window discovered after JavaFX startup. */
    private static Stage stage;

    /** Prevents construction of the agent entry-point class. */
    private GuiSmokeAgent() {
    }

    /**
     * Starts a bounded background driver while the unmodified application launches.
     *
     * @param phase fresh, reload, or corrupt scenario.
     * @param instrumentation JVM instrumentation service, not used to transform classes.
     */
    public static void premain(String phase, Instrumentation instrumentation) {
        Thread driver = new Thread(() -> runScenario(phase), "gui-smoke-driver");
        driver.start();
    }

    /**
     * Executes a complete GUI session and reports failures with a nonzero exit code.
     *
     * @param phase scenario to execute.
     */
    private static void runScenario(String phase) {
        try {
            waitForWindow();
            require(onFx(() -> stage.getTitle()).contains("Chatty"), "Missing product title");
            onFx(() -> {
                stage.setWidth(720);
                stage.setHeight(700);
                stage.toFront();
                return null;
            });
            verifyEmptyInput();
            switch (phase) {
                case "fresh" -> testFreshSession();
                case "reload" -> testReloadSession();
                case "corrupt" -> testCorruptSession();
                default -> throw new AssertionError("Unknown smoke scenario");
            }
            // Allow the JavaFX pulse to lay out and paint the last response before capture.
            Thread.sleep(500);
            captureWindow(phase);
            CountDownLatch closed = new CountDownLatch(1);
            onFx(() -> {
                stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> closed.countDown());
                return null;
            });
            send("bye", "Chatty signing off", true);
            require(closed.await(10, TimeUnit.SECONDS), "Bye did not close the GUI window");
            Files.writeString(Path.of(phase + "-passed.txt"), "PASS: " + phase + " GUI scenario\n");
            System.out.println("PASS: " + phase + " GUI scenario; normal bye shutdown");
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Waits for the ordinary application's visible JavaFX stage.
     *
     * @throws Exception if startup fails or times out.
     */
    private static void waitForWindow() throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(25);
        while (System.nanoTime() < deadline) {
            try {
                Stage candidate = onFx(() -> Window.getWindows().stream()
                        .filter(window -> window instanceof Stage && window.isShowing())
                        .map(window -> (Stage) window).findFirst().orElse(null));
                if (candidate != null) {
                    stage = candidate;
                    return;
                }
            } catch (IllegalStateException notStarted) {
                // The agent starts before the JavaFX toolkit; retry until initialization.
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Application did not show its GUI within 25 seconds");
    }

    /**
     * Checks the real composer prevents empty and whitespace-only submissions.
     *
     * @throws Exception if a control assertion fails.
     */
    private static void verifyEmptyInput() throws Exception {
        onFx(() -> {
            TextField input = (TextField) stage.getScene().lookup("#userInput");
            Button button = (Button) stage.getScene().lookup("#sendButton");
            require(input != null && button != null, "Composer controls missing");
            require(button.isDisabled(), "Empty composer should disable Launch");
            input.setText("   ");
            require(button.isDisabled(), "Whitespace composer should disable Launch");
            input.clear();
            return null;
        });
    }

    /**
     * Exercises core task operations and invalid-input feedback through the GUI.
     *
     * @throws Exception if any response or persisted record is incorrect.
     */
    private static void testFreshSession() throws Exception {
        require(!Files.exists(Path.of("data", "chatty.txt")), "Fresh session has existing data");
        send("todo read project brief", "[T][ ] read project brief", false);
        send("deadline submit proposal /by 2026-09-18", "Sep 18 2026", true);
        send("event design review /from fri 2pm /to 3pm", "[E][ ] design review", false);
        send("recurring team check-in /on 2026-09-21 /every 1 week", "Sep 21 2026", true);
        send("find proposal", "2.[D][ ] submit proposal", false);
        send("mark 2", "[D][X] submit proposal", true);
        send("unmark 2", "[D][ ] submit proposal", false);
        send("mark 4", "Sep 28 2026", true);
        send("delete 3", "Task removed", false);
        send("deadline invalid /by 2026-02-30", "OOPS!!!", true);
        require(onFx(() -> lastDialog().getStyleClass().contains("error-dialog")),
                "Invalid input did not produce a styled error dialog");
        verifySavedList();
        require(Files.readAllLines(Path.of("data", "chatty.txt")).size() == 3,
                "Expected three persisted tasks");
    }

    /**
     * Checks a new application process loads tasks saved by the first GUI session.
     *
     * @throws Exception if persisted task content or numbering changed.
     */
    private static void testReloadSession() throws Exception {
        verifySavedList();
        send("find check-in", "3.[R][ ] team check-in", true);
        verifySavedList();
    }

    /**
     * Ensures the GUI rejects every mutation after a failed startup load.
     *
     * @throws Exception if the warning or data-protection behavior is missing.
     */
    private static void testCorruptSession() throws Exception {
        require(onFx(() -> ((Label) lastDialog().lookup("#dialog")).getText())
                .contains("Task changes are disabled"), "Missing startup protection warning");
        String[] commands = {"todo new task", "deadline new /by 2026-09-18",
            "event new /from 2pm /to 3pm", "recurring new /on 2026-09-21 /every 1 week",
            "mark 1", "unmark 1", "delete 1"};
        for (String command : commands) {
            send(command, "Task changes are disabled", false);
        }
        send("list", "Flight plan status:", true);
    }

    /**
     * Checks the list rendered by the real application's list command.
     *
     * @throws Exception if tasks are missing, renumbered incorrectly, or not deleted.
     */
    private static void verifySavedList() throws Exception {
        String response = send("list", "1.[T][ ] read project brief", false);
        require(response.contains("2.[D][ ] submit proposal"), "Deadline missing from list");
        require(response.contains("3.[R][ ] team check-in (on: Sep 28 2026"),
                "Recurring schedule was not saved or correctly numbered");
        require(!response.contains("design review"), "Deleted event still present");
    }

    /**
     * Submits a command through a real control handler and checks its rendered response.
     * This drives JavaFX control events, not native keyboard or mouse input.
     *
     * @param command input text to submit.
     * @param expected response substring required for success.
     * @param useEnter whether to exercise the text-field action instead of Launch.
     * @return latest displayed chatbot response.
     * @throws Exception if control dispatch or response validation fails.
     */
    private static String send(String command, String expected, boolean useEnter) throws Exception {
        return onFx(() -> {
            TextField input = (TextField) stage.getScene().lookup("#userInput");
            Button button = (Button) stage.getScene().lookup("#sendButton");
            VBox dialogs = (VBox) stage.getScene().lookup("#dialogContainer");
            int previousCount = dialogs.getChildren().size();
            input.setText(command);
            require(!button.isDisabled(), "Launch disabled for valid nonempty input");
            if (useEnter) {
                input.fireEvent(new ActionEvent());
            } else {
                button.fire();
            }
            require(dialogs.getChildren().size() == previousCount + 2, "Missing conversation bubbles");
            require(input.getText().isEmpty(), "Composer did not clear after submission");
            String response = ((Label) lastDialog().lookup("#dialog")).getText();
            require(response.contains(expected), "Unexpected response for " + command + ": " + response);
            System.out.println("PASS control action: " + command);
            return response;
        });
    }

    /**
     * Returns the latest dialog on the JavaFX application thread.
     *
     * @return last conversation bubble.
     */
    private static Node lastDialog() {
        VBox dialogs = (VBox) stage.getScene().lookup("#dialogContainer");
        return dialogs.getChildren().getLast();
    }

    /**
     * Captures the visible window and a scene snapshot for rendering diagnosis.
     *
     * @param phase output filename prefix.
     * @throws Exception if screenshot capture fails.
     */
    private static void captureWindow(String phase) throws Exception {
        onFx(() -> {
            stage.getScene().getRoot().applyCss();
            stage.getScene().getRoot().layout();
            WritableImage screen = new Robot().getScreenCapture(null,
                    stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight(), false);
            saveImage(screen, Path.of(phase + ".png"));
            saveImage(stage.getScene().snapshot(null), Path.of(phase + "-scene.png"));
            return null;
        });
    }

    /**
     * Writes screenshot pixels to PNG without requiring the optional javafx-swing module.
     *
     * @param image captured image.
     * @param path destination file.
     * @throws IOException if the PNG cannot be written.
     */
    private static void saveImage(WritableImage image, Path path) throws IOException {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getPixelReader().getArgb(x, y);
                output.setRGB(x, y, pixel);
                colors.add(pixel);
            }
        }
        require(colors.size() > 20, "Blank or unrendered screenshot: " + path);
        require(ImageIO.write(output, "png", path.toFile()), "PNG writer unavailable");
    }

    /**
     * Executes one operation on the GUI thread with bounded error propagation.
     *
     * @param action operation that may read or manipulate JavaFX controls.
     * @param <T> result type.
     * @return result produced on the JavaFX thread.
     * @throws Exception if execution fails or times out.
     */
    private static <T> T onFx(Callable<T> action) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(action.call());
            } catch (Throwable failure) {
                result.completeExceptionally(failure);
            }
        });
        return result.get(10, TimeUnit.SECONDS);
    }

    /**
     * Fails a smoke check when the required condition is false.
     *
     * @param condition required condition.
     * @param message explanation shown on failure.
     */
    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
