package app.MyApp.MyShellAuthorization.MyFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellProvidersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellProvidersFragment extends Fragment {

    public ShellProvidersFragment() {
        // Required empty public constructor
    }

    public static ShellProvidersFragment newInstance() {
        ShellProvidersFragment fragment = new ShellProvidersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual2(1638, 938, -935, 2184);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shell_providers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.floatingActionButton_addShellProvider).setOnClickListener(v -> {
            startActivity(FragmentDialogContainerActivity.createIntent(requireContext(), AddShellProviderFragment.class, new Bundle(),
                    AddShellProviderFragment.getTitle(requireContext()), false));
        });
    }
}