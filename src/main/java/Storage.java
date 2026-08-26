import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and saves Chatty's tasks using a text file on the local file system.
 */
public class Storage {
    /** Separator placed between fields in each saved task record. */
    private static final String FIELD_SEPARATOR = " | ";

    /** Relative path of the file used to store tasks. */
    private final Path filePath;

    /**
     * Creates storage that writes tasks to the given path.
     *
     * @param filePath relative path of the task data file
     */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Loads tasks from the data file, or returns an empty list when it does not exist.
     *
     * @return tasks restored from the data file
     * @throws ChattyException if the data file cannot be read or contains an invalid record
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
     * @param tasks tasks to save
     * @throws ChattyException if the directory or data file cannot be written
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

    /** Restores one task from a delimited storage record. */
    private Task parseTask(String record, int lineNumber) throws ChattyException {
        String[] fields = record.split("\\s*\\|\\s*", -1);
        if (fields.length < 3 || fields[2].isBlank()) {
            throw invalidRecord(lineNumber);
        }

        boolean isDone;
        if (fields[1].equals("1")) {
            isDone = true;
        } else if (fields[1].equals("0")) {
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
        default:
            throw invalidRecord(lineNumber);
        }

        if (isDone) {
            task.markAsDone();
        }
        return task;
    }

    /** Ensures that a stored task record has the expected number of fields. */
    private void requireFields(String[] fields, int expectedCount, int lineNumber) throws ChattyException {
        if (fields.length != expectedCount) {
            throw invalidRecord(lineNumber);
        }
    }

    /** Returns an error describing the location of a malformed stored task. */
    private ChattyException invalidRecord(int lineNumber) {
        return new ChattyException("OOPS!!! The data file is corrupted at line " + lineNumber + ".");
    }

    /** Returns one task encoded as a delimited storage record. */
    private String formatTask(Task task) {
        String status = task.isDone() ? "1" : "0";
        String record = task.getTypeIcon() + FIELD_SEPARATOR
                + status + FIELD_SEPARATOR + task.getDescription();
        if (task instanceof Deadline deadline) {
            return record + FIELD_SEPARATOR + deadline.getBy();
        } else if (task instanceof Event event) {
            return record + FIELD_SEPARATOR + event.getFrom()
                    + FIELD_SEPARATOR + event.getTo();
        }
        return record;
    }
}
