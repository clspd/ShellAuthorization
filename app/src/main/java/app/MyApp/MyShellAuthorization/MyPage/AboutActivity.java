package app.MyApp.MyShellAuthorization.MyPage;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import top.clspd.shellauthorization.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_about);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView t = findViewById(R.id.textView_appVersion);
        PackageManager pm = getPackageManager();
        try {
            PackageInfo app = pm.getPackageInfo(getPackageName(), 0);

            t.setText(app.versionName + " (" + app.getLongVersionCode() + ")");
        } catch (PackageManager.NameNotFoundException e) {
            t.setText("N/A");
        }

        findViewById(R.id.button_learnMore).setOnClickListener(v -> {
            List<String> pkg = Arrays.asList(getPackageName().split("\\."));
            Collections.reverse(pkg);
            String reversed = String.join(".", pkg);
            Uri webpage = Uri.parse("https://" + reversed + "/introduction");
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(AboutActivity.this, getString(R.string.cannot_open_page), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}