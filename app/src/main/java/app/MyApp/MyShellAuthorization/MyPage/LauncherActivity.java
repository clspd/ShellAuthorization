package app.MyApp.MyShellAuthorization.MyPage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import top.clspd.shellauthorization.R;

public class LauncherActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 1000;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_launcher);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        prefs = getSharedPreferences(getString(R.string.app_prefs_name), MODE_PRIVATE);
        new Handler(Looper.getMainLooper()).postDelayed(this::checkAppState, SPLASH_DELAY);
    }

    private void checkAppState() {
        boolean isLicenseAccepted = prefs.getBoolean("LicenseAccepted", false);
        if (!isLicenseAccepted) {
            AskAcceptLicense();
            return;
        }

        Intent intent = new Intent(LauncherActivity.this, MainPage.class);
        startActivity(intent);
        finish();
    }

    private void AskAcceptLicense() {
        // TODO: add legeital real license
        new android.app.AlertDialog.Builder(this)
            .setTitle("License")
            .setMessage("By continue using the application, you agree to the license.")
            .setCancelable(false)
            .setPositiveButton("Accept", (dialog, which) -> {
                prefs.edit().putBoolean("LicenseAccepted", true).apply();
                Intent intent = new Intent(LauncherActivity.this, MainPage.class);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Decline", (dialog, which) -> {
                finishAffinity();
            })
            .show();
    }
}