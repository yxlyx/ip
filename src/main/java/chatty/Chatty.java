package chatty;

import java.nio.file.Path;

import chatty.exception.ChattyException;
import chatty.parser.CommandType;
import chatty.parser.Parser;
import chatty.storage.Storage;
import chatty.task.Task;
import chatty.task.TaskList;
import chatty.ui.Ui;

/**
 * Coordinates Chatty's user interface, command parsing, task list, and storage.
 */
public class Chatty {
    /** User interface used to read commands and display responses. */
    private final Ui ui;

    /** Storage used to load and save tasks. */
    private final Storage storage;

    /** Tasks managed during the current session. */
    private TaskList tasks;

    /**
     * Creates Chatty with a task data file at the given path.
     *
     * @param filePath relative path of the task data file.
     */
    public Chatty(Path filePath) {
        ui = new Ui();
        storage = new Storage(filePath);
        tasks = new TaskList();
    }

    /** Runs Chatty until input ends or the user enters {@code bye}. */
    public void run() {
        ui.showGreeting();
        tasks = loadTasks();
        while (ui.hasNextCommand() && processCommand(ui.readCommand())) {
            // Continue processing commands until the input ends or the user exits.
        }
    }

    /** Loads saved tasks, or starts with an empty list if loading fails. */
    private TaskList loadTasks() {
        try {
            return new TaskList(storage.loadTasks());
        } catch (ChattyException exception) {
            ui.showError(exception.getMessage());
            return new TaskList();
        }
    }

    /**
     * Processes one user command and displays Chatty's response.
     *
     * @param rawInput command entered by the user.
     * @return false if Chatty should exit, and true otherwise
     */
    private boolean processCommand(String rawInput) {
        String input = rawInput.strip();
        ui.showLine();
        CommandType command = Parser.parseCommand(input);
        try {
            switch (command) {
                case BYE:
                    ui.showExit();
                    return false;
                case LIST:
                    ui.showTaskList(tasks.getTasks());
                    break;
                case MARK:
                    Task markedTask = tasks.mark(Parser.parseTaskNumber(input, command));
                    ui.showTaskMarked(markedTask);
                    storage.saveTasks(tasks.getTasks());
                    break;
                case UNMARK:
                    Task unmarkedTask = tasks.unmark(Parser.parseTaskNumber(input, command));
                    ui.showTaskUnmarked(unmarkedTask);
                    storage.saveTasks(tasks.getTasks());
                    break;
                case DELETE:
                    Task deletedTask = tasks.delete(Parser.parseTaskNumber(input, command));
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    storage.saveTasks(tasks.getTasks());
                    break;
                case TODO:
                    // Fallthrough
                case DEADLINE:
                    // Fallthrough
                case EVENT:
                    Task addedTask = Parser.parseTask(input, command);
                    tasks.add(addedTask);
                    ui.showTaskAdded(addedTask, tasks.size());
                    storage.saveTasks(tasks.getTasks());
                    break;
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

    /** Starts Chatty using its default relative data-file path. */
    public static void main(String[] args) {
        new Chatty(Path.of("data", "chatty.txt")).run();
    }
}
