package me.tatarka.fakeartist;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;
import java.util.Collections;

import me.tatarka.fakeartist.api.State;
import me.tatarka.fakeartist.game.lobby.GameLobbyActivity;
import me.tatarka.fakeartist.game.main.Drawing;
import me.tatarka.fakeartist.game.main.GameActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_DIALOG = "dialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_main);

        final EditText textUserName = (EditText) findViewById(R.id.username);
        final Button create = (Button) findViewById(R.id.create);
        final Button join = (Button) findViewById(R.id.join);

        textUserName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                create.setEnabled(!TextUtils.isEmpty(s));
                join.setEnabled(!TextUtils.isEmpty(s));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = textUserName.getText().toString();
                startActivity(GameLobbyActivity.newIntentCreate(v.getContext(), userName));
            }
        });

        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                if (fm.findFragmentByTag(TAG_DIALOG) == null) {
                    String userName = textUserName.getText().toString();
                    InputRoomDialogFragment.newInstance(userName).show(fm, TAG_DIALOG);
                    fm.executePendingTransactions();
                }
            }
        });

        findViewById(R.id.draw).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                State.Builder state = new State.Builder();
                state.roomId = "abcdef";
                state.roomName = "abcdef";
                state.userName = "first";
                state.players = Arrays.asList("first");
                state.drawing = new Drawing(new int[]{Color.RED}, Collections.<Drawing.Line>emptyList());
                startActivity(GameActivity.newIntent(v.getContext(), state.build()));
            }
        });
    }
}
