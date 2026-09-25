package app.MyApp.MyShellAuthorization.MyPage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import top.clspd.shellauthorization.R;

public class MainPage extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager2 = findViewById(R.id.viewPager2);

        setupBottomNavigation();
        setupViewPager();

        if (!checkStartupConditions()) {
            finish();
            return;
        }

        AntiTamper.AntiTamper_ValueMustWithin2(0x7912dfa1, 2142310, 14634918, 0, android.os.Process.myPid());
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int index = indexOfMenuItem(item.getItemId());
            if (index >= 0 && viewPager2.getCurrentItem() != index) {
                viewPager2.setCurrentItem(index, true);
            }
            return true;
        });
    }

    private void setupViewPager() {
        viewPager2.setAdapter(new MainPagerAdapter(this));
        viewPager2.setOffscreenPageLimit(MainPagerAdapter.PAGE_COUNT - 1);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
            }
        });
    }

    private int indexOfMenuItem(int itemId) {
        return MainPagerAdapter.indexOfMenuItem(itemId);
    }

    private boolean checkStartupConditions() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_prefs_name), MODE_PRIVATE);
        boolean isLicenseAccepted = prefs.getBoolean("LicenseAccepted", false);
        if (!isLicenseAccepted) {
            Intent intent = new Intent(MainPage.this, LauncherActivity.class);
            startActivity(intent);
            return false;
        }
        return true;
    }
}
