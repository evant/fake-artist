package me.tatarka.fakeartist.game.main;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import me.tatarka.fakeartist.R;

public class Drawing implements Parcelable {

    public final int playerCount;
    public final int[] colorPallet;
    public final ArrayList<Line> lines;

    public Drawing(Resources res, int playerCount) {
        this.playerCount = playerCount;
        lines = new ArrayList<>();
        // TODO: change? Randomize?
        int pink = res.getColor(R.color.pink);
        int blue = res.getColor(R.color.blue);
        int orange = res.getColor(R.color.orange);
        colorPallet = new int[]{
                pink, blue, orange
        };
    }
    
    public static class Line implements Parcelable {
        public final int player;
        public final int[] points;

        /**
         * Construct a new line in the drawing.
         *
         * @param player the player index who drew the line (0-n)
         * @param points the points that make up the line, in the format [x0 y0 x1 y1...].
         */
        public Line(int player, int[] points) {
            this.player = player;
            this.points = points;
        }

        protected Line(Parcel in) {
            player = in.readInt();
            points = in.createIntArray();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(player);
            dest.writeIntArray(points);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Line> CREATOR = new Creator<Line>() {
            @Override
            public Line createFromParcel(Parcel in) {
                return new Line(in);
            }

            @Override
            public Line[] newArray(int size) {
                return new Line[size];
            }
        };
    }

    protected Drawing(Parcel in) {
        playerCount = in.readInt();
        colorPallet = in.createIntArray();
        lines = in.createTypedArrayList(Line.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(playerCount);
        dest.writeIntArray(colorPallet);
        dest.writeTypedList(lines);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Drawing> CREATOR = new Creator<Drawing>() {
        @Override
        public Drawing createFromParcel(Parcel in) {
            return new Drawing(in);
        }

        @Override
        public Drawing[] newArray(int size) {
            return new Drawing[size];
        }
    };
}
