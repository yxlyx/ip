import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs the Chatty chatbot application.
 */
public class Chatty {
    /** Prevents instantiation of this application entry-point class. */
    private Chatty() {
    }

    /**
     * Runs Chatty until input ends or the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by Chatty
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showGreeting();
        Storage storage = new Storage(Path.of("data", "chatty.txt"));
        List<Task> tasks = loadTasks(storage, ui);
        while (ui.hasNextCommand() && processCommand(ui.readCommand(), tasks, storage, ui)) {
            // Continue processing commands until the input ends or the user exits.
        }
    }

    /** Loads saved tasks, or starts with an empty list if loading fails. */
    private static List<Task> loadTasks(Storage storage, Ui ui) {
        try {
            return storage.loadTasks();
        } catch (ChattyException exception) {
            ui.showError(exception.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Processes one user command and prints Chatty's response.
     *
     * @param rawInput command entered by the user
     * @param tasks tasks stored during this Chatty session
     * @param storage storage used to save task changes
     * @param ui user interface used to display responses
     * @return false if Chatty should exit, and true otherwise
     */
    private static boolean processCommand(String rawInput, List<Task> tasks, Storage storage, Ui ui) {
        String input = rawInput.strip();
        ui.showLine();
        CommandType command = Parser.parseCommand(input);
        try {
            switch (command) {
            case BYE:
                ui.showExit();
                return false;
            case LIST:
                ui.showTaskList(tasks);
                break;
            case MARK:
                markTask(Parser.parseTaskNumber(input, command), tasks, ui);
                storage.saveTasks(tasks);
                break;
            case UNMARK:
                unmarkTask(Parser.parseTaskNumber(input, command), tasks, ui);
                storage.saveTasks(tasks);
                break;
            case DELETE:
                deleteTask(Parser.parseTaskNumber(input, command), tasks, ui);
                storage.saveTasks(tasks);
                break;
            case TODO:
            case DEADLINE:
            case EVENT:
                addTask(Parser.parseTask(input, command), tasks, ui);
                storage.saveTasks(tasks);
                break;
            case UNKNOWN:
            default:
                throw new ChattyException("OOPS!!! I don't recognise that command. "
                        + "Try todo, deadline, event, list, mark, unmark, delete, or bye.");
            }
        } catch (ChattyException exception) {
            ui.showError(exception.getMessage());
        }
        ui.showLine();
        return true;
    }

    /** Adds a task and prints its details and the updated task count. */
    private static void addTask(Task task, List<Task> tasks, Ui ui) {
        tasks.add(task);
        ui.showTaskAdded(task, tasks.size());
    }

    /** Marks the task identified by a one-based task number as done. */
    private static void markTask(int taskNumber, List<Task> tasks, Ui ui) throws ChattyException {
        Task task = getTask(taskNumber, tasks);
        task.markAsDone();
        ui.showTaskMarked(task);
    }

    /** Marks the task identified by a one-based task number as not done. */
    private static void unmarkTask(int taskNumber, List<Task> tasks, Ui ui) throws ChattyException {
        Task task = getTask(taskNumber, tasks);
        task.markAsNotDone();
        ui.showTaskUnmarked(task);
    }

    /** Deletes the task identified by a one-based task number. */
    private static void deleteTask(int taskNumber, List<Task> tasks, Ui ui) throws ChattyException {
        Task task = getTask(taskNumber, tasks);
        tasks.remove(task);
        ui.showTaskDeleted(task, tasks.size());
    }

    /** Returns the task selected by its one-based task number. */
    private static Task getTask(int taskNumber, List<Task> tasks) throws ChattyException {
        if (tasks.isEmpty()) {
            throw new ChattyException("OOPS!!! Your task list is empty.");
        } else if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new ChattyException("OOPS!!! Task " + taskNumber + " does not exist. "
                    + "Choose a number from 1 to " + tasks.size() + ".");
        }
        return tasks.get(taskNumber - 1);
    }
}
