package me.tatarka.fakeartist.api.listeners;

import com.shephertz.app42.gaming.multiplayer.client.events.ConnectEvent;
import com.shephertz.app42.gaming.multiplayer.client.listener.ConnectionRequestListener;

public abstract class ConnectonRequestAdapter implements ConnectionRequestListener {
    @Override
    public void onConnectDone(ConnectEvent connectEvent) {
        
    }

    @Override
    public void onDisconnectDone(ConnectEvent connectEvent) {

    }

    @Override
    public void onInitUDPDone(byte b) {

    }
}
