/**
 * Represents a task with a description and completion status.
 */
public class Task {
    /** Description of the work represented by this task. */
    protected String description;

    /** Whether this task has been completed. */
    protected boolean isDone;

    /** Symbol identifying this task's type. */
    private final String typeIcon;

    /** Date or time information displayed after the description. */
    private final String timingDetails;

    /**
     * Creates an incomplete task of the specified type.
     *
     * @param description description of the task
     * @param typeIcon symbol identifying the task type
     * @param timingDetails formatted date or time information
     */
    public Task(String description, String typeIcon, String timingDetails) {
        this.description = description;
        this.isDone = false;
        this.typeIcon = typeIcon;
        this.timingDetails = timingDetails;
    }

    /**
     * Returns the symbol used to display this task's completion status.
     *
     * @return {@code X} when done, or a space when not done
     */
    public String getStatusIcon() {
        return isDone ? "X" : " ";
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        this.isDone = true;
    }

    /** Marks this task as not completed. */
    public void markAsNotDone() {
        this.isDone = false;
    }

    /**
     * Returns this task's type, status, description, and timing details.
     *
     * @return formatted task information
     */
    @Override
    public String toString() {
        return "[" + typeIcon + "][" + getStatusIcon() + "] " + description + timingDetails;
    }
}
