package app.MyApp.MyShellAuthorization.MyFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity;
import app.MyApp.MyShellAuthorization.MyDataStructures.CommandTemplate;
import app.MyApp.MyShellAuthorization.MyDataStructures.CommandTemplateStore;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderDeclaration;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderRepository;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellsDatabase;
import top.clspd.shellauthorization.R;

public class EditTemplateFragment extends Fragment {

    public static final String ARG_TEMPLATE_NAME = "template_name";

    private static final String STATE_PROFILE = "state_profile";
    private static final String STATE_PROVIDER = "state_provider";

    private String templateName;
    private CommandTemplate.Profile profile = new CommandTemplate.Profile();
    private final List<ShellProviderDeclaration> providers = new ArrayList<>();
    private String selectedProviderId;
    private String pendingProviderId;

    private EditText nameInput;
    private TextInputEditText commandInput;
    private Spinner providerSpinner;
    private TextView profileSummary;
    private Button deleteButton;

    private final ActivityResultLauncher<Intent> profileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                CommandTemplate.Profile edited = CommandTemplate.Profile.fromJson(
                        result.getData().getStringExtra(EditUserProfileFragment.RESULT_PROFILE));
                if (edited != null) {
                    profile = edited;
                    updateProfileSummary();
                }
            });

    public EditTemplateFragment() {
        // Required empty public constructor
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual(0x11d0bec, 2766, 102);
            templateName = getArguments().getString(ARG_TEMPLATE_NAME);
            if (templateName != null && templateName.isEmpty()) templateName = null;
        }
        if (savedInstanceState != null) {
            CommandTemplate.Profile savedProfile =
                    CommandTemplate.Profile.fromJson(savedInstanceState.getString(STATE_PROFILE));
            if (savedProfile != null) profile = savedProfile;
            selectedProviderId = savedInstanceState.getString(STATE_PROVIDER);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_PROFILE, CommandTemplate.Profile.toJson(profile));
        outState.putString(STATE_PROVIDER, selectedProviderId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_template, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInput = view.findViewById(R.id.editTextTemplateName);
        commandInput = view.findViewById(R.id.textView_editShellCommand);
        providerSpinner = view.findViewById(R.id.spinnerShellProvider);
        profileSummary = view.findViewById(R.id.textView_userProfileContent);
        deleteButton = view.findViewById(R.id.button_delete);

        view.findViewById(R.id.button_cancel).setOnClickListener(v -> requireActivity().finish());
        view.findViewById(R.id.button_save).setOnClickListener(v -> save());
        view.findViewById(R.id.button_delete).setOnClickListener(v -> confirmDelete());
        view.findViewById(R.id.button_editUserProfile).setOnClickListener(v -> openProfileEditor());

        if (templateName == null) {
            deleteButton.setVisibility(View.GONE);
        } else {
            nameInput.setText(templateName);
        }

        updateProfileSummary();
        loadProviders();
        if (savedInstanceState == null && templateName != null) {
            loadTemplate();
        }
    }

    private void loadProviders() {
        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            List<ShellProviderDeclaration> loaded;
            try {
                loaded = ShellsDatabase.get(appContext).shellProviderDao().getAll();
            } catch (RuntimeException e) {
                loaded = new ArrayList<>();
            }
            if (!isAdded()) return;
            List<ShellProviderDeclaration> result = loaded;
            requireActivity().runOnUiThread(() -> {
                if (result.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.edit_template_no_shell_provider_title))
                            .setMessage(getString(R.string.edit_template_no_shell_provider_content))
                            .setPositiveButton(getString(R.string.Close), null)
                            .setOnDismissListener(dialog -> requireActivity().finish())
                            .show();
                    return;
                }
                providers.clear();
                providers.addAll(result);
                List<String> names = new ArrayList<>();
                for (ShellProviderDeclaration provider : result) {
                    names.add(ShellProviderRepository.displayName(provider));
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, names);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                providerSpinner.setAdapter(adapter);
                providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedProviderId = providers.get(position).name;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
                applyProviderSelection();
            });
        }).start();
    }

    private void applyProviderSelection() {
        if (providers.isEmpty()) return;
        int index = 0;
        String wanted = pendingProviderId != null ? pendingProviderId : selectedProviderId;
        if (wanted != null) {
            for (int i = 0; i < providers.size(); i++) {
                if (wanted.equals(providers.get(i).name)) {
                    index = i;
                    break;
                }
            }
        }
        providerSpinner.setSelection(index);
        selectedProviderId = providers.get(index).name;
        pendingProviderId = null;
    }

    private void loadTemplate() {
        Context appContext = requireContext().getApplicationContext();
        final String name = templateName;
        new Thread(() -> {
            CommandTemplate loaded;
            try {
                loaded = CommandTemplateStore.get().read(name);
            } catch (Exception e) {
                loaded = null;
            }
            if (!isAdded()) return;
            CommandTemplate result = loaded;
            requireActivity().runOnUiThread(() -> {
                if (result == null) {
                    Toast.makeText(requireContext(), R.string.edit_template_invalid_file,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                nameInput.setText(name);
                commandInput.setText(result.command);
                if (result.profile != null) profile = result.profile;
                updateProfileSummary();
                pendingProviderId = result.provider;
                applyProviderSelection();
            });
        }).start();
    }

    private void updateProfileSummary() {
        if (profileSummary == null) return;
        String type = profile == null ? CommandTemplate.Profile.TYPE_DEFAULT : profile.type;
        if (CommandTemplate.Profile.TYPE_TEMPLATE.equals(type)) {
            String name = profile == null || profile.template == null ? "" : profile.template;
            profileSummary.setText(getString(R.string.edit_template_user_profile_use_template, name));
        } else if (CommandTemplate.Profile.TYPE_CUSTOM.equals(type)) {
            profileSummary.setText(R.string.edit_template_user_profile_custom);
        } else {
            profileSummary.setText(R.string.edit_template_user_profile_default);
        }
    }

    private void openProfileEditor() {
        Bundle args = new Bundle();
        args.putString(EditUserProfileFragment.ARG_PROFILE, CommandTemplate.Profile.toJson(profile));
        profileLauncher.launch(FragmentDialogContainerActivity.createIntent(requireContext(),
                EditUserProfileFragment.class, args,
                EditUserProfileFragment.getTitle(requireContext()), false));
    }

    private void save() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString();
        if (name.isEmpty()) {
            nameInput.setError(getString(R.string.edit_template_invalid_name));
            return;
        }
        nameInput.setError(null);

        if (selectedProviderId == null) {
            Toast.makeText(requireContext(), R.string.edit_template_no_provider,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String command = commandInput.getText() == null ? "" : commandInput.getText().toString();
        if (command.isEmpty()) {
            commandInput.setError(getString(R.string.edit_template_command_required));
            return;
        }
        commandInput.setError(null);

        CommandTemplate template = new CommandTemplate();
        template.version = CommandTemplate.VERSION;
        template.provider = selectedProviderId;
        template.profile = profile == null ? new CommandTemplate.Profile() : profile.copy();
        template.command = command;

        checkConflictAndPersist(name, template);
    }

    private void checkConflictAndPersist(String name, CommandTemplate template) {
        final boolean renamed = templateName == null || !templateName.equals(name);
        new Thread(() -> {
            boolean invalid = false;
            boolean conflict = false;
            try {
                File file = CommandTemplateStore.get().fileFor(name);
                if (file == null) {
                    invalid = true;
                } else {
                    conflict = renamed && file.isFile();
                }
            } catch (RuntimeException ignored) {
            }
            if (!isAdded()) return;
            boolean invalidName = invalid;
            boolean ask = conflict;
            requireActivity().runOnUiThread(() -> {
                if (invalidName) {
                    nameInput.setError(getString(R.string.edit_template_invalid_name));
                    return;
                }
                if (ask) {
                    new AlertDialog.Builder(requireContext())
                            .setMessage(getString(R.string.edit_template_overwrite_confirm, name))
                            .setPositiveButton(getString(R.string.Yes),
                                    (dialog, which) -> persist(name, template))
                            .setNegativeButton(getString(R.string.No), null)
                            .show();
                } else {
                    persist(name, template);
                }
            });
        }).start();
    }

    private void persist(String name, CommandTemplate template) {
        final String previousName = templateName;
        new Thread(() -> {
            boolean success;
            try {
                CommandTemplateStore store = CommandTemplateStore.get();
                store.write(name, template);
                if (previousName != null && !previousName.equals(name)) {
                    store.delete(previousName);
                }
                success = true;
            } catch (Exception e) {
                success = false;
            }
            if (!isAdded()) return;
            boolean result = success;
            requireActivity().runOnUiThread(() -> {
                if (result) {
                    requireActivity().finish();
                } else {
                    Toast.makeText(requireContext(), R.string.edit_template_save_failed,
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void confirmDelete() {
        if (templateName == null) return;
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.edit_template_delete_confirm)
                .setPositiveButton(getString(R.string.Yes), (dialog, which) -> deleteTemplate())
                .setNegativeButton(getString(R.string.No), null)
                .show();
    }

    private void deleteTemplate() {
        final String name = templateName;
        new Thread(() -> {
            boolean success;
            try {
                success = CommandTemplateStore.get().delete(name);
            } catch (RuntimeException e) {
                success = false;
            }
            if (!isAdded()) return;
            boolean result = success;
            requireActivity().runOnUiThread(() -> {
                if (result) {
                    requireActivity().finish();
                } else {
                    Toast.makeText(requireContext(), R.string.edit_template_save_failed,
                            Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
}
