package app.MyApp.MyShellAuthorization.MyPage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.MyApp.MyShellAuthorization.LawAndCopyright.LicenseViewerActivity;
import top.clspd.shellauthorization.R;

public class UserFirstUseLicenseShowActivity extends AppCompatActivity {

    private static final int FLOW_USER = 1;
    private static final int FLOW_PRIVACY = 2;
    private int currentFlow = 0;

    private Button btnUser;
    private Button btnPrivacy;
    private Button btnAll;
    private Button btnDecline;

    private static final boolean userMustReadLicenseBeforeContinue = false;

    private final ActivityResultLauncher<Intent> licenseLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                    return;
                }

                if (currentFlow == FLOW_USER) {
                    btnPrivacy.setEnabled(true);
                } else if (currentFlow == FLOW_PRIVACY) {
                    btnAll.setEnabled(true);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_first_use_license_show);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnUser = findViewById(R.id.button_license_dialog_user);
        btnPrivacy = findViewById(R.id.button_license_dialog_privacy);
        btnAll = findViewById(R.id.button_license_dialog_all);
        btnDecline = findViewById(R.id.button_license_dialog_decline);

        btnUser.setOnClickListener(v -> {
            currentFlow = FLOW_USER;
            Intent intent = new Intent(this, LicenseViewerActivity.class);
            intent.putExtra("identifier", R.raw.userlicense);
            licenseLauncher.launch(intent);
        });

        btnPrivacy.setOnClickListener(v -> {
            currentFlow = FLOW_PRIVACY;
            Intent intent = new Intent(this, LicenseViewerActivity.class);
            intent.putExtra("identifier", R.raw.privacypolicy);
            licenseLauncher.launch(intent);
        });

        btnAll.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });

        btnDecline.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });

        if (!userMustReadLicenseBeforeContinue) {
            btnUser.setEnabled(true);
            btnPrivacy.setEnabled(true);
            btnAll.setEnabled(true);
        }
    }
}