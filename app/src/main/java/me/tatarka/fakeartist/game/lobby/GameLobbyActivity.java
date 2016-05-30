package me.tatarka.fakeartist.game.lobby;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.TooManyListenersException;

import javax.inject.Inject;

import me.tatarka.fakeartist.Dagger;
import me.tatarka.fakeartist.R;
import me.tatarka.fakeartist.RetainActivity;
import me.tatarka.fakeartist.api.GameApi;
import me.tatarka.fakeartist.api.Room;
import me.tatarka.loader.Loader;
import me.tatarka.loader.Result;
import me.tatarka.loader.RxLoader;

public class GameLobbyActivity extends RetainActivity {

    private static final String TAG = "GameLobbyActivity";
    private static final String EXTRA_USER_NAME = "user_name";
    private static final String EXTRA_ROOM_NAME = "room_name";

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dagger.gameComponent().inject(this);
        String userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        String roomName = getIntent().getStringExtra(EXTRA_ROOM_NAME);
        setContentView(R.layout.lobby_main);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RecyclerView listPlayers = (RecyclerView) findViewById(R.id.list_players);
        listPlayers.setLayoutManager(new LinearLayoutManager(this));
        final PlayersAdapter adapter = new PlayersAdapter(userName);
        listPlayers.setAdapter(adapter);

        rx.Observable<Room> observable = roomName == null
                ? api.connectAndCreateRoom(userName) : api.connectAndJoinRoom(roomName, userName);

        loaderManager().init(0, RxLoader.create(observable), new Loader.CallbacksAdapter<Result<Room>>() {
            @Override
            public void onLoaderResult(Result<Room> result) {
                if (result.isSuccess()) {
                    Room room = result.getSuccess();
                    toolbar.setTitle(room.name);
                    adapter.setRoom(room);
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
    }
}
