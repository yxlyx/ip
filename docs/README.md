# Chatty User Guide

Chatty is a command-line task manager that keeps track of tasks during the
current application session.

## Adding a task

Enter the task description directly to add it to the list.

Example: `read book`

```text
added: read book
```

## Listing tasks

Enter `list` to display every task with its number and status. `[ ]` means a
task is not done, while `[X]` means it is done.

```text
Here are the tasks in your list:
1.[X] read book
2.[ ] return book
```

## Marking a task as done

Enter `mark INDEX`, replacing `INDEX` with the task number shown by `list`.

Example: `mark 2`

```text
Nice! I've marked this task as done:
  [X] return book
```

## Marking a task as not done

Enter `unmark INDEX` to change a completed task back to not done.

Example: `unmark 2`

```text
OK, I've marked this task as not done yet:
  [ ] return book
```

## Exiting Chatty

Enter `bye` to close Chatty.

```text
Bye. Hope to see you again soon!
```

Chatty does not save tasks after the application exits.
