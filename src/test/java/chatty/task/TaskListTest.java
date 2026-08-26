package chatty.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import chatty.exception.ChattyException;

/**
 * Tests task collection mutations and one-based task selection.
 */
public class TaskListTest {
    @Test
    public void constructor_suppliedList_copiesTasksAndExposesReadOnlyView() {
        Todo originalTask = new Todo("read book");
        List<Task> suppliedTasks = new ArrayList<>(List.of(originalTask));
        TaskList taskList = new TaskList(suppliedTasks);

        suppliedTasks.clear();

        assertEquals(1, taskList.size());
        assertSame(originalTask, taskList.getTasks().get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> taskList.getTasks().add(new Todo("write notes")));
    }

    @Test
    public void markAndUnmark_validTaskNumber_completionStateChanged() throws ChattyException {
        Todo firstTask = new Todo("read book");
        Todo secondTask = new Todo("write notes");
        TaskList taskList = new TaskList(List.of(firstTask, secondTask));

        Task markedTask = taskList.mark(2);

        assertSame(secondTask, markedTask);
        assertFalse(firstTask.isDone());
        assertTrue(secondTask.isDone());

        Task unmarkedTask = taskList.unmark(2);

        assertSame(secondTask, unmarkedTask);
        assertFalse(secondTask.isDone());
    }

    @Test
    public void delete_validTaskNumber_selectedTaskRemovedAndReturned() throws ChattyException {
        Todo firstTask = new Todo("read book");
        Todo secondTask = new Todo("write notes");
        TaskList taskList = new TaskList(List.of(firstTask, secondTask));

        Task deletedTask = taskList.delete(1);

        assertSame(firstTask, deletedTask);
        assertEquals(List.of(secondTask), taskList.getTasks());
    }

    @Test
    public void taskOperations_emptyOrOutOfRangeTaskNumber_exceptionThrown() {
        TaskList emptyTaskList = new TaskList();
        ChattyException emptyListException = assertThrows(ChattyException.class,
                () -> emptyTaskList.mark(1));
        assertEquals("OOPS!!! Your task list is empty.", emptyListException.getMessage());

        TaskList taskList = new TaskList(List.of(new Todo("read book")));
        assertThrows(ChattyException.class, () -> taskList.mark(0));
        assertThrows(ChattyException.class, () -> taskList.unmark(2));
        assertThrows(ChattyException.class, () -> taskList.delete(-1));
    }
}
