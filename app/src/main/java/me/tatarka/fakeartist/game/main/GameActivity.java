package me.tatarka.fakeartist.game.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import javax.inject.Inject;

import me.tatarka.fakeartist.Dagger;
import me.tatarka.fakeartist.R;
import me.tatarka.fakeartist.RetainActivity;
import me.tatarka.fakeartist.api.Game;
import me.tatarka.fakeartist.api.GameApi;
import me.tatarka.fakeartist.api.State;
import me.tatarka.loader.Loader;
import me.tatarka.loader.Result;
import me.tatarka.loader.RxLoader;

public class GameActivity extends RetainActivity {

    private static final String TAG = "GameActivity";
    private static final String EXTRA_STATE = "state";

    public static Intent newIntent(Context context, State state) {
        return new Intent(context, GameActivity.class)
                .putExtra(EXTRA_STATE, state);
    }

    @Inject
    GameApi api;
    private State state;
    private BottomSheetBehavior bottomSheet;
    private DrawingView drawingView;
    private TextView yourTurn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.gameComponent().inject(this);
        setContentView(R.layout.game_main);
        state = getIntent().getParcelableExtra(EXTRA_STATE);

        drawingView = (DrawingView) findViewById(R.id.drawing);
        final ImageButton undo = (ImageButton) findViewById(R.id.button_undo);
        final ImageButton done = (ImageButton) findViewById(R.id.button_done);
        final View overlay = findViewById(R.id.overlay);
        final View info = findViewById(R.id.info);
        final View controls = findViewById(R.id.controls);
        bottomSheet = BottomSheetBehavior.from(info);
        TextView category = (TextView) info.findViewById(R.id.category);
        TextView title = (TextView) info.findViewById(R.id.title);
        TextView role = (TextView) info.findViewById(R.id.role);
        yourTurn = (TextView) info.findViewById(R.id.your_turn);

        if (savedInstanceState == null) {
            drawingView.setDrawing(state.drawing);
            drawingView.setPlayer(state.players.indexOf(state.userName));
            // Delay so that it animates up
            info.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }, 300);
        }

        category.setText(getString(R.string.info_category, state.category));
        if (state.role() == State.Role.FAKE) {
            title.setVisibility(View.GONE);
        } else {
            title.setText(getString(R.string.info_title, state.title));
        }
        switch (state.role()) {
            case QM:
                role.setText(getString(R.string.info_role, getString(R.string.quiz_master)));
                break;
            case FAKE:
                role.setText(getString(R.string.info_role, getString(R.string.fake_artist)));
                break;
            case ARTIST:
                role.setText(getString(R.string.info_role, getString(R.string.artist)));
                break;
        }
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else if (bottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });
        bottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    overlay.animate()
                            .alpha(0)
                            .setDuration(300)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    overlay.setVisibility(View.GONE);
                                }
                            });
                } else {
                    overlay.setVisibility(View.VISIBLE);
                    overlay.animate()
                            .alpha(1)
                            .setDuration(300)
                            .setListener(null);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        drawingView.setOnLineDoneListener(new DrawingView.OnLineDoneListener() {
            @Override
            public void onLineDone() {
                controls.setVisibility(View.VISIBLE);
                int bottom = ((View) controls.getParent()).getBottom();
                controls.setTranslationY(bottom - controls.getTop());
                controls.animate()
                        .translationY(0)
                        .setDuration(300)
                        .setInterpolator(new OvershootInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                controls.setTranslationY(0);
                            }
                        });
            }
        });

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearCurrentPoints();
                int bottom = ((View) controls.getParent()).getBottom();
                controls.animate()
                        .translationY(bottom - controls.getTop())
                        .setDuration(300)
                        .setInterpolator(new AccelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                controls.setVisibility(View.INVISIBLE);
                                controls.setTranslationY(0);
                            }
                        });
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.commitCurrentPoints();
                drawingView.setEnabled(false);
                api.finishTurn(drawingView.getDrawing());
                int bottom = ((View) controls.getParent()).getBottom();
                controls.animate()
                        .translationY(bottom - controls.getTop())
                        .setDuration(300)
                        .setInterpolator(new AccelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                controls.setVisibility(View.INVISIBLE);
                                controls.setTranslationY(0);
                            }
                        });
            }
        });

        loaderManager().init(0, RxLoader.create(api.connect()), new Loader.CallbacksAdapter<Result<Game>>() {
            @Override
            public void onLoaderResult(Result<Game> result) {
                if (result.isSuccess()) {
                    Game game = result.getSuccess();
                    state = game.state;
                    boolean isYourTurn = state.userName.equals(state.turn);
                    drawingView.setEnabled(isYourTurn);
                    drawingView.setDrawing(game.state.drawing);
                    yourTurn.setVisibility(isYourTurn ? View.VISIBLE : View.GONE);
                } else {
                    Throwable error = result.getError();
                    Log.e(TAG, error.getMessage(), error);
                    Snackbar.make(drawingView, error.getMessage(), Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.go_back, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            });
                }
            }
        }).start();
    }
    
    @Override
    public void onBackPressed() {
        if (bottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
