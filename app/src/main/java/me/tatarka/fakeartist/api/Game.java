package me.tatarka.fakeartist.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Game implements Parcelable {
    public static final int MIN_PLAYERS = 4;
    public static final int MAX_PLAYERS = 16;
    
    public final Event event;
    public final State state;

    public Game(Event event, State state) {
        this.event = event;
        this.state = state;
    }

    protected Game(Parcel in) {
        event = Event.valueOf(in.readString());
        state = in.readParcelable(State.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(event.name());
        dest.writeParcelable(state, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Game> CREATOR = new Creator<Game>() {
        @Override
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        @Override
        public Game[] newArray(int size) {
            return new Game[size];
        }
    };
}
