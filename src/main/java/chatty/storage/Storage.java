package chatty.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import chatty.exception.ChattyException;
import chatty.task.Deadline;
import chatty.task.Event;
import chatty.task.RecurrenceUnit;
import chatty.task.RecurringTask;
import chatty.task.Task;
import chatty.task.Todo;

/**
 * Loads and saves Chatty's tasks using a text file on the local file system.
 */
public class Storage {
    /** Separator placed between fields in each saved task record. */
    private static final String FIELD_SEPARATOR = " | ";

    /** Stored status representing a completed task. */
    private static final String DONE_STATUS = "1";

    /** Stored status representing an incomplete task. */
    private static final String NOT_DONE_STATUS = "0";

    /** Relative path of the file used to store tasks. */
    private final Path filePath;

    /**
     * Creates storage that writes tasks to the given path.
     *
     * @param filePath relative path of the task data file.
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads tasks from the data file, or returns an empty list when it does not exist.
     *
     * @return tasks restored from the data file.
     * @throws ChattyException if the data file cannot be read or contains an invalid record.
     */
    public List<Task> loadTasks() throws ChattyException {
        if (Files.notExists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<Task> tasks = new ArrayList<>();
            List<String> taskRecords = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (int i = 0; i < taskRecords.size(); i++) {
                String record = taskRecords.get(i);
                if (!record.isBlank()) {
                    tasks.add(parseTask(record, i + 1));
                }
            }
            return tasks;
        } catch (IOException exception) {
            throw new ChattyException("OOPS!!! I couldn't read your tasks from the data file.");
        }
    }

    /**
     * Replaces the data file contents with the current task list.
     *
     * @param tasks tasks to save.
     * @throws ChattyException if the directory or data file cannot be written.
     */
    public void saveTasks(List<Task> tasks) throws ChattyException {
        try {
            Path parentDirectory = filePath.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            List<String> taskRecords = new ArrayList<>();
            for (Task task : tasks) {
                taskRecords.add(formatTask(task));
            }
            Files.write(filePath, taskRecords, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new ChattyException("OOPS!!! I couldn't save your tasks to the data file.");
        }
    }

    /**
     * Restores one task from a delimited storage record.
     *
     * @param record delimited task record.
     * @param lineNumber one-based line number of the record.
     * @return task restored from the record.
     * @throws ChattyException if the record is malformed.
     */
    private Task parseTask(String record, int lineNumber) throws ChattyException {
        String[] fields = record.split("\\s*\\|\\s*", -1);
        if (fields.length < 3 || fields[2].isBlank()) {
            throw invalidRecord(lineNumber);
        }

        boolean isDone;
        if (fields[1].equals(DONE_STATUS)) {
            isDone = true;
        } else if (fields[1].equals(NOT_DONE_STATUS)) {
            isDone = false;
        } else {
            throw invalidRecord(lineNumber);
        }

        Task task;
        switch (fields[0]) {
            case "T":
                requireFields(fields, 3, lineNumber);
                task = new Todo(fields[2]);
                break;
            case "D":
                requireFields(fields, 4, lineNumber);
                if (fields[3].isBlank()) {
                    throw invalidRecord(lineNumber);
                }
                try {
                    task = new Deadline(fields[2], LocalDate.parse(fields[3]));
                } catch (DateTimeParseException exception) {
                    throw invalidRecord(lineNumber);
                }
                break;
            case "E":
                requireFields(fields, 5, lineNumber);
                if (fields[3].isBlank() || fields[4].isBlank()) {
                    throw invalidRecord(lineNumber);
                }
                task = new Event(fields[2], fields[3], fields[4]);
                break;
            case "R":
                requireFields(fields, 6, lineNumber);
                task = parseRecurringTask(fields, isDone, lineNumber);
                break;
            default:
                throw invalidRecord(lineNumber);
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Restores a recurring task from its validated storage fields.
     *
     * @param fields recurring-task storage fields.
     * @param isDone stored completion status.
     * @param lineNumber one-based line number of the record.
     * @return recurring task restored from the record.
     * @throws ChattyException if the recurring-task fields are invalid.
     */
    private Task parseRecurringTask(String[] fields, boolean isDone, int lineNumber)
            throws ChattyException {
        if (isDone || fields[3].isBlank() || fields[4].isBlank() || fields[5].isBlank()) {
            throw invalidRecord(lineNumber);
        }

        LocalDate nextOccurrenceDate;
        try {
            nextOccurrenceDate = LocalDate.parse(fields[3]);
        } catch (DateTimeParseException exception) {
            throw invalidRecord(lineNumber);
        }

        int interval;
        try {
            interval = Integer.parseInt(fields[4]);
        } catch (NumberFormatException exception) {
            throw invalidRecord(lineNumber);
        }
        if (interval <= 0) {
            throw invalidRecord(lineNumber);
        }

        RecurrenceUnit recurrenceUnit;
        try {
            recurrenceUnit = RecurrenceUnit.valueOf(fields[5]);
        } catch (IllegalArgumentException exception) {
            throw invalidRecord(lineNumber);
        }
        return new RecurringTask(fields[2], nextOccurrenceDate, interval, recurrenceUnit);
    }

    /**
     * Ensures that a stored task record has the expected number of fields.
     *
     * @param fields fields parsed from the record.
     * @param expectedCount required number of fields.
     * @param lineNumber one-based line number of the record.
     * @throws ChattyException if the field count is incorrect.
     */
    private void requireFields(String[] fields, int expectedCount, int lineNumber) throws ChattyException {
        if (fields.length != expectedCount) {
            throw invalidRecord(lineNumber);
        }
    }

    /**
     * Returns an error describing the location of a malformed stored task.
     *
     * @param lineNumber one-based line number of the malformed record.
     * @return exception describing the malformed record.
     */
    private ChattyException invalidRecord(int lineNumber) {
        return new ChattyException("OOPS!!! The data file is corrupted at line " + lineNumber + ".");
    }

    /**
     * Returns one task encoded as a delimited storage record.
     *
     * @param task task to encode.
     * @return delimited record representing the task.
     */
    private String formatTask(Task task) {
        String status = task.isDone() ? DONE_STATUS : NOT_DONE_STATUS;
        String record = task.getTypeIcon() + FIELD_SEPARATOR
                + status + FIELD_SEPARATOR + task.getDescription();
        if (task instanceof Deadline deadline) {
            return record + FIELD_SEPARATOR + deadline.getDueDate();
        } else if (task instanceof Event event) {
            return record + FIELD_SEPARATOR + event.getFrom()
                    + FIELD_SEPARATOR + event.getTo();
        } else if (task instanceof RecurringTask recurringTask) {
            return record + FIELD_SEPARATOR + recurringTask.getNextOccurrenceDate()
                    + FIELD_SEPARATOR + recurringTask.getInterval()
                    + FIELD_SEPARATOR + recurringTask.getRecurrenceUnit();
        }
        assert task instanceof Todo : "Only todo tasks can use the base storage format";
        return record;
    }
}
