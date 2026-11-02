package de.fsr.mariokart_backend.schedule.service.admin;

import org.springframework.context.ApplicationEvent;

public class NextRoundChangedEvent extends ApplicationEvent {

    public NextRoundChangedEvent(Object source) {
        super(source);
    }
}
