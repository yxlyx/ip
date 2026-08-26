# Chatty User Guide

Chatty is a command-line task manager that keeps track of todos, deadlines,
and events across application sessions.

## Adding a todo

Enter `todo DESCRIPTION` to add a task without a date or time.

Example: `todo borrow book`

```text
Got it. I've added this task:
  [T][ ] borrow book
Now you have 1 tasks in the list.
```

## Adding a deadline

Enter `deadline DESCRIPTION /by DATE_OR_TIME` to add a task that must be
completed before the given date or time. Chatty stores the date or time exactly
as entered.

Example: `deadline return book /by Sunday`

```text
Got it. I've added this task:
  [D][ ] return book (by: Sunday)
Now you have 2 tasks in the list.
```

## Adding an event

Enter `event DESCRIPTION /from START /to END` to add a task that occurs between
the given start and end. Chatty stores both values exactly as entered.

Example: `event project meeting /from Mon 2pm /to 4pm`

```text
Got it. I've added this task:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
Now you have 3 tasks in the list.
```

## Listing tasks

Enter `list` to display every task with its number, type, and status:

- `[T]` identifies a todo, `[D]` a deadline, and `[E]` an event.
- `[ ]` means a task is not done, while `[X]` means it is done.

```text
Here are the tasks in your list:
1.[T][ ] borrow book
2.[D][ ] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
```

## Marking a task as done

Enter `mark INDEX`, replacing `INDEX` with the task number shown by `list`.

Example: `mark 2`

```text
Nice! I've marked this task as done:
  [D][X] return book (by: Sunday)
```

## Marking a task as not done

Enter `unmark INDEX` to change a completed task back to not done.

Example: `unmark 2`

```text
OK, I've marked this task as not done yet:
  [D][ ] return book (by: Sunday)
```

## Deleting a task

Enter `delete INDEX`, replacing `INDEX` with the task number shown by `list`.
The remaining tasks are renumbered automatically.

Example: `delete 2`

```text
Noted. I've removed this task:
  [D][ ] return book (by: Sunday)
Now you have 2 tasks in the list.
```

## Handling invalid input

Chatty explains invalid commands instead of stopping unexpectedly. Error
messages identify what is missing and, where useful, show the expected format.
For example, a todo must have a description:

```text
OOPS!!! The description of a todo cannot be empty.
```

Task numbers used with `mark`, `unmark`, and `delete` must be whole numbers
that appear in the current list. Deadline and event commands must include all documented
delimiters and values.

## Exiting Chatty

Enter `bye` to close Chatty.

```text
Bye. Hope to see you again soon!
```

Chatty saves every task change to `data/chatty.txt` and restores the task list
when the application starts. The data directory and file are created automatically
when the first task change is saved.
