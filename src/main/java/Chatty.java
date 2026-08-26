import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
        CommandType command = CommandType.fromInput(input);
        try {
            switch (command) {
            case BYE:
                ui.showExit();
                return false;
            case LIST:
                ui.showTaskList(tasks);
                break;
            case MARK:
                markTask(input, tasks, ui);
                storage.saveTasks(tasks);
                break;
            case UNMARK:
                unmarkTask(input, tasks, ui);
                storage.saveTasks(tasks);
                break;
            case DELETE:
                deleteTask(input, tasks, ui);
                storage.saveTasks(tasks);
                break;
            case TODO:
                addTodo(input, tasks, ui);
                storage.saveTasks(tasks);
                break;
            case DEADLINE:
                addDeadline(input, tasks, ui);
                storage.saveTasks(tasks);
                break;
            case EVENT:
                addEvent(input, tasks, ui);
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

    /** Adds a todo described by the text after the {@code todo} command. */
    private static void addTodo(String input, List<Task> tasks, Ui ui) throws ChattyException {
        String description = input.substring("todo".length()).strip();
        requireDescription(description, "todo");
        addTask(new Todo(description), tasks, ui);
    }

    /** Adds a deadline using the description and text after the {@code /by} delimiter. */
    private static void addDeadline(String input, List<Task> tasks, Ui ui) throws ChattyException {
        String details = input.substring("deadline".length()).strip();
        int byIndex = details.indexOf("/by");
        if (byIndex < 0) {
            throw new ChattyException("OOPS!!! A deadline needs '/by'. "
                    + "Try: deadline DESCRIPTION /by DATE_OR_TIME");
        }

        String description = details.substring(0, byIndex).strip();
        String byText = details.substring(byIndex + "/by".length()).strip();
        requireDescription(description, "deadline");
        if (byText.isEmpty()) {
            throw new ChattyException("OOPS!!! Tell me when the deadline is due after '/by'.");
        }

        try {
            LocalDate by = LocalDate.parse(byText);
            addTask(new Deadline(description, by), tasks, ui);
        } catch (DateTimeParseException exception) {
            throw new ChattyException("OOPS!!! Use YYYY-MM-DD for deadline dates, such as 2019-10-15.");
        }
    }

    /** Adds an event using the description and text after its time delimiters. */
    private static void addEvent(String input, List<Task> tasks, Ui ui) throws ChattyException {
        String details = input.substring("event".length()).strip();
        int fromIndex = details.indexOf("/from");
        if (fromIndex < 0) {
            throw new ChattyException("OOPS!!! An event needs '/from' and '/to'. "
                    + "Try: event DESCRIPTION /from START /to END");
        }

        int toIndex = details.indexOf("/to", fromIndex + "/from".length());
        if (toIndex < 0) {
            throw new ChattyException("OOPS!!! An event with '/from' also needs an ending value after '/to'.");
        }

        String description = details.substring(0, fromIndex).strip();
        String from = details.substring(fromIndex + "/from".length(), toIndex).strip();
        String to = details.substring(toIndex + "/to".length()).strip();
        requireDescription(description, "event");
        if (from.isEmpty()) {
            throw new ChattyException("OOPS!!! Tell me when the event starts after '/from'.");
        } else if (to.isEmpty()) {
            throw new ChattyException("OOPS!!! Tell me when the event ends after '/to'.");
        }
        addTask(new Event(description, from, to), tasks, ui);
    }

    /** Throws a specific error when a task description is empty. */
    private static void requireDescription(String description, String taskType) throws ChattyException {
        if (description.isEmpty()) {
            String article = taskType.equals("event") ? "an" : "a";
            throw new ChattyException("OOPS!!! The description of " + article + " "
                    + taskType + " cannot be empty.");
        }
    }

    /** Adds a task and prints its details and the updated task count. */
    private static void addTask(Task task, List<Task> tasks, Ui ui) {
        tasks.add(task);
        ui.showTaskAdded(task, tasks.size());
    }

    /** Marks the task identified by a one-based task number as done. */
    private static void markTask(String input, List<Task> tasks, Ui ui) throws ChattyException {
        Task task = getTask(input, "mark", tasks);
        task.markAsDone();
        ui.showTaskMarked(task);
    }

    /** Marks the task identified by a one-based task number as not done. */
    private static void unmarkTask(String input, List<Task> tasks, Ui ui) throws ChattyException {
        Task task = getTask(input, "unmark", tasks);
        task.markAsNotDone();
        ui.showTaskUnmarked(task);
    }

    /** Deletes the task identified by a one-based task number. */
    private static void deleteTask(String input, List<Task> tasks, Ui ui) throws ChattyException {
        Task task = getTask(input, "delete", tasks);
        tasks.remove(task);
        ui.showTaskDeleted(task, tasks.size());
    }

    /** Returns the task selected by a command containing a one-based task number. */
    private static Task getTask(String input, String command, List<Task> tasks) throws ChattyException {
        String indexText = input.substring(command.length()).strip();
        if (indexText.isEmpty()) {
            throw new ChattyException("OOPS!!! Tell me which task to " + command + ".");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(indexText);
        } catch (NumberFormatException exception) {
            throw new ChattyException("OOPS!!! The task number must be a whole number.");
        }

        if (tasks.isEmpty()) {
            throw new ChattyException("OOPS!!! Your task list is empty.");
        } else if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new ChattyException("OOPS!!! Task " + taskNumber + " does not exist. "
                    + "Choose a number from 1 to " + tasks.size() + ".");
        }
        return tasks.get(taskNumber - 1);
    }
}
