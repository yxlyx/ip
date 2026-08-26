package chatty.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import chatty.exception.ChattyException;
import chatty.task.Deadline;
import chatty.task.Event;
import chatty.task.Task;
import chatty.task.Todo;

/**
 * Interprets user input as commands, tasks, and task numbers.
 */
public class Parser {
    /** Prevents instantiation of this command-parsing utility class. */
    private Parser() {
    }

    /**
     * Returns the command type represented by the normalized input.
     *
     * @param input normalized user input.
     * @return matching command type, or {@link CommandType#UNKNOWN} when no command matches.
     */
    public static CommandType parseCommand(String input) {
        for (CommandType command : CommandType.values()) {
            boolean isExactMatch = input.equals(command.getKeyword());
            boolean hasAcceptedArguments = command.acceptsArguments()
                    && input.startsWith(command.getKeyword() + " ");
            if (isExactMatch || hasAcceptedArguments) {
                return command;
            }
        }
        return CommandType.UNKNOWN;
    }

    /**
     * Creates a task from an add command and its arguments.
     *
     * @param input normalized user input.
     * @param command type of task to create.
     * @return task described by the command.
     * @throws ChattyException if required task details are missing or invalid.
     */
    public static Task parseTask(String input, CommandType command) throws ChattyException {
        switch (command) {
        case TODO:
            return parseTodo(input);
        case DEADLINE:
            return parseDeadline(input);
        case EVENT:
            return parseEvent(input);
        default:
            throw new ChattyException("OOPS!!! That command does not create a task.");
        }
    }

    /**
     * Returns the one-based task number supplied to a task operation.
     *
     * @param input normalized user input.
     * @param command task operation being parsed.
     * @return one-based task number.
     * @throws ChattyException if the task number is absent or not a whole number.
     */
    public static int parseTaskNumber(String input, CommandType command) throws ChattyException {
        String indexText = input.substring(command.getKeyword().length()).strip();
        if (indexText.isEmpty()) {
            throw new ChattyException("OOPS!!! Tell me which task to " + command.getKeyword() + ".");
        }

        try {
            return Integer.parseInt(indexText);
        } catch (NumberFormatException exception) {
            throw new ChattyException("OOPS!!! The task number must be a whole number.");
        }
    }

    /**
     * Creates a todo from its command arguments.
     *
     * @param input normalized todo command.
     * @return parsed todo.
     * @throws ChattyException if the todo description is empty.
     */
    private static Todo parseTodo(String input) throws ChattyException {
        String description = input.substring("todo".length()).strip();
        requireDescription(description, "todo");
        return new Todo(description);
    }

    /**
     * Creates a deadline from its description and ISO date.
     *
     * @param input normalized deadline command.
     * @return parsed deadline.
     * @throws ChattyException if the deadline details or date are invalid.
     */
    private static Deadline parseDeadline(String input) throws ChattyException {
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
            return new Deadline(description, LocalDate.parse(byText));
        } catch (DateTimeParseException exception) {
            throw new ChattyException("OOPS!!! Use YYYY-MM-DD for deadline dates, such as 2019-10-15.");
        }
    }

    /**
     * Creates an event from its description and time range.
     *
     * @param input normalized event command.
     * @return parsed event.
     * @throws ChattyException if the event details are incomplete.
     */
    private static Event parseEvent(String input) throws ChattyException {
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
        return new Event(description, from, to);
    }

    /**
     * Throws a specific error when a task description is empty.
     *
     * @param description task description to validate.
     * @param taskType task type used in the error message.
     * @throws ChattyException if the description is empty.
     */
    private static void requireDescription(String description, String taskType) throws ChattyException {
        if (description.isEmpty()) {
            String article = taskType.equals("event") ? "an" : "a";
            throw new ChattyException("OOPS!!! The description of " + article + " "
                    + taskType + " cannot be empty.");
        }
    }
}
