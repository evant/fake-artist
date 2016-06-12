package me.tatarka.fakeartist.game.lobby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.inject.Inject;

import me.tatarka.fakeartist.Dagger;
import me.tatarka.fakeartist.R;
import me.tatarka.fakeartist.RetainActivity;
import me.tatarka.fakeartist.api.Event;
import me.tatarka.fakeartist.api.Game;
import me.tatarka.fakeartist.api.GameApi;
import me.tatarka.fakeartist.api.State;
import me.tatarka.fakeartist.game.main.GameActivity;
import me.tatarka.fakeartist.util.FragmentUtil;
import me.tatarka.loader.Loader;
import me.tatarka.loader.Result;
import me.tatarka.loader.RxLoader;

public class GameLobbyActivity extends RetainActivity implements QmDialogFragment.OnQmSetup {

    private static final String TAG = "GameLobbyActivity";
    private static final String EXTRA_USER_NAME = "user_name";
    private static final String EXTRA_ROOM_NAME = "room_name";
    private static final String TAG_QM = "qm";
    private static final String KEY_STATE = "state";

    public static Intent newIntentCreate(Context context, String userName) {
        return new Intent(context, GameLobbyActivity.class)
                .putExtra(EXTRA_USER_NAME, userName);
    }

    public static Intent newIntentJoin(Context context, String userName, String roomName) {
        return new Intent(context, GameLobbyActivity.class)
                .putExtra(EXTRA_USER_NAME, userName)
                .putExtra(EXTRA_ROOM_NAME, roomName);
    }

    @Inject
    GameApi api;
    private State state;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.gameComponent().inject(this);
        final String userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        setContentView(R.layout.lobby_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView listPlayers = (RecyclerView) findViewById(R.id.list_players);
        listPlayers.setLayoutManager(new LinearLayoutManager(this));
        final PlayersAdapter adapter = new PlayersAdapter(userName);
        listPlayers.setAdapter(adapter);
        final FloatingActionButton ready = (FloatingActionButton) findViewById(R.id.ready);
        final ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
        final TextView info = (TextView) findViewById(R.id.info);

        ready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state == null) {
                    return;
                }
                if (state.role() == State.Role.QM) {
                    FragmentUtil.showDialogFragment(getSupportFragmentManager(), new QmDialogFragment(), TAG_QM);
                } else {
                    api.ready(state);
                }
            }
        });

        loaderManager().init(0, RxLoader.create(api.connect()), new Loader.CallbacksAdapter<Result<Game>>() {
            @Override
            public void onLoaderResult(Result<Game> result) {
                if (result.isSuccess()) {
                    final Game game = result.getSuccess();
                    state = game.state;
                    toolbar.setTitle(game.state.roomName);
                    adapter.setState(game.state);
                    
                    progress.setVisibility(game.event == Event.LOADING ? View.VISIBLE : View.GONE);
                    info.setVisibility(game.event == Event.READY ? View.VISIBLE : View.GONE);
                    info.setText(game.state.role() == State.Role.QM 
                            ? R.string.waiting_for_other_players 
                            : R.string.waiting_for_quiz_master);
                    
                    if (game.event != Event.READY && game.state.players.size() >= Game.MIN_PLAYERS) {
                        ready.show();
                    } else {
                        ready.hide();
                    }
                    
                    if (game.event == Event.START) {
                        startActivity(GameActivity.newIntent(GameLobbyActivity.this, state));
                        finish();
                    }
                } else {
                    Throwable error = result.getError();
                    Log.e(TAG, error.getMessage(), error);
                    Snackbar.make(toolbar, error.getMessage(), Snackbar.LENGTH_INDEFINITE)
                            .setAction(R.string.go_back, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    finish();
                                }
                            }).show();
                }
            }
        }).start();

        if (savedInstanceState == null) {
            if (roomName == null) {
                api.createRoom(userName);
            } else {
                api.joinRoom(roomName, userName);
            }
        } else {
            state = savedInstanceState.getParcelable(KEY_STATE);
        }
    }

    @Override
    public void onQmSetup(String category, String title) {
        api.setup(state, category, title);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_STATE, state);
    }
}
