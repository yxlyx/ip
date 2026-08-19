/**
 * Starts the Chatty chatbot application.
 */
public class Chatty {
    /**
     * Runs Chatty and prints its greeting and farewell.
     *
     * @param args command-line arguments; not used by Chatty
     */
    public static void main(String[] args) {
        String horizontalLine = "____________________________________________________________";
        String banner = "  ____ _           _   _         \n"
                + " / ___| |__   __ _| |_| |_ _   _ \n"
                + "| |   | '_ \\ / _` | __| __| | | |\n"
                + "| |___| | | | (_| | |_| |_| |_| |\n"
                + " \\____|_| |_|\\__,_|\\__|\\__|\\__, |\n"
                + "                           |___/";
        String welcome = "Hello! I'm Chatty.\nWhat can I do for you?";

        System.out.println(horizontalLine);
        System.out.println(banner);
        System.out.println(welcome);
        System.out.println(horizontalLine);
        System.out.println("Bye. Hope to see you again soon!");
    }
}
