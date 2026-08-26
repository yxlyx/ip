/**
 * Represents a task that must be completed by a specific date or time.
 */
public class Deadline extends Task {
    /** Date or time by which this task should be completed. */
    private final String by;

    /**
     * Creates an incomplete deadline with the given description and due value.
     *
     * @param description description of the deadline
     * @param by date or time by which the task should be completed
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String getTypeIcon() {
        return "D";
    }

    /**
     * Returns the date or time by which this task should be completed.
     *
     * @return deadline due value
     */
    public String getBy() {
        return by;
    }

    @Override
    public String toString() {
        return super.toString() + " (by: " + by + ")";
    }
}
