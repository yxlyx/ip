import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves Chatty's tasks to a text file on the local file system.
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
