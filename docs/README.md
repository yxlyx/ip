# Chatty User Guide

Chatty is a command-line task manager that keeps track of todos, deadlines,
and events during the current application session.

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

## Exiting Chatty

Enter `bye` to close Chatty.

```text
Bye. Hope to see you again soon!
```

Chatty does not save tasks after the application exits.
