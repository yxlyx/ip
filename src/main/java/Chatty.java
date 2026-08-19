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
        List<Task> tasks = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine() && processCommand(scanner.nextLine(), tasks)) {
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

    /**
     * Processes one user command and prints Chatty's response.
     *
     * @param input command entered by the user
     * @param tasks tasks stored during this Chatty session
     * @return false if Chatty should exit, and true otherwise
     */
    private static boolean processCommand(String input, List<Task> tasks) {
        System.out.println(HORIZONTAL_LINE);
        if (input.equals("bye")) {
            System.out.println(" Bye. Hope to see you again soon!");
            System.out.println(HORIZONTAL_LINE);
            return false;
        } else if (input.equals("list")) {
            printTaskList(tasks);
        } else if (input.startsWith("mark ")) {
            markTask(input, tasks);
        } else if (input.startsWith("unmark ")) {
            unmarkTask(input, tasks);
        } else if (input.startsWith("todo ")) {
            addTodo(input, tasks);
        } else if (input.startsWith("deadline ")) {
            addDeadline(input, tasks);
        } else if (input.startsWith("event ")) {
            addEvent(input, tasks);
        } else {
            addTask(new Todo(input), tasks);
        }
        System.out.println(HORIZONTAL_LINE);
        return true;
    }

    /** Adds a todo described by the text after the {@code todo} command. */
    private static void addTodo(String input, List<Task> tasks) {
        String description = input.substring("todo ".length());
        addTask(new Todo(description), tasks);
    }

    /** Adds a deadline using the description and text after the {@code /by} delimiter. */
    private static void addDeadline(String input, List<Task> tasks) {
        String details = input.substring("deadline ".length());
        int byIndex = details.indexOf(" /by ");
        String description = details.substring(0, byIndex);
        String by = details.substring(byIndex + " /by ".length());
        addTask(new Deadline(description, by), tasks);
    }

    /** Adds an event using the description and text after its time delimiters. */
    private static void addEvent(String input, List<Task> tasks) {
        String details = input.substring("event ".length());
        int fromIndex = details.indexOf(" /from ");
        int toIndex = details.indexOf(" /to ", fromIndex + " /from ".length());
        String description = details.substring(0, fromIndex);
        String from = details.substring(fromIndex + " /from ".length(), toIndex);
        String to = details.substring(toIndex + " /to ".length());
        addTask(new Event(description, from, to), tasks);
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
    private static void markTask(String input, List<Task> tasks) {
        int taskIndex = Integer.parseInt(input.substring("mark ".length())) - 1;
        Task task = tasks.get(taskIndex);
        task.markAsDone();
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
    }

    /** Marks the task identified by a one-based task number as not done. */
    private static void unmarkTask(String input, List<Task> tasks) {
        int taskIndex = Integer.parseInt(input.substring("unmark ".length())) - 1;
        Task task = tasks.get(taskIndex);
        task.markAsNotDone();
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
    }
}
