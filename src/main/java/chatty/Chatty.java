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
        tasks = loadTasks();
    }

    /** Runs Chatty until input ends or the user enters {@code bye}. */
    public void run() {
        ui.showGreeting();
        while (ui.hasNextCommand()) {
            String input = ui.readCommand();
            ui.showLine();
            ui.showResponse(getResponse(input));
            ui.showLine();
            if (shouldExit(input)) {
                return;
            }
        }
    }

    /**
     * Loads saved tasks, or starts with an empty list if loading fails.
     *
     * @return loaded tasks, or an empty task list when loading fails.
     */
    private TaskList loadTasks() {
        try {
            return new TaskList(storage.loadTasks());
        } catch (ChattyException exception) {
            ui.showResponse(ui.formatError(exception.getMessage()));
            return new TaskList();
        }
    }

    /**
     * Processes one user command and returns the response for any user interface.
     *
     * @param rawInput command entered by the user.
     * @return response produced after processing the command.
     */
    public String getResponse(String rawInput) {
        String input = rawInput.strip();
        CommandType command = Parser.parseCommand(input);
        try {
            switch (command) {
                case BYE:
                    return ui.formatExit();
                case LIST:
                    return ui.formatTaskList(tasks.getTasks());
                case FIND:
                    return ui.formatMatchingTasks(tasks.find(Parser.parseFindKeyword(input)));
                case MARK:
                    Task markedTask = tasks.mark(Parser.parseTaskNumber(input, command));
                    storage.saveTasks(tasks.getTasks());
                    return ui.formatTaskMarked(markedTask);
                case UNMARK:
                    Task unmarkedTask = tasks.unmark(Parser.parseTaskNumber(input, command));
                    storage.saveTasks(tasks.getTasks());
                    return ui.formatTaskUnmarked(unmarkedTask);
                case DELETE:
                    Task deletedTask = tasks.delete(Parser.parseTaskNumber(input, command));
                    storage.saveTasks(tasks.getTasks());
                    return ui.formatTaskDeleted(deletedTask, tasks.size());
                case TODO:
                    // Fallthrough
                case DEADLINE:
                    // Fallthrough
                case EVENT:
                    Task addedTask = Parser.parseTask(input, command);
                    tasks.add(addedTask);
                    storage.saveTasks(tasks.getTasks());
                    return ui.formatTaskAdded(addedTask, tasks.size());
                default:
                    throw new ChattyException("OOPS!!! I don't recognise that command. "
                            + "Try todo, deadline, event, list, find, mark, unmark, delete, or bye.");
            }
        } catch (ChattyException exception) {
            return ui.formatError(exception.getMessage());
        }
    }

    /**
     * Returns whether the supplied input is the command that closes Chatty.
     *
     * @param rawInput command entered by the user.
     * @return {@code true} when the normalized command is {@code bye}.
     */
    public boolean shouldExit(String rawInput) {
        return Parser.parseCommand(rawInput.strip()) == CommandType.BYE;
    }

    /**
     * Starts Chatty using its default relative data-file path.
     *
     * @param args command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        new Chatty(Path.of("data", "chatty.txt")).run();
    }
}
