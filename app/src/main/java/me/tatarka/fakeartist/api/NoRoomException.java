package me.tatarka.fakeartist.api;

public class NoRoomException extends ApiException {
    public NoRoomException(String roomName) {
        super("Failed to find room: " + roomName, 0);
    }
}
