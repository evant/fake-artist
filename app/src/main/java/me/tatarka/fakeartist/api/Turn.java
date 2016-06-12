package me.tatarka.fakeartist.api;

import android.os.Parcel;
import android.os.Parcelable;

public class Turn implements Parcelable {

    public static final int ROUNDS = 2;

    public final String player;
    public final int playerCount;
    public final int count;

    public Turn(State.Builder state) {
        this(
                state.players.get((state.players.indexOf(state.qm) + 1) % state.players.size()),
                state
        );
    }
    
    public Turn(String player, State.Builder state) {
        this(
                player,
                state.players.size(),
                state.drawing.lines.size()
        );
    }

    Turn(String player, int playerCount, int count) {
        this.player = player;
        this.playerCount = playerCount;
        this.count = count;
    }

    public Turn next(String player) {
        int newCount = count + 1;
        return new Turn(player, playerCount, newCount);
    }

    public int round() {
        return count % (playerCount - 1);
    }

    public boolean isOver() {
        return count >= (playerCount - 1) * ROUNDS;
    }

    protected Turn(Parcel in) {
        player = in.readString();
        playerCount = in.readInt();
        count = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(player);
        dest.writeInt(playerCount);
        dest.writeInt(count);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Turn> CREATOR = new Creator<Turn>() {
        @Override
        public Turn createFromParcel(Parcel in) {
            return new Turn(in);
        }

        @Override
        public Turn[] newArray(int size) {
            return new Turn[size];
        }
    };

}
