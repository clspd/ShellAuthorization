package app.MyApp.MyShellAuthorization.LawAndCopyright;

import static app.MyApp.MyShellAuthorization.MyUtility.RawResourceReader.readRawResource;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import top.clspd.shellauthorization.R;

public class LicenseViewerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_license_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView content = findViewById(R.id.textView_licenseContent);
        content.setText(readRawResource(this, getIntent().getIntExtra("identifier", 0)));

        findViewById(R.id.button).setOnClickListener(v -> {
            setResult(RESULT_OK);
            finish();
        });
        findViewById(R.id.button2).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
