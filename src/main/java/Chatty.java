import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Runs the Chatty chatbot application.
 */
public class Chatty {
    /** Line used to separate Chatty's responses from user input. */
    private static final String HORIZONTAL_LINE = "____________________________________________________________";

    /** Chatty's logo, displayed when the application starts. */
    private static final String BANNER = "  ____ _           _   _         \n"
            + " / ___| |__   __ _| |_| |_ _   _ \n"
            + "| |   | '_ \\ / _` | __| __| | | |\n"
            + "| |___| | | | (_| | |_| |_| |_| |\n"
            + " \\____|_| |_|\\__,_|\\__|\\__|\\__, |\n"
            + "                           |___/";

    /** Greeting displayed after Chatty's logo. */
    private static final String WELCOME = "Hello! I'm Chatty.\nWhat can I do for you?";

    /** Prevents instantiation of this application entry-point class. */
    private Chatty() {
    }

    /**
     * Runs Chatty until input ends or the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by Chatty
     */
    public static void main(String[] args) {
        printGreeting();
        Storage storage = new Storage(Path.of("data", "chatty.txt"));
        List<Task> tasks = loadTasks(storage);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine() && processCommand(scanner.nextLine(), tasks, storage)) {
            // Continue processing commands until the input ends or the user exits.
        }
    }

    /** Prints Chatty's startup banner and greeting. */
    private static void printGreeting() {
        System.out.println(HORIZONTAL_LINE);
        System.out.println(BANNER);
        System.out.println(WELCOME);
        System.out.println(HORIZONTAL_LINE);
    }

    /** Loads saved tasks, or starts with an empty list if loading fails. */
    private static List<Task> loadTasks(Storage storage) {
        try {
            return storage.loadTasks();
        } catch (ChattyException exception) {
            System.out.println(" " + exception.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Processes one user command and prints Chatty's response.
     *
     * @param rawInput command entered by the user
     * @param tasks tasks stored during this Chatty session
     * @param storage storage used to save task changes
     * @return false if Chatty should exit, and true otherwise
     */
    private static boolean processCommand(String rawInput, List<Task> tasks, Storage storage) {
        String input = rawInput.strip();
        System.out.println(HORIZONTAL_LINE);
        CommandType command = CommandType.fromInput(input);
        try {
            switch (command) {
            case BYE:
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(HORIZONTAL_LINE);
                return false;
            case LIST:
                printTaskList(tasks);
                break;
            case MARK:
                markTask(input, tasks);
                storage.saveTasks(tasks);
                break;
            case UNMARK:
                unmarkTask(input, tasks);
                storage.saveTasks(tasks);
                break;
            case DELETE:
                deleteTask(input, tasks);
                storage.saveTasks(tasks);
                break;
            case TODO:
                addTodo(input, tasks);
                storage.saveTasks(tasks);
                break;
            case DEADLINE:
                addDeadline(input, tasks);
                storage.saveTasks(tasks);
                break;
            case EVENT:
                addEvent(input, tasks);
                storage.saveTasks(tasks);
                break;
            case UNKNOWN:
            default:
                throw new ChattyException("OOPS!!! I don't recognise that command. "
                        + "Try todo, deadline, event, list, mark, unmark, delete, or bye.");
            }
        } catch (ChattyException exception) {
            System.out.println(" " + exception.getMessage());
        }
        System.out.println(HORIZONTAL_LINE);
        return true;
    }

    /** Adds a todo described by the text after the {@code todo} command. */
    private static void addTodo(String input, List<Task> tasks) throws ChattyException {
        String description = input.substring("todo".length()).strip();
        requireDescription(description, "todo");
        addTask(new Todo(description), tasks);
    }

    /** Adds a deadline using the description and text after the {@code /by} delimiter. */
    private static void addDeadline(String input, List<Task> tasks) throws ChattyException {
        String details = input.substring("deadline".length()).strip();
        int byIndex = details.indexOf("/by");
        if (byIndex < 0) {
            throw new ChattyException("OOPS!!! A deadline needs '/by'. "
                    + "Try: deadline DESCRIPTION /by DATE_OR_TIME");
        }

        String description = details.substring(0, byIndex).strip();
        String by = details.substring(byIndex + "/by".length()).strip();
        requireDescription(description, "deadline");
        if (by.isEmpty()) {
            throw new ChattyException("OOPS!!! Tell me when the deadline is due after '/by'.");
        }
        addTask(new Deadline(description, by), tasks);
    }

    /** Adds an event using the description and text after its time delimiters. */
    private static void addEvent(String input, List<Task> tasks) throws ChattyException {
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
        addTask(new Event(description, from, to), tasks);
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
    private static void addTask(Task task, List<Task> tasks) {
        tasks.add(task);
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
    }

    /** Prints all tasks with their numbers and completion statuses. */
    private static void printTaskList(List<Task> tasks) {
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
    }

    /** Marks the task identified by a one-based task number as done. */
    private static void markTask(String input, List<Task> tasks) throws ChattyException {
        Task task = getTask(input, "mark", tasks);
        task.markAsDone();
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
    }

    /** Marks the task identified by a one-based task number as not done. */
    private static void unmarkTask(String input, List<Task> tasks) throws ChattyException {
        Task task = getTask(input, "unmark", tasks);
        task.markAsNotDone();
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
    }

    /** Deletes the task identified by a one-based task number. */
    private static void deleteTask(String input, List<Task> tasks) throws ChattyException {
        Task task = getTask(input, "delete", tasks);
        tasks.remove(task);
        System.out.println(" Noted. I've removed this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
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
