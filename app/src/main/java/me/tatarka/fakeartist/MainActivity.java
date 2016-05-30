package me.tatarka.fakeartist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import me.tatarka.fakeartist.game.lobby.GameLobbyActivity;
import me.tatarka.fakeartist.game.main.GameActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_DIALOG = "dialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_main);

        final EditText textUserName = (EditText) findViewById(R.id.username);
        Button create = (Button) findViewById(R.id.create);
        Button join = (Button) findViewById(R.id.join);

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

        //Just for testing
        Button draw = (Button) findViewById(R.id.draw);
        draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), GameActivity.class));
            }
        });
    }
}
