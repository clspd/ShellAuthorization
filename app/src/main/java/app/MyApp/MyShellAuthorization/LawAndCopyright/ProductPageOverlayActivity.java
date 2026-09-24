package app.MyApp.MyShellAuthorization.LawAndCopyright;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class ProductPageOverlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_page_overlay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.product_page_overlay).setOnClickListener(v -> finish());
        Button btn = findViewById(R.id.button_openHomepage);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> pkg = Arrays.asList(getPackageName().split("\\."));
                Collections.reverse(pkg);
                String reversed = String.join(".", pkg);
                String myStr = "https://" + reversed + "/";
                Uri webpage = Uri.parse(myStr);
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(ProductPageOverlayActivity.this, getString(R.string.cannot_open_page), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}