package com.baeldung.spring.statemachine;

import com.baeldung.spring.statemachine.config.PositionStateMachineConfiguration;
import com.baeldung.spring.statemachine.position.PositionEvents;
import com.baeldung.spring.statemachine.position.PositionStates;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PositionStateMachineConfiguration.class)
public class PositionStateMachineIntegrationTest {

    @Autowired
    private StateMachine<PositionStates, PositionEvents> stateMachine;

    @Before
    public void setUp() {
        stateMachine.start();
    }

    @Test
    public void whenPositionOpenedAndClosed_thenReachesClosedState() {
        assertEquals(PositionStates.FLAT, stateMachine.getState().getId());

        assertTrue(stateMachine.sendEvent(PositionEvents.SUBMIT_OPEN));
        assertEquals(PositionStates.PENDING_OPEN, stateMachine.getState().getId());

        assertTrue(stateMachine.sendEvent(PositionEvents.FILL_OPEN));
        assertEquals(PositionStates.OPEN, stateMachine.getState().getId());

        assertTrue(stateMachine.sendEvent(PositionEvents.SUBMIT_CLOSE));
        assertEquals(PositionStates.PENDING_CLOSE, stateMachine.getState().getId());

        assertTrue(stateMachine.sendEvent(PositionEvents.FILL_CLOSE));
        assertEquals(PositionStates.CLOSED, stateMachine.getState().getId());
    }

    @Test
    public void whenPendingOpenCancelled_thenReturnsToFlat() {
        assertTrue(stateMachine.sendEvent(PositionEvents.SUBMIT_OPEN));
        assertEquals(PositionStates.PENDING_OPEN, stateMachine.getState().getId());

        assertTrue(stateMachine.sendEvent(PositionEvents.CANCEL));
        assertEquals(PositionStates.FLAT, stateMachine.getState().getId());
    }

    @Test
    public void whenPendingCloseCancelled_thenReturnsToOpen() {
        stateMachine.sendEvent(PositionEvents.SUBMIT_OPEN);
        stateMachine.sendEvent(PositionEvents.FILL_OPEN);

        assertTrue(stateMachine.sendEvent(PositionEvents.SUBMIT_CLOSE));
        assertEquals(PositionStates.PENDING_CLOSE, stateMachine.getState().getId());

        assertTrue(stateMachine.sendEvent(PositionEvents.CANCEL));
        assertEquals(PositionStates.OPEN, stateMachine.getState().getId());
    }

    @Test
    public void whenInvalidEventForState_thenTransitionIsRejected() {
        assertEquals(PositionStates.FLAT, stateMachine.getState().getId());

        assertFalse(stateMachine.sendEvent(PositionEvents.FILL_OPEN));
        assertEquals(PositionStates.FLAT, stateMachine.getState().getId());
    }

    @After
    public void tearDown() {
        stateMachine.stop();
    }
}
