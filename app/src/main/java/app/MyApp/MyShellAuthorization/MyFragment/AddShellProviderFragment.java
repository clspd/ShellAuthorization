package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddShellProviderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddShellProviderFragment extends Fragment {

    public AddShellProviderFragment() {
        // Required empty public constructor
    }

    public static AddShellProviderFragment newInstance(Bundle bundle) {
        AddShellProviderFragment fragment = new AddShellProviderFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_add_shell_provider_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual2(982691035, 1082376, 1868812, 7035844);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_shell_provider, container, false);
    }
}