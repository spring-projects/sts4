*Required*. Only applies to monit start operation.

- If the `update_watch_time` is an integer, the Director sleeps for that many milliseconds, then checks whether the instances are healthy.
- If the `update_watch_time` is a range (low-high), the Director:
  - Waits for `low` milliseconds
  - Waits until instances are healthy or `high` milliseconds have passed since instances started updating
