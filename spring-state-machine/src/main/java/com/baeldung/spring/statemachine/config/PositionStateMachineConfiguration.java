package com.baeldung.spring.statemachine.config;

import com.baeldung.spring.statemachine.position.PositionEvents;
import com.baeldung.spring.statemachine.position.PositionStates;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachine
public class PositionStateMachineConfiguration extends StateMachineConfigurerAdapter<PositionStates, PositionEvents> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<PositionStates, PositionEvents> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(new StateMachineListener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<PositionStates, PositionEvents> states) throws Exception {
        states
                .withStates()
                .initial(PositionStates.FLAT)
                .state(PositionStates.PENDING_OPEN)
                .state(PositionStates.OPEN)
                .state(PositionStates.PENDING_CLOSE)
                .end(PositionStates.CLOSED);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PositionStates, PositionEvents> transitions) throws Exception {
        transitions.withExternal()
                .source(PositionStates.FLAT).target(PositionStates.PENDING_OPEN).event(PositionEvents.SUBMIT_OPEN)
                .and().withExternal()
                .source(PositionStates.PENDING_OPEN).target(PositionStates.OPEN).event(PositionEvents.FILL_OPEN)
                .and().withExternal()
                .source(PositionStates.PENDING_OPEN).target(PositionStates.FLAT).event(PositionEvents.CANCEL)
                .and().withExternal()
                .source(PositionStates.OPEN).target(PositionStates.PENDING_CLOSE).event(PositionEvents.SUBMIT_CLOSE)
                .and().withExternal()
                .source(PositionStates.PENDING_CLOSE).target(PositionStates.CLOSED).event(PositionEvents.FILL_CLOSE)
                .and().withExternal()
                .source(PositionStates.PENDING_CLOSE).target(PositionStates.OPEN).event(PositionEvents.CANCEL)
                .and().withExternal()
                .source(PositionStates.OPEN).target(PositionStates.CLOSED).event(PositionEvents.DAY_TRADE_OFFSET);
    }
}
