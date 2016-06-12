package me.tatarka.fakeartist.game.main;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.tatarka.fakeartist.util.Encoding;

public class Drawing implements Parcelable {
    public static final int CANVAS_HEIGHT = 800;
    public static final int CANVAS_WIDTH = 600;
    
    public final int[] colors;
    public final List<Line> lines;

    public Drawing(int[] colors, List<Line> lines) {
        this.colors = colors;
        this.lines = Collections.unmodifiableList(lines);
    }

    @Nullable
    public Line lastLine() {
        if (lines.isEmpty()) {
            return null;
        }
        return lines.get(lines.size() - 1);
    }

    public Drawing withNewLine(Line line) {
        ArrayList<Line> newLines = new ArrayList<>(lines);
        newLines.add(line);
        return new Drawing(colors, newLines);
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

        public String serialize() {
            return Encoding.toBase64(Encoding.deflate(Encoding.deltaEncode(points)));
        }

        public static Line deserialize(int player, String data) {
            int[] points = Encoding.deltaDecode(Encoding.inflate(Encoding.fromBase64(data)));
            return new Line(player, points);
        }
    }

    protected Drawing(Parcel in) {
        colors = in.createIntArray();
        lines = in.createTypedArrayList(Line.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(colors);
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
