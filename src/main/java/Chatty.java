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
        } else {
            tasks.add(new Task(input));
            System.out.println(" added: " + input);
        }
        System.out.println(HORIZONTAL_LINE);
        return true;
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
