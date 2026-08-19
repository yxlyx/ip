/**
 * Identifies the commands understood by Chatty.
 */
public enum CommandType {
    /** Ends the current Chatty session. */
    BYE("bye", false),

    /** Displays all stored tasks. */
    LIST("list", false),

    /** Marks a selected task as done. */
    MARK("mark", true),

    /** Marks a selected task as not done. */
    UNMARK("unmark", true),

    /** Removes a selected task. */
    DELETE("delete", true),

    /** Adds a todo. */
    TODO("todo", true),

    /** Adds a deadline. */
    DEADLINE("deadline", true),

    /** Adds an event. */
    EVENT("event", true),

    /** Represents input that does not match a supported command. */
    UNKNOWN("", false);

    /** Keyword entered by the user to select this command. */
    private final String keyword;

    /** Whether the command can be followed by arguments. */
    private final boolean acceptsArguments;

    CommandType(String keyword, boolean acceptsArguments) {
        this.keyword = keyword;
        this.acceptsArguments = acceptsArguments;
    }

    /**
     * Returns the command type represented by the given input.
     *
     * @param input normalized user input
     * @return matching command type, or {@link #UNKNOWN} when no command matches
     */
    public static CommandType fromInput(String input) {
        for (CommandType command : values()) {
            boolean isExactMatch = input.equals(command.keyword);
            boolean hasAcceptedArguments = command.acceptsArguments
                    && input.startsWith(command.keyword + " ");
            if (isExactMatch || hasAcceptedArguments) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
