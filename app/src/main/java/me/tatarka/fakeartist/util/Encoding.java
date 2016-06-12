package me.tatarka.fakeartist.util;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import me.tatarka.fakeartist.game.main.Drawing;

/**
 * Utilities to encode data for sending over the api.
 */
public class Encoding {

    /**
     * Encodes the deltas between points. Since points in a line are near each other, getting their
     * delta improves compression.
     */
    public static int[] deltaEncode(int[] points) {
        int[] out = new int[points.length];
        int pX = Drawing.CANVAS_WIDTH / 2;
        int pY = Drawing.CANVAS_HEIGHT / 2;
        for (int i = 0; i < points.length; i += 2) {
            int x = points[i];
            int y = points[i + 1];
            int dX = x - pX;
            int dY = y - pY;
            out[i] = dX;
            out[i + 1] = dY;
            pX = x;
            pY = y;
        }
        return out;
    }

    /**
     * Decodes the deltas between points to their absolute values.
     */
    public static int[] deltaDecode(int[] in) {
        int[] points = new int[in.length];
        int pX = Drawing.CANVAS_WIDTH / 2;
        int pY = Drawing.CANVAS_HEIGHT / 2;
        for (int i = 0; i < in.length; i += 2) {
            int dX = in[i];
            int dY = in[i + 1];
            int x = pX + dX;
            int y = pY + dY;
            points[i] = x;
            points[i + 1] = y;
            pX = x;
            pY = y;
        }
        return points;
    }

    /**
     * Compresses the points using {@link DeflaterOutputStream}.
     */
    public static byte[] deflate(int[] points) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(bytes));
            for (int point : points) {
                out.writeInt(point);
            }
            out.close();
            return bytes.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Decompresses the points using {@link InflaterInputStream}.
     */
    public static int[] inflate(byte[] bytes) {
        ByteArrayInputStream byteArray = new ByteArrayInputStream(bytes);
        DataInputStream in = new DataInputStream(new InflaterInputStream(byteArray));
        int[] points = new int[bytes.length / 8]; // assumes 50% compression
        int i = 0;
        while (true) {
            try {
                if (i >= points.length) {
                    int[] newPoints = new int[points.length * 2];
                    System.arraycopy(points, 0, newPoints, 0, points.length);
                    points = newPoints;
                }
                points[i] = in.readInt();
                i += 1;
            } catch (EOFException e) {
                break;
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return Arrays.copyOf(points, i);
    }

    public static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    public static byte[] fromBase64(String str) {
        return Base64.decode(str, Base64.NO_PADDING | Base64.NO_WRAP);
    }
}
