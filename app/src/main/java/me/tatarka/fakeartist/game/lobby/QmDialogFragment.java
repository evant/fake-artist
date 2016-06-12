package me.tatarka.fakeartist.game.lobby;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import me.tatarka.fakeartist.R;

public class QmDialogFragment extends AppCompatDialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.qm_start, null);
        EditText title = (EditText) view.findViewById(R.id.edit_title);
        title.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    onPositiveButtonClick();
                    return true;
                }
                return false;
            }
        });
        return new AlertDialog.Builder(getContext(), getTheme())
                .setTitle(R.string.setup)
                .setView(view)
                .setPositiveButton(R.string.ready, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPositiveButtonClick();
                    }
                })
                .create();
    }
    
    private void onPositiveButtonClick() {
        String category = ((EditText) getDialog().findViewById(R.id.edit_category)).getText().toString();
        String title = ((EditText) getDialog().findViewById(R.id.edit_title)).getText().toString();
        ((OnQmSetup) getHost()).onQmSetup(category, title);
    }

    public interface OnQmSetup {
        void onQmSetup(String category, String title);
    }
}
