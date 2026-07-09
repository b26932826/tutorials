### Relevant articles

- [A Guide to the Spring State Machine Project](http://www.baeldung.com/spring-state-machine)

### Examples

- A trading position lifecycle modeled as a state machine
  (`FLAT` → `PENDING_OPEN` → `OPEN` → `PENDING_CLOSE` → `CLOSED`, with cancellation
  paths back to the previous state). It also supports a same-day
  day-trade offset (當沖), where a `DAY_TRADE_OFFSET` event closes an
  `OPEN` position directly within the session without a separate
  pending-close order. See the `position` package and
  `PositionStateMachineConfiguration`.
