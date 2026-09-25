package app.MyApp.MyCommon.MySupport.MyFragmentContainer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.lang.reflect.Method;

import top.clspd.shellauthorization.R;

public class FragmentDialogContainerActivity extends AppCompatActivity {

    private boolean closeOnBackdropClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fragment_dialog_container);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (savedInstanceState == null) {
            String className = getIntent().getStringExtra("Name");
            Bundle Parameters = getIntent().getBundleExtra("Parameters");
            closeOnBackdropClick = getIntent().getBooleanExtra("CloseOnBackdropClick", false);
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
                Log.e("FragmentDialogContainer", e.toString());
                finish();
                return;
            }
            Log.i("FragmentDialogContainer", "Fragment successfully created");
        }
        findViewById(R.id.overlay).setOnClickListener((v) -> { if (closeOnBackdropClick) finish(); });
        findViewById(R.id.dialog).setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    return true;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    return true;
                default:
                    return false;
            }
        });
        String title = getIntent().getStringExtra("Title");
        if (TextUtils.isEmpty(title)) title = getIntent().getStringExtra("Name");
        ((TextView)findViewById(R.id.textView_title)).setText(title);
        findViewById(R.id.button_close).setOnClickListener((v) -> finish());
    }

    public static Intent createIntent(Context ctx, Class<? extends Fragment> c, Bundle a, String title, boolean close_on_backdrop_click) {
        Intent intent = new Intent(ctx, FragmentDialogContainerActivity.class);
        intent.putExtra("Name", c.getName());
        intent.putExtra("Parameters", a);
        intent.putExtra("Title", title);
        intent.putExtra("CloseOnBackdropClick", close_on_backdrop_click);
        return intent;
    }
}