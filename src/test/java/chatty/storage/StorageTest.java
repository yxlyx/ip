package chatty.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import chatty.exception.ChattyException;
import chatty.task.Deadline;
import chatty.task.Event;
import chatty.task.Task;
import chatty.task.Todo;

/**
 * Tests saving and restoring tasks through Chatty's text-file format.
 */
public class StorageTest {
    /** Temporary directory used for isolated storage-file tests. */
    @TempDir
    private Path tempDirectory;

    /**
     * Verifies that loading a missing data file returns an empty list.
     *
     * @throws ChattyException if loading the missing file fails unexpectedly.
     */
    @Test
    public void loadTasks_missingFile_emptyListReturned() throws ChattyException {
        Storage storage = new Storage(tempDirectory.resolve("data/chatty.txt"));

        assertTrue(storage.loadTasks().isEmpty());
    }

    /**
     * Verifies that saving and loading preserves all supported task data and status.
     *
     * @throws ChattyException if task storage fails unexpectedly.
     * @throws IOException if reading the saved file fails unexpectedly.
     */
    @Test
    public void saveAndLoadTasks_multipleTaskTypes_allDataAndStatusPreserved()
            throws ChattyException, IOException {
        Path filePath = tempDirectory.resolve("data/chatty.txt");
        Storage storage = new Storage(filePath);
        Todo todo = new Todo("read book");
        todo.markAsDone();
        Deadline deadline = new Deadline("submit report", LocalDate.of(2026, 10, 15));
        Event event = new Event("project meeting", "2pm", "4pm");
        event.markAsDone();

        storage.saveTasks(List.of(todo, deadline, event));

        assertEquals(List.of(
                "T | 1 | read book",
                "D | 0 | submit report | 2026-10-15",
                "E | 1 | project meeting | 2pm | 4pm"),
                Files.readAllLines(filePath, StandardCharsets.UTF_8));

        List<Task> loadedTasks = storage.loadTasks();
        Todo loadedTodo = assertInstanceOf(Todo.class, loadedTasks.get(0));
        Deadline loadedDeadline = assertInstanceOf(Deadline.class, loadedTasks.get(1));
        Event loadedEvent = assertInstanceOf(Event.class, loadedTasks.get(2));

        assertEquals(3, loadedTasks.size());
        assertEquals("read book", loadedTodo.getDescription());
        assertTrue(loadedTodo.isDone());
        assertEquals("submit report", loadedDeadline.getDescription());
        assertEquals(LocalDate.of(2026, 10, 15), loadedDeadline.getBy());
        assertFalse(loadedDeadline.isDone());
        assertEquals("project meeting", loadedEvent.getDescription());
        assertEquals("2pm", loadedEvent.getFrom());
        assertEquals("4pm", loadedEvent.getTo());
        assertTrue(loadedEvent.isDone());
    }

    /**
     * Verifies that blank lines in a data file are ignored.
     *
     * @throws ChattyException if loading the valid data fails unexpectedly.
     * @throws IOException if creating the test data file fails unexpectedly.
     */
    @Test
    public void loadTasks_blankLines_blankLinesIgnored() throws ChattyException, IOException {
        Path filePath = tempDirectory.resolve("chatty.txt");
        Files.write(filePath, List.of("", "   ", "T | 0 | read book"), StandardCharsets.UTF_8);
        Storage storage = new Storage(filePath);

        List<Task> loadedTasks = storage.loadTasks();

        assertEquals(1, loadedTasks.size());
        assertEquals("read book", loadedTasks.get(0).getDescription());
    }

    /**
     * Verifies that malformed records identify their one-based line number.
     *
     * @throws IOException if creating the malformed test data file fails unexpectedly.
     */
    @Test
    public void loadTasks_malformedRecord_exceptionIdentifiesLine() throws IOException {
        Path filePath = tempDirectory.resolve("chatty.txt");
        Files.write(filePath, List.of(
                "T | 0 | read book",
                "D | 0 | submit report | 2026-02-30"), StandardCharsets.UTF_8);
        Storage storage = new Storage(filePath);

        ChattyException exception = assertThrows(ChattyException.class, storage::loadTasks);

        assertEquals("OOPS!!! The data file is corrupted at line 2.", exception.getMessage());
    }
}
