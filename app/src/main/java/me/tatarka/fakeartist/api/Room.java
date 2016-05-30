package me.tatarka.fakeartist.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {
    public final String name;
    public final List<String> players;

    public Room(String name, List<String> players) {
        this.name = name;
        this.players = Collections.unmodifiableList(new ArrayList<>(players));
    }
}
