package app.MyApp.MyShellAuthorization.MyPermission;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import top.clspd.shellauthorization.R;

/**
 * Reusable helpers for special permissions that are granted through a system settings page.
 */
public final class PermissionHelper {

    private PermissionHelper() {
    }

    /**
     * @return whether the application currently has the "display over other apps" permission.
     */
    public static boolean hasOverlayPermission(Context context) {
        return Settings.canDrawOverlays(context);
    }

    /**
     * Ensures the overlay permission is granted. If it is missing, shows a confirmation dialog
     * using the supplied description and opens the system settings page when the user accepts.
     *
     * @param description text shown to the user explaining why the permission is needed.
     */
    public static void ensureOverlayPermission(Fragment fragment, CharSequence description) {
        ensureOverlayPermission(fragment.requireActivity(), description);
    }

    /**
     * @see #ensureOverlayPermission(Fragment, CharSequence)
     */
    public static void ensureOverlayPermission(Activity activity, CharSequence description) {
        if (hasOverlayPermission(activity)) return;
        new AlertDialog.Builder(activity)
                .setTitle(R.string.permission_overlay_required_title)
                .setMessage(description)
                .setPositiveButton(R.string.Yes,
                        (dialog, which) -> openOverlayPermissionSettings(activity))
                .setNegativeButton(R.string.No, null)
                .show();
    }

    private static void openOverlayPermissionSettings(Activity activity) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + activity.getPackageName()));
        try {
            activity.startActivity(intent);
            return;
        } catch (ActivityNotFoundException ignored) {
            // Fall through to the application details page.
        }

        Intent fallback = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()));
        try {
            activity.startActivity(fallback);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.permission_overlay_settings_unavailable,
                    Toast.LENGTH_SHORT).show();
        }
    }
}
