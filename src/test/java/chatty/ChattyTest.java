package chatty;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests commands that require coordination between parsing, tasks, responses, and storage.
 */
public class ChattyTest {
    /** Temporary directory used for isolated command tests. */
    @TempDir
    private Path tempDirectory;

    /** Verifies command normalization, exit detection, and unknown-command handling. */
    @Test
    public void basicCommands_whitespaceAndUnknownInput_handledCorrectly() {
        Chatty chatty = new Chatty(tempDirectory.resolve("chatty.txt"));

        assertTrue(chatty.shouldExit("  bye  "));
        assertTrue(!chatty.shouldExit("bye now"));
        assertTrue(chatty.getResponse("unknown command").contains("don't recognise"));
        assertTrue(chatty.getResponse("   list   ").equals(" Flight plan status:"));
    }

    /**
     * Verifies add, find, mark, unmark, delete, and reload behavior across commands.
     */
    @Test
    public void getResponse_standardTaskCommands_mutationsPersistAcrossReload() {
        Path filePath = tempDirectory.resolve("chatty.txt");
        Chatty chatty = new Chatty(filePath);

        assertTrue(chatty.getResponse("todo read book").contains("[T][ ] read book"));
        assertTrue(chatty.getResponse("deadline submit report /by 2026-10-15")
                .contains("[D][ ] submit report"));
        assertTrue(chatty.getResponse("event meeting /from 2pm /to 4pm")
                .contains("[E][ ] meeting"));
        assertTrue(chatty.getResponse("find report").contains("submit report"));
        assertTrue(chatty.getResponse("mark 1").contains("[T][X] read book"));
        assertTrue(chatty.getResponse("unmark 1").contains("[T][ ] read book"));
        assertTrue(chatty.getResponse("delete 2").contains("submit report"));

        Chatty reloadedChatty = new Chatty(filePath);
        String reloadedList = reloadedChatty.getResponse("list");
        assertTrue(reloadedList.contains("read book"));
        assertTrue(reloadedList.contains("meeting"));
        assertTrue(!reloadedList.contains("submit report"));
    }

    /**
     * Verifies that a failed save restores the last task list held in storage.
     *
     * @throws IOException if the blocking parent file cannot be created.
     */
    @Test
    public void getResponse_saveFails_unsavedTaskRemovedFromMemory() throws IOException {
        Path blockingParent = tempDirectory.resolve("not-a-directory");
        Files.writeString(blockingParent, "blocking file", StandardCharsets.UTF_8);
        Chatty chatty = new Chatty(blockingParent.resolve("chatty.txt"));

        String addResponse = chatty.getResponse("todo read book");
        String listResponse = chatty.getResponse("list");

        assertTrue(addResponse.contains("couldn't save your tasks"));
        assertTrue(listResponse.equals(" Flight plan status:"));
    }

    /** Verifies the recurring-task add, mark, list, and unmark command flow. */
    @Test
    public void getResponse_recurringTaskCommands_scheduleManagedAcrossCommands() {
        Chatty chatty = new Chatty(tempDirectory.resolve("chatty.txt"));

        String addedResponse = chatty.getResponse(
                "recurring project meeting /on 2026-09-14 /every 1 week");
        String markedResponse = chatty.getResponse("mark 1");
        String listResponse = chatty.getResponse("list");
        String unmarkResponse = chatty.getResponse("unmark 1");

        assertTrue(addedResponse.contains(
                "[R][ ] project meeting (on: Sep 14 2026, every: 1 week)"));
        assertTrue(markedResponse.contains("Recurring milestone cleared"));
        assertTrue(markedResponse.contains("on: Sep 21 2026"));
        assertTrue(listResponse.contains("on: Sep 21 2026"));
        assertTrue(unmarkResponse.contains("cannot be unmarked"));
    }
}
