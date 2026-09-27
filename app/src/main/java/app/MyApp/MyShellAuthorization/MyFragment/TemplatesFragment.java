package app.MyApp.MyShellAuthorization.MyFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TemplatesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TemplatesFragment extends Fragment {

    public TemplatesFragment() {
        // Required empty public constructor
    }

    public static TemplatesFragment newInstance() {
        TemplatesFragment fragment = new TemplatesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustWithin2(320480663, 19390801, 20260925, 310088640, 360720960);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_templates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.floatingActionButton_addTemplate).setOnClickListener(v -> {
            startActivity(app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity.createIntent(
                    requireContext(), EditTemplateFragment.class, new Bundle(), EditTemplateFragment.getTitle(requireContext()), false));
        });
    }
}