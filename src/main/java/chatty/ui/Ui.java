package chatty.ui;

import java.util.List;
import java.util.Scanner;

import chatty.task.Task;

/**
 * Handles console input and presents Chatty's responses to the user.
 */
public class Ui {
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

    /** Scanner used to read commands from standard input. */
    private final Scanner scanner;

    /** Creates a user interface that reads commands from standard input. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /** Returns whether another command is available. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Returns the next command entered by the user. */
    public String readCommand() {
        return scanner.nextLine();
    }

    /** Displays Chatty's startup banner and greeting. */
    public void showGreeting() {
        showLine();
        System.out.println(BANNER);
        System.out.println(WELCOME);
        showLine();
    }

    /** Displays the line that separates responses from user input. */
    public void showLine() {
        System.out.println(HORIZONTAL_LINE);
    }

    /** Displays Chatty's farewell response and its closing separator. */
    public void showExit() {
        System.out.println(" Bye. Hope to see you again soon!");
        showLine();
    }

    /** Displays an error message to the user. */
    public void showError(String message) {
        System.out.println(" " + message);
    }

    /** Displays every task with its one-based list number. */
    public void showTaskList(List<Task> tasks) {
        System.out.println(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            System.out.println(" " + (i + 1) + "." + tasks.get(i));
        }
    }

    /** Displays confirmation that a task was added. */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println(" Got it. I've added this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + taskCount + " tasks in the list.");
    }

    /** Displays confirmation that a task was marked as done. */
    public void showTaskMarked(Task task) {
        System.out.println(" Nice! I've marked this task as done:");
        System.out.println("   " + task);
    }

    /** Displays confirmation that a task was marked as not done. */
    public void showTaskUnmarked(Task task) {
        System.out.println(" OK, I've marked this task as not done yet:");
        System.out.println("   " + task);
    }

    /** Displays confirmation that a task was deleted. */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println(" Noted. I've removed this task:");
        System.out.println("   " + task);
        System.out.println(" Now you have " + taskCount + " tasks in the list.");
    }
}
