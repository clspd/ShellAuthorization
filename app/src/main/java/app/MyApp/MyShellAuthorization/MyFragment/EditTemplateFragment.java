package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderDeclaration;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellsDatabase;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditTemplateFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditTemplateFragment extends Fragment {

    private static final String ARG_TEMPLATE_ID = "template_id";
    private String template_id;
    private String shellProviderName;

    public EditTemplateFragment() {
        // Required empty public constructor
        if (template_id == null) template_id = "";
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_edit_template_title);
    }

    public static EditTemplateFragment newInstance(Bundle args) {
        EditTemplateFragment fragment = new EditTemplateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual(0x11d0bec, 2766, 102);
            template_id = getArguments().getString(ARG_TEMPLATE_ID);
            if (template_id == null || template_id.isEmpty()) {
                template_id = UUID.randomUUID().toString(); // create new template
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_template, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Spinner spinner = view.findViewById(R.id.spinnerShellProvider);
        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            List<ShellProviderDeclaration> providers = ShellsDatabase.get(appContext).shellProviderDao().getAll();
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (providers.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.edit_template_no_shell_provider_title))
                            .setMessage(getString(R.string.edit_template_no_shell_provider_content))
                            .setPositiveButton(getString(R.string.Close), null)
                            .setOnDismissListener(dialog -> requireActivity().finish())
                            .show();
                    return;
                }
                List<String> names = new ArrayList<>();
                for (ShellProviderDeclaration provider : providers) {
                    names.add(app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderRepository.displayName(provider));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                if (!names.isEmpty()) {
                    spinner.setSelection(0);
                    shellProviderName = names.get(0);
                }
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                        shellProviderName = names.get(position);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            });
        }).start();

        view.findViewById(R.id.button_editUserProfile).setOnClickListener(v -> {
            // TODO: pass args and get result...
            // TODO: this should be refactored to a DialogFragment, not an activity
            // so that the data and state can be easily passed between them
            startActivity(app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity.createIntent(
                    requireContext(), EditUserProfileFragment.class, new Bundle(), EditUserProfileFragment.getTitle(requireContext()), false));
        });
    }
}
