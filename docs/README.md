# Chatty User Guide

Chatty is a mission-control-inspired task manager that keeps track of todos,
deadlines, events, and recurring work across application sessions.

## Adding a todo

Enter `todo DESCRIPTION` to add a task without a date or time.

Example: `todo borrow book`

```text
Task added to the flight plan:
  [T][ ] borrow book
1 task(s) now on board.
```

## Adding a deadline

Enter `deadline DESCRIPTION /by YYYY-MM-DD` to add a task that must be
completed by a specific date. Chatty validates the date and displays it in a
more readable format.

Example: `deadline return book /by 2019-10-15`

```text
Task added to the flight plan:
  [D][ ] return book (by: Oct 15 2019)
2 task(s) now on board.
```

## Adding an event

Enter `event DESCRIPTION /from START /to END` to add a task that occurs between
the given start and end. Chatty stores both values exactly as entered.

Example: `event project meeting /from Mon 2pm /to 4pm`

```text
Task added to the flight plan:
  [E][ ] project meeting (from: Mon 2pm to: 4pm)
3 task(s) now on board.
```

## Adding a recurring task

Enter `recurring DESCRIPTION /on YYYY-MM-DD /every NUMBER UNIT` to add a task
that repeats on a calendar schedule. `NUMBER` must be a positive whole number,
and `UNIT` can be `day`, `week`, `month`, or `year`, in singular or plural form.

Example: `recurring project meeting /on 2026-09-14 /every 1 week`

```text
Task added to the flight plan:
  [R][ ] project meeting (on: Sep 14 2026, every: 1 week)
4 task(s) now on board.
```

A recurring task represents its next pending occurrence. Marking it completes
that occurrence and advances the displayed date by one interval. Month and year
intervals use calendar arithmetic, so a date near the end of a month can be
adjusted to the last valid day of the resulting month.

## Listing tasks

Enter `list` to display every task with its number, type, and status:

- `[T]` identifies a todo, `[D]` a deadline, `[E]` an event, and `[R]` a recurring task.
- `[ ]` means a task is not done, while `[X]` means it is done.

```text
Flight plan status:
1.[T][ ] borrow book
2.[D][ ] return book (by: Sunday)
3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
```

## Finding tasks

Enter `find KEYWORD` to display tasks whose descriptions contain that keyword.
Matches retain their existing order and are numbered within the search results.

Example: `find book`

```text
Radar found these matching tasks:
1.[T][ ] borrow book
2.[D][ ] return book (by: Sunday)
```

## Marking a task as done

Enter `mark INDEX`, replacing `INDEX` with the task number shown by `list`.

Example: `mark 2`

```text
Milestone cleared:
  [D][X] return book (by: Sunday)
```

For a recurring task, `mark INDEX` completes the current occurrence and advances
exactly one interval. It does not skip overdue occurrences automatically.

```text
Recurring milestone cleared. Next occurrence locked in:
  [R][ ] project meeting (on: Sep 21 2026, every: 1 week)
```

## Marking a task as not done

Enter `unmark INDEX` to change a completed task back to not done.

Example: `unmark 2`

```text
Task returned to active duty:
  [D][ ] return book (by: Sunday)
```

Recurring tasks cannot be unmarked because Chatty keeps only their next pending
occurrence rather than a history of completed occurrences.

## Deleting a task

Enter `delete INDEX`, replacing `INDEX` with the task number shown by `list`.
The remaining tasks are renumbered automatically.

Example: `delete 2`

```text
Task removed from the flight plan:
  [D][ ] return book (by: Sunday)
2 task(s) remain on board.
```

## Handling invalid input

Chatty explains invalid commands instead of stopping unexpectedly. Error
messages identify what is missing and, where useful, show the expected format.
For example, a todo must have a description:

```text
OOPS!!! The description of a todo cannot be empty.
```

Task numbers used with `mark`, `unmark`, and `delete` must be whole numbers
that appear in the current list. Deadline, event, and recurring-task commands
must include all documented delimiters and values. Deadline and recurring-task
dates must use `YYYY-MM-DD` and must represent valid calendar dates. Recurrence
intervals must contain a positive whole number and a supported unit.

## Exiting Chatty

Enter `bye` to close Chatty.

```text
Chatty signing off. Keep your next milestone in sight!
```

Chatty saves every task change to `data/chatty.txt` and restores the task list
when the application starts. A missing file is treated as an empty task list,
and the data directory and file are created automatically when the first task
change is saved. If an existing file cannot be read or is malformed, Chatty
shows an error and starts with an empty list instead of terminating.
