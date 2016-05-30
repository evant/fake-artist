package me.tatarka.fakeartist.game.main;

import android.content.res.Resources;

import java.util.ArrayList;

import me.tatarka.fakeartist.R;

public class Drawing {

    public final int[] colorPallet;
    public final ArrayList<Line> lines;

    public Drawing(Resources res) {
        lines = new ArrayList<>();
        // TODO: change? Randomize?
        int pink = res.getColor(R.color.pink);
        int blue = res.getColor(R.color.blue);
        int orange = res.getColor(R.color.orange);
        colorPallet = new int[]{
                pink, blue, orange
        };
    }

    public static class Line {
        public final int player;
        public final float[] points;

        /**
         * Construct a new line in the drawing.
         *
         * @param player the player index who drew the line (0-n)
         * @param points the points that make up the line, in the format [x0 y0 x1 y1...].
         */
        public Line(int player, float[] points) {
            this.player = player;
            this.points = points;
        }
    }
}
