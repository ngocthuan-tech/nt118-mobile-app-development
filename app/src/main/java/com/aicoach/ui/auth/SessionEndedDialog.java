package com.aicoach.ui.auth;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.aicoach.R;
import com.aicoach.data.local.SessionEndReason;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/** DialogFragment keeps the explanation visible across rotation instead of a transient toast. */
public class SessionEndedDialog extends DialogFragment {
    public static final String TAG = "session-ended";
    public static SessionEndedDialog create(SessionEndReason reason) {
        SessionEndedDialog dialog = new SessionEndedDialog(); Bundle args = new Bundle();
        args.putString("reason", reason.name()); dialog.setArguments(args); return dialog;
    }
    @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
        String reason = requireArguments().getString("reason");
        boolean changedHere = SessionEndReason.PASSWORD_UPDATED_HERE.name().equals(reason);
        int message = changedHere ? R.string.session_password_updated_here
                : SessionEndReason.PASSWORD_CHANGED.name().equals(reason) ? R.string.session_password_changed : R.string.session_expired;
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(changedHere ? R.string.profile_password_success : R.string.session_ended_title)
                .setMessage(message).setPositiveButton(R.string.session_login_again, null).create();
    }
}
