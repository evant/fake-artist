package me.tatarka.fakeartist;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.widget.EditText;

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
        return new AlertDialog.Builder(getContext())
                .setTitle("Room")
                .setView(R.layout.start_input_room_dialog)
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
