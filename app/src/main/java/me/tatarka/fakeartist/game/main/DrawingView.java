package me.tatarka.fakeartist.game.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.tatarka.fakeartist.R;

public class DrawingView extends View {
    private static final int CANVAS_HEIGHT = 800;
    private static final int CANVAS_WIDTH = 600;
    private static final int POINT_LIMIT = 500;
    private static final int MIN_POINT_DELTA = 8;

    private Drawing drawing;
    private int player;
    private boolean lineDrawn;
    private OnLineDoneListener lineDoneListener;

    private final Paint linePaint;
    private Path[] paths;

    private int[] currentPoints;
    private int currentPointsLength;
    private final Path currentPath;
    private int lastX;
    private int lastY;

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.line_width));
        linePaint.setStyle(Paint.Style.STROKE);
        currentPath = new Path();
        currentPoints = new int[16];
    }

    public void setDrawing(Drawing drawing) {
        this.drawing = drawing;
        if (paths == null || paths.length < drawing.colors.length * 2) {
            paths = new Path[drawing.colors.length * 2];
        }
        for (int i = 0; i < drawing.lines.size(); i++) {
            Drawing.Line line = drawing.lines.get(i);
            paths[i] = lineToPath(paths[i], line);
        }
        currentPath.reset();
        currentPointsLength = 0;
        lineDrawn = false;
        invalidate();
    }
    
    public void setPlayer(int player) {
        this.player = player;
    }

    public Drawing getDrawing() {
        return drawing;
    }
    
    public int getPlayer() {
        return player;
    }

    public void setOnLineDoneListener(OnLineDoneListener listener) {
        lineDoneListener = listener;
    }

    public void clearCurrentPoints() {
        if (drawing == null) {
            return;
        }
        currentPath.reset();
        currentPointsLength = 0;
        lineDrawn = false;
        invalidate();
    }

    public void commitCurrentPoints() {
        if (drawing == null) {
            return;
        }
        Drawing.Line line = new Drawing.Line(player, Arrays.copyOf(currentPoints, currentPointsLength));
        paths[drawing.lines.size()] = lineToPath(paths[drawing.lines.size()], line);
        drawing = drawing.withNewLine(line);
        currentPath.reset();
        currentPointsLength = 0;
        lineDrawn = false;
    }

    private Path lineToPath(@Nullable Path path, Drawing.Line line) {
        if (path == null) {
            path = new Path();
        } else {
            path.reset();
        }
        if (line.points.length > 2) {
            path.moveTo(line.points[0], line.points[1]);
        }
        for (int i = 2; i < line.points.length; i += 2) {
            path.lineTo(line.points[i], line.points[i + 1]);
        }
        return path;
    }

    private boolean addCurrentPoint(int x, int y) {
        if (currentPointsLength >= POINT_LIMIT) {
            return false;
        }
        if (currentPath.isEmpty()) {
            currentPath.moveTo(x, y);
        } else {
            currentPath.lineTo(x, y);
        }
        if ((currentPointsLength + 2) >= currentPoints.length) {
            int[] newPoints = new int[currentPoints.length * 2];
            System.arraycopy(currentPoints, 0, newPoints, 0, currentPoints.length);
            currentPoints = newPoints;
        }
        currentPoints[currentPointsLength] = x;
        currentPoints[currentPointsLength + 1] = y;
        currentPointsLength += 2;
        return true;
    }

    private int scalePoint(float point, int viewSize, int targetSize) {
        return Math.round(point * targetSize / viewSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawing == null) {
            return;
        }
        canvas.scale(getWidth() / (float) CANVAS_WIDTH, getHeight() / (float) CANVAS_HEIGHT);
        List<Drawing.Line> lines = drawing.lines;
        for (int i = 0, size = lines.size(); i < size; i++) {
            Drawing.Line line = lines.get(i);
            linePaint.setColor(drawing.colors[line.player]);
            canvas.drawPath(paths[i], linePaint);
        }
        if (!currentPath.isEmpty()) {
            linePaint.setColor(drawing.colors[player]);
            canvas.drawPath(currentPath, linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || lineDrawn || drawing == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                lastX = scalePoint(event.getX(), getWidth(), CANVAS_WIDTH);
                lastY = scalePoint(event.getY(), getHeight(), CANVAS_HEIGHT);
                addCurrentPoint(lastX, lastY);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < event.getHistorySize(); i++) {
                    float x = event.getHistoricalX(i);
                    float y = event.getHistoricalY(i);
                    // skip points outside the canvas
                    if (x < 0 || y < 0 || x > getWidth() || y > getHeight()) {
                        continue;
                    }
                    int scaledX = scalePoint(x, getWidth(), CANVAS_WIDTH);
                    int scaledY = scalePoint(y, getHeight(), CANVAS_HEIGHT);
                    // Don't add point if we haven't moved enough. Instead, move the last point to
                    // the current position so drawing still feels smooth.
                    if (Math.abs(lastX - scaledX) < MIN_POINT_DELTA && Math.abs(lastY - scaledY) < MIN_POINT_DELTA) {
                        currentPoints[currentPointsLength - 2] = scaledX;
                        currentPoints[currentPointsLength - 1] = scaledY;
                        continue;
                    } else {
                        currentPoints[currentPointsLength - 2] = lastX;
                        currentPoints[currentPointsLength - 1] = lastY;
                    }
                    lastX = scaledX;
                    lastY = scaledY;
                    if (!addCurrentPoint(scaledX, scaledY)) {
                        lineDrawn = true;
                        break;
                    }
                }
                invalidate();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                lineDrawn = true;
                if (lineDoneListener != null) {
                    lineDoneListener.onLineDone();
                }
            }
            break;
        }
        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        int height = View.MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

        if (height <= 0 && width <= 0 && heightMode == View.MeasureSpec.UNSPECIFIED
                && widthMode == View.MeasureSpec.UNSPECIFIED) {
            width = 0;
            height = 0;
        } else {
            if (height <= 0 && heightMode == MeasureSpec.UNSPECIFIED) {
                height = width * CANVAS_HEIGHT / CANVAS_WIDTH;
            } else if (width <= 0 && widthMode == MeasureSpec.UNSPECIFIED) {
                width = height * CANVAS_WIDTH / CANVAS_HEIGHT;
            } else if (width * CANVAS_HEIGHT > CANVAS_WIDTH * height) {
                width = height * CANVAS_WIDTH / CANVAS_HEIGHT;
            } else {
                height = width * CANVAS_HEIGHT / CANVAS_WIDTH;
            }
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        SavedState state = new SavedState(super.onSaveInstanceState());
        state.drawing = drawing;
        state.player = player;
        state.currentPoints = Arrays.copyOf(currentPoints, currentPointsLength);
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcelable) {
        SavedState state = (SavedState) parcelable;
        super.onRestoreInstanceState(state.getSuperState());
        setDrawing(state.drawing);
        setPlayer(state.player);
        if (state.currentPoints.length > 0) {
            currentPoints = state.currentPoints;
            currentPointsLength = state.currentPoints.length;
            for (int i = 0; i < currentPointsLength; i += 2) {
                if (currentPath.isEmpty()) {
                    currentPath.moveTo(currentPoints[i], currentPoints[i + 1]);
                } else {
                    currentPath.lineTo(currentPoints[i], currentPoints[i + 1]);
                }
            }
            lineDrawn = true;
            if (lineDoneListener != null) {
                lineDoneListener.onLineDone();
            }
        }
    }

    public interface OnLineDoneListener {
        void onLineDone();
    }

    static class SavedState extends BaseSavedState {
        Drawing drawing;
        int player;
        int[] currentPoints;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            drawing = source.readParcelable(getClass().getClassLoader());
            player = source.readInt();
            currentPoints = source.createIntArray();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeParcelable(drawing, flags);
            out.writeInt(player);
            out.writeIntArray(currentPoints);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
