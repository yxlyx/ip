package chatty;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(markedResponse.contains("completed this occurrence"));
        assertTrue(markedResponse.contains("on: Sep 21 2026"));
        assertTrue(listResponse.contains("on: Sep 21 2026"));
        assertTrue(unmarkResponse.contains("cannot be unmarked"));
    }
}
