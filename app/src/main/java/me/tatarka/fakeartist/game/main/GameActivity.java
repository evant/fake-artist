package me.tatarka.fakeartist.game.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;

import me.tatarka.fakeartist.R;
import me.tatarka.fakeartist.RetainActivity;

public class GameActivity extends RetainActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);

        final DrawingView drawingView = (DrawingView) findViewById(R.id.drawing);
        final ImageButton undo = (ImageButton) findViewById(R.id.button_undo);
        final ImageButton done = (ImageButton) findViewById(R.id.button_done);
        
        drawingView.setDrawing(new Drawing(getResources()));

        drawingView.setOnLineDoneListener(new DrawingView.OnLineDoneListener() {
            @Override
            public void onLineDone() {
                AnimatorSet set = new AnimatorSet();
                set.playTogether(animateUp(undo), animateUp(done));
                set.start();
            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearCurrentPoints();
                AnimatorSet set = new AnimatorSet();
                set.playTogether(animateDown(undo), animateDown(done));
                set.start();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.commitCurrentPoints();
                drawingView.setCurrentPlayer((drawingView.getCurrentPlayer() + 1) % 3);
                
                AnimatorSet set = new AnimatorSet();
                set.playTogether(animateDown(undo), animateDown(done));
                set.start();
            }
        });
    }

    private ObjectAnimator animateDown(final View view) {
        int bottom = ((View) view.getParent()).getBottom();
        final ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0, bottom - view.getTop());
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
                view.setTranslationY(0);
            }
        });
        animator.setDuration(300);
        return animator;
    }

    private ObjectAnimator animateUp(final View view) {
        view.setVisibility(View.VISIBLE);
        int bottom = ((View) view.getParent()).getBottom();
        final ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, bottom - view.getTop(), 0);
        animator.setInterpolator(new OvershootInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setTranslationY(0);
            }
        });
        animator.setDuration(300);
        return animator;
    }
}
