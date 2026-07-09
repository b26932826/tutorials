### Relevant articles

- [A Guide to the Spring State Machine Project](http://www.baeldung.com/spring-state-machine)

### Examples

- A trading position lifecycle modeled as a state machine
  (`FLAT` → `PENDING_OPEN` → `OPEN` → `PENDING_CLOSE` → `CLOSED`, with cancellation
  paths back to the previous state). See the `position` package and
  `PositionStateMachineConfiguration`.
