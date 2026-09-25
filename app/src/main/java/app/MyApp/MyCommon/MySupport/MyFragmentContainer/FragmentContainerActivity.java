package app.MyApp.MyCommon.MySupport.MyFragmentContainer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;

import top.clspd.shellauthorization.R;

public class FragmentContainerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fragment_container);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (savedInstanceState == null) {
            String className = getIntent().getStringExtra("Name");
            Bundle Parameters = getIntent().getBundleExtra("Parameters");
            if (className == null || Parameters == null) {
                finish();
                return;
            }
            try {
                Class<?> c = Class.forName(className);
                Method m = c.getDeclaredMethod("newInstance", Bundle.class);
                Fragment fragment = (Fragment) m.invoke(null, Parameters);
                if (null == fragment) throw new Exception("Cannot create fragment");
                getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
            } catch (Exception e) {
                finish();
                return;
            }
            Log.i("FragmentContainer", "Fragment successfully created");
        }
    }

    public static Intent createIntent(Context ctx, Class<? extends Fragment> c, Bundle a) {
        Intent intent = new Intent(ctx, FragmentContainerActivity.class);
        intent.putExtra("Name", c.getName());
        intent.putExtra("Parameters", a);
        return intent;
    }
}