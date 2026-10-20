package app.MyApp.MyShellAuthorization.MySecurity;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import top.clspd.shellauthorization.R;

public class OpenOnlineContentHelper {
    public static void OpenOnlineContent(Context ctx, String u, @Nullable DialogInterface.OnClickListener onAccepted, @Nullable DialogInterface.OnClickListener onDeclined) {
        new AlertDialog.Builder(ctx)
                .setTitle(R.string.open_online_content_confirm_title)
                .setMessage(ctx.getString(R.string.open_online_content_confirm_message, u))
                .setPositiveButton(R.string.Yes, onAccepted)
                .setNegativeButton(R.string.No, onDeclined)
                .show();
    }
}
