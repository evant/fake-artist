package me.tatarka.fakeartist.game.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

import me.tatarka.fakeartist.R;

public class DrawingView extends View {
    public static final int CANVAS_HEIGHT = 800;
    private Drawing drawing;
    private int currentPlayer = 0;
    private float[] currentPoints = new float[16];
    private int currentPointsLength = 0;
    private boolean lineDrawn;
    private OnLineDoneListener lineDoneListener;
    private Paint linePaint;
    private int CANVAS_WIDTH;

    public DrawingView(Context context) {
        this(context, null);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        linePaint = new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.line_width));
    }

    public void setDrawing(Drawing drawing) {
        this.drawing = drawing;
        currentPointsLength = 0;
        lineDrawn = false;
        invalidate();
    }

    public Drawing getDrawing() {
        return drawing;
    }

    public void setCurrentPlayer(int currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void setOnLineDoneListener(OnLineDoneListener listener) {
        lineDoneListener = listener;
    }

    public void clearCurrentPoints() {
        if (drawing == null) {
            return;
        }
        currentPointsLength = 0;
        lineDrawn = false;
        invalidate();
    }

    public void commitCurrentPoints() {
        if (drawing == null) {
            return;
        }
        Drawing.Line line = new Drawing.Line(currentPlayer, Arrays.copyOf(currentPoints, currentPointsLength));
        drawing.lines.add(line);
        currentPointsLength = 0;
        lineDrawn = false;
    }

    private void addCurrentPoint(float x, float y) {
        if ((currentPointsLength + 4) >= currentPoints.length) {
            float[] newPoints = new float[currentPoints.length * 2];
            System.arraycopy(currentPoints, 0, newPoints, 0, currentPoints.length);
            currentPoints = newPoints;
        }
        if (currentPointsLength > 2) {
            currentPoints[currentPointsLength] = currentPoints[currentPointsLength - 2];
            currentPoints[currentPointsLength + 1] = currentPoints[currentPointsLength - 1];
            currentPointsLength += 2;
        }
        currentPoints[currentPointsLength] = x;
        currentPoints[currentPointsLength + 1] = y;
        currentPointsLength += 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (drawing == null) {
            return;
        }
        canvas.scale(getWidth() / (float) CANVAS_WIDTH, getHeight() / (float) CANVAS_HEIGHT);
        ArrayList<Drawing.Line> lines = drawing.lines;
        for (int i = 0, size = lines.size(); i < size; i++) {
            Drawing.Line line = lines.get(i);
            linePaint.setColor(drawing.colorPallet[line.player]);
            canvas.drawLines(line.points, linePaint);
        }
        if (currentPointsLength > 2) {
            linePaint.setColor(drawing.colorPallet[currentPlayer]);
            canvas.drawLines(currentPoints, 0, currentPointsLength, linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled() || lineDrawn || drawing == null) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                float scaledX = event.getX() * CANVAS_WIDTH / getWidth();
                float scaledY = event.getY() * CANVAS_HEIGHT / getHeight();
                addCurrentPoint(scaledX, scaledY);
                invalidate();
            }
                break;
            case MotionEvent.ACTION_MOVE: {
                for (int i = 0; i < event.getHistorySize(); i++) {
                    float scaledX = event.getHistoricalX(i) * CANVAS_WIDTH / getWidth();
                    float scaledY = event.getHistoricalY(i) * CANVAS_HEIGHT / getHeight();
                    addCurrentPoint(scaledX, scaledY);
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
            CANVAS_WIDTH = 600;
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


    public interface OnLineDoneListener {
        void onLineDone();
    }
}
