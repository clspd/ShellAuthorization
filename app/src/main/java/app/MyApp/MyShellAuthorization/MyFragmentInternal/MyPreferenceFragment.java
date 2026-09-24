package app.MyApp.MyShellAuthorization.MyFragmentInternal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import app.MyApp.MyShellAuthorization.LawAndCopyright.LicenseViewerActivity;
import app.MyApp.MyShellAuthorization.LawAndCopyright.OpenSourceLicensesActivity;
import app.MyApp.MyShellAuthorization.LawAndCopyright.PermissionExplanationActivity;
import app.MyApp.MyShellAuthorization.LawAndCopyright.ProductPageOverlayActivity;
import app.MyApp.MyShellAuthorization.MyPage.AboutActivity;
import app.MyApp.MyShellAuthorization.MyPage.LauncherActivity;
import top.clspd.shellauthorization.R;

public class MyPreferenceFragment extends PreferenceFragmentCompat {

    private static final String LICENSE_ACCEPTED_KEY = "LicenseAccepted";
    private static final String LICENSE_ACCEPT = "accept";
    private static final String LICENSE_DECLINE = "decline";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        try {
            findPreference("accept_license").setOnPreferenceChangeListener((preference, newValue) -> {
                boolean accepted = LICENSE_ACCEPT.equals(newValue);
                appPrefs().edit().putBoolean(LICENSE_ACCEPTED_KEY, accepted).apply();
                if (!accepted) {
                    Intent intent = new Intent(requireContext(), LauncherActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                return true;
            });

            findPreference("open_privacy_policy").setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), LicenseViewerActivity.class);
                intent.putExtra("identifier", R.raw.privacypolicy);
                startActivity(intent);
                return true;
            });

            findPreference("about_app").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), AboutActivity.class));
                return true;
            });

            findPreference("open_source_license").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), OpenSourceLicensesActivity.class));
                return true;
            });

            findPreference("permission_explain").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), PermissionExplanationActivity.class));
                return true;
            });

            findPreference("product_main_page").setOnPreferenceClickListener(preference -> {
                startActivity(new Intent(requireContext(), ProductPageOverlayActivity.class));
                return true;
            });

            findPreference("feedback").setOnPreferenceClickListener(preference -> {
                List<String> pkg = Arrays.asList(requireContext().getPackageName().split("\\."));
                Collections.reverse(pkg);
                String reversed = String.join(".", pkg);
                Uri webpage = Uri.parse("https://" + reversed + "/feedback");
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), getString(R.string.cannot_open_page), Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }
        catch (NullPointerException ignored) {
            Toast.makeText(requireContext(), "BAD SETTINGS!!", Toast.LENGTH_SHORT).show();
        }

        syncState();
    }

    @Override
    public void onResume() {
        super.onResume();
        syncState();
    }

    private SharedPreferences appPrefs() {
        return requireContext().getSharedPreferences(getString(R.string.app_prefs_name), Context.MODE_PRIVATE);
    }

    private void syncState() {
        ListPreference acceptLicense = findPreference("accept_license");
        if (acceptLicense != null) {
            boolean accepted = appPrefs().getBoolean(LICENSE_ACCEPTED_KEY, false);
            acceptLicense.setValue(accepted ? LICENSE_ACCEPT : LICENSE_DECLINE);
        }
    }
}
