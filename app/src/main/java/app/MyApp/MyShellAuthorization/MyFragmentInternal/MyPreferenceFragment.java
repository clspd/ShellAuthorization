package app.MyApp.MyShellAuthorization.MyFragmentInternal;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import top.clspd.shellauthorization.R;

public class MyPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}
