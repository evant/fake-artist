package me.tatarka.fakeartist.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.tatarka.fakeartist.game.main.Drawing;

public class State implements Parcelable {
    public final String roomId;
    public final String roomName;
    public final String userName;
    public final String qm;
    public final String fake;
    public final String category;
    public final String title;
    public final List<String> players;
    public final String turn;
    public final Drawing drawing;

    private State(String roomId, String roomName, String userName, String qm, String fake, String category, String title, List<String> players, String turn, Drawing drawing) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.userName = userName;
        this.qm = qm;
        this.fake = fake;
        this.category = category;
        this.title = title;
        this.players = Collections.unmodifiableList(new ArrayList<>(players));
        this.turn = turn;
        this.drawing = drawing;
    }

    public Role role() {
        return role(userName);
    }

    public Role role(@Nullable String userName) {
        if (userName == null) {
            return Role.UNKNOWN;
        }
        if (userName.equals(qm)) {
            return Role.QM;
        } else if (fake == null) {
            return Role.UNKNOWN;
        } else if (userName.equals(fake)) {
            return Role.FAKE;
        } else {
            return Role.ARTIST;
        }
    }

    public int color() {
        return color(userName);
    }

    public int color(String player) {
        return drawing.colors[players.indexOf(player)];
    }

    public static class Builder {
        public String roomId;
        public String roomName;
        public String userName;
        public String qm;
        public String fake;
        public String category;
        public String title;
        public List<String> players = new ArrayList<>();
        public String turn;
        public Drawing drawing;

        public Builder() {
        }

        public Builder(State state) {
            set(state);
        }

        public void set(State state) {
            roomId = state.roomId;
            roomName = state.roomName;
            userName = state.userName;
            qm = state.qm;
            fake = state.fake;
            category = state.category;
            title = state.title;
            players = new ArrayList<>(state.players);
            turn = state.turn;
            drawing = state.drawing;
        }

        public void set(HashMap<String, Object> props) {
            if (props.containsKey("qm")) {
                qm = (String) props.get("qm");
            }
            if (props.containsKey("fake")) {
                fake = (String) props.get("fake");
            }
            if (props.containsKey("category")) {
                category = (String) props.get("category");
            }
            if (props.containsKey("title")) {
                title = (String) props.get("title");
            }
            if (props.containsKey("colors")) {
                int[] colors = parseColors((String) props.get("colors"));
                drawing = new Drawing(colors, Collections.<Drawing.Line>emptyList());
            }
        }

        public void get(HashMap<String, Object> props) {
            props.put("qm", qm);
            props.put("fake", fake);
            props.put("category", category);
            props.put("title", title);
            if (drawing != null) {
                props.put("colors", serializeColors(drawing.colors));
            }
        }

        public Builder addPlayer(String player) {
            int index = Collections.binarySearch(players, player);
            if (index < 0) {
                players.add(-(index + 1), player);
            }
            return this;
        }

        public Builder removePlayer(String player) {
            players.remove(player);
            return this;
        }

        public Builder setPlayers(Collection<String> players) {
            this.players.clear();
            this.players.addAll(players);
            return this;
        }

        public State build() {
            return new State(roomId, roomName, userName, qm, fake, category, title, players, turn, drawing);
        }

        private static int[] parseColors(String str) {
            if (str == null) {
                return null;
            }
            String[] parts = str.split(",");
            int[] colors = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                colors[i] = Integer.parseInt(parts[i]);
            }
            return colors;
        }

        private static String serializeColors(int[] colors) {
            if (colors == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            boolean firstTime = true;
            for (int color : colors) {
                if (firstTime) {
                    firstTime = false;
                } else {
                    sb.append(",");
                }
                sb.append(color);
            }
            return sb.toString();
        }

        public String nextTurn() {
            int index = players.indexOf(userName);
            int nextIndex = (index + 1) % players.size();
            return players.get(nextIndex);
        }
    }

    protected State(Parcel in) {
        roomId = in.readString();
        roomName = in.readString();
        userName = in.readString();
        qm = in.readString();
        fake = in.readString();
        category = in.readString();
        title = in.readString();
        players = in.createStringArrayList();
        turn = in.readString();
        drawing = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomId);
        dest.writeString(roomName);
        dest.writeString(userName);
        dest.writeString(qm);
        dest.writeString(fake);
        dest.writeString(category);
        dest.writeString(title);
        dest.writeStringList(players);
        dest.writeString(turn);
        dest.writeParcelable(drawing, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<State> CREATOR = new Creator<State>() {
        @Override
        public State createFromParcel(Parcel in) {
            return new State(in);
        }

        @Override
        public State[] newArray(int size) {
            return new State[size];
        }
    };

    public enum Role {
        QM, FAKE, ARTIST, UNKNOWN
    }
}
