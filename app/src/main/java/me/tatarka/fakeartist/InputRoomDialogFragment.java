package me.tatarka.fakeartist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import me.tatarka.fakeartist.game.lobby.GameLobbyActivity;

public class InputRoomDialogFragment extends AppCompatDialogFragment {

    private static final String EXTRA_USER_NAME = "user_name";

    public static InputRoomDialogFragment newInstance(String userName) {
        Bundle args = new Bundle();
        args.putString(EXTRA_USER_NAME, userName);
        InputRoomDialogFragment fragment = new InputRoomDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.start_input_room_dialog, null);
        EditText name = (EditText) view.findViewById(R.id.input);
        name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    onPositiveButtonClick();
                    return true;
                }
                return false;
            }
        });
        return new AlertDialog.Builder(getContext())
                .setTitle("Room")
                .setView(view)
                .setPositiveButton(R.string.join, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPositiveButtonClick();
                    }
                })
                .create();
    }

    private void onPositiveButtonClick() {
        EditText input = (EditText) getDialog().findViewById(R.id.input);
        String userName = getArguments().getString(EXTRA_USER_NAME);
        String roomName = input.getText().toString();
        startActivity(GameLobbyActivity.newIntentJoin(getContext(), userName, roomName));
    }
}
