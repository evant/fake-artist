package me.tatarka.fakeartist.util;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

public class FragmentUtil {

    /**
     * Helper that prevents a dialog fragment being shown more than once.
     */
    public static void showDialogFragment(FragmentManager fm, DialogFragment fragment, String tag) {
        if (fm.findFragmentByTag(tag) == null) {
            fragment.show(fm, tag);
            fm.executePendingTransactions();
        }
    }
}
