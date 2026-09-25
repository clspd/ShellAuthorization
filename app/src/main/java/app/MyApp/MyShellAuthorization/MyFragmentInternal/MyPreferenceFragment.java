package app.MyApp.MyShellAuthorization.MyFragmentInternal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.SignalSender;
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

    private final ActivityResultLauncher<Intent> licenseViewerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> applyLicenseDecision(result.getResultCode() == Activity.RESULT_OK));

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        try {
            findPreference("ignore_bo").setOnPreferenceClickListener(preference -> {
                PowerManager powerManager = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
                if (powerManager.isIgnoringBatteryOptimizations(requireContext().getPackageName())) {
                    Toast.makeText(requireContext(), getString(R.string.ignore_battery_optimization_already), Toast.LENGTH_LONG).show();
                    return true;
                }
                // fuck you google
                @SuppressLint("BatteryLife") Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                startActivity(intent);
                return true;
            });

            findPreference("accept_license").setOnPreferenceChangeListener((preference, newValue) -> {
                applyLicenseDecision(LICENSE_ACCEPT.equals(newValue));
                return true;
            });

            findPreference("open_privacy_policy").setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(requireContext(), LicenseViewerActivity.class);
                intent.putExtra("identifier", R.raw.privacypolicy);
                licenseViewerLauncher.launch(intent);
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

            findPreference("close_app").setOnPreferenceClickListener(preference -> {
                SignalSender.SendSignal(android.os.Process.myPid(), 9);
                Toast.makeText(requireContext(), "Cannot close the app!!!", Toast.LENGTH_SHORT).show();
                return true;
            });

            findPreference("uninstall_app").setOnPreferenceClickListener(preference -> {
                new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.uninstall_the_app))
                    .setMessage(getString(R.string.uninstall_the_app_confirm))
                    .setPositiveButton(getString(R.string.Yes), (dialog, which) -> {
                        Uri packageUri = Uri.parse("package:" + requireContext().getPackageName());
                        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE);
                        uninstallIntent.setData(packageUri);
                        uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        requireContext().startActivity(uninstallIntent, null);
                    })
                    .setNegativeButton(getString(R.string.No), (dialog, which) -> {})
                    .show();
                return true;
            });

            AntiTamper.AntiTamper_ValueMustEqual2(2031280033, 15966981, 810247, android.os.Process.myPid());
        }
        catch (NullPointerException ignored) {
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

    private void applyLicenseDecision(boolean accepted) {
        appPrefs().edit().putBoolean(LICENSE_ACCEPTED_KEY, accepted).apply();
        if (!accepted) {
            Intent intent = new Intent(requireContext(), LauncherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        syncState();
    }

    private void syncState() {
        ListPreference acceptLicense = findPreference("accept_license");
        if (acceptLicense != null) {
            boolean accepted = appPrefs().getBoolean(LICENSE_ACCEPTED_KEY, false);
            acceptLicense.setValue(accepted ? LICENSE_ACCEPT : LICENSE_DECLINE);
        }
    }
}
