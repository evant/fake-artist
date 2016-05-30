package me.tatarka.fakeartist.api;

import com.shephertz.app42.gaming.multiplayer.client.events.ConnectEvent;

public class ConnectException extends ApiException {
    private final ConnectEvent event;

    public ConnectException(ConnectEvent event) {
        super("Failed to connect result code: " + event.getResult() + " reason code: " + event.getReasonCode(), event.getResult());
        this.event = event;
    }

    public ConnectEvent getEvent() {
        return event;
    }
}
