package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.LinkedHashMap;
import java.util.Map;

import app.MyApp.MyShellAuthorization.MyDataStructures.ProfilesDatabase;
import app.MyApp.MyShellAuthorization.MyDataStructures.UserProfileConfig;
import app.MyApp.MyShellAuthorization.MyDataStructures.UserProfileTemplate;
import top.clspd.shellauthorization.R;

public class EditUserProfileTemplateFragment extends Fragment {

    public static final String ARG_NAME = "template_name";

    private String originalName;
    private TextInputLayout nameLayout;
    private TextInputEditText nameInput;
    private Button deleteButton;
    private EditUserProfileItemFragment itemFragment;

    public EditUserProfileTemplateFragment() {
        // Required empty public constructor
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.edit_user_profile_template_title);
    }

    public static EditUserProfileTemplateFragment newInstance(Bundle args) {
        EditUserProfileTemplateFragment fragment = new EditUserProfileTemplateFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        originalName = getArguments() == null ? null : getArguments().getString(ARG_NAME);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_user_profile_template, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameLayout = view.findViewById(R.id.textInputLayout_templateName);
        nameInput = view.findViewById(R.id.editText_templateName);
        deleteButton = view.findViewById(R.id.button_delete);

        ensureItemFragment();

        view.findViewById(R.id.button_cancel).setOnClickListener(v -> requireActivity().finish());
        view.findViewById(R.id.button_save).setOnClickListener(v -> save());
        deleteButton.setOnClickListener(v -> confirmDelete());

        if (originalName == null) {
            deleteButton.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            if (originalName == null) {
                if (itemFragment != null) itemFragment.applyConfig(UserProfileConfig.createDefault());
            } else {
                loadTemplate();
            }
        }
    }

    private void ensureItemFragment() {
        Fragment existing = getChildFragmentManager().findFragmentById(R.id.container_profileItem);
        if (existing instanceof EditUserProfileItemFragment) {
            itemFragment = (EditUserProfileItemFragment) existing;
            return;
        }
        itemFragment = EditUserProfileItemFragment.newInstance(UserProfileConfig.createDefault());
        getChildFragmentManager().beginTransaction()
                .replace(R.id.container_profileItem, itemFragment)
                .commit();
    }

    private void loadTemplate() {
        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            UserProfileTemplate template;
            try {
                template = ProfilesDatabase.get(appContext).userProfileTemplateDao().getByName(originalName);
            } catch (RuntimeException e) {
                template = null;
            }
            if (!isAdded()) return;
            UserProfileTemplate loaded = template;
            requireActivity().runOnUiThread(() -> {
                if (loaded == null) {
                    Toast.makeText(requireContext(), R.string.edit_user_profile_template_missing,
                            Toast.LENGTH_SHORT).show();
                    requireActivity().finish();
                    return;
                }
                nameInput.setText(loaded.name);
                if (itemFragment != null) itemFragment.applyConfig(loaded.toConfig());
            });
        }).start();
    }

    private void save() {
        String name = nameInput.getText() == null ? "" : nameInput.getText().toString();
        if (name.isEmpty()) {
            nameLayout.setError(getString(R.string.edit_user_profile_name_invalid));
            return;
        }
        nameLayout.setError(null);

        if (itemFragment == null) return;
        getChildFragmentManager().executePendingTransactions();
        if (itemFragment.getView() == null) return;
        Map<String, String> errors = new LinkedHashMap<>();
        UserProfileConfig config = itemFragment.readConfig(errors);
        if (errors.isEmpty()) errors.putAll(config.validate());
        itemFragment.showErrors(errors);
        if (!errors.isEmpty()) {
            Toast.makeText(requireContext(), R.string.edit_user_profile_validation_failed,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        checkConflictAndPersist(name, config);
    }

    private void checkConflictAndPersist(String name, UserProfileConfig config) {
        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            boolean conflict;
            try {
                conflict = ProfilesDatabase.get(appContext).userProfileTemplateDao().getByName(name) != null;
            } catch (RuntimeException e) {
                conflict = false;
            }
            if (!isAdded()) return;
            boolean ask = conflict && !name.equals(originalName);
            requireActivity().runOnUiThread(() -> {
                if (ask) {
                    new AlertDialog.Builder(requireContext())
                            .setMessage(getString(R.string.edit_user_profile_template_exists, name))
                            .setPositiveButton(getString(R.string.Yes), (dialog, which) -> persist(name, config))
                            .setNegativeButton(getString(R.string.No), null)
                            .show();
                } else {
                    persist(name, config);
                }
            });
        }).start();
    }

    private void persist(String name, UserProfileConfig config) {
        Context appContext = requireContext().getApplicationContext();
        final String renamedFrom = originalName != null && !originalName.equals(name) ? originalName : null;
        new Thread(() -> {
            boolean success;
            try {
                ProfilesDatabase.get(appContext).userProfileTemplateDao()
                        .upsert(UserProfileTemplate.create(name, config));
                if (renamedFrom != null) {
                    ProfilesDatabase.get(appContext).userProfileTemplateDao().deleteByName(renamedFrom);
                }
                success = true;
            } catch (RuntimeException e) {
                success = false;
            }
            if (!isAdded()) return;
            boolean result = success;
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        result ? R.string.Success : R.string.Failed, Toast.LENGTH_SHORT).show();
                if (result) requireActivity().finish();
            });
        }).start();
    }

    private void confirmDelete() {
        if (originalName == null) return;
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.edit_user_profile_delete_confirm)
                .setPositiveButton(getString(R.string.Yes), (dialog, which) -> delete())
                .setNegativeButton(getString(R.string.No), null)
                .show();
    }

    private void delete() {
        Context appContext = requireContext().getApplicationContext();
        final String name = originalName;
        new Thread(() -> {
            boolean success;
            try {
                ProfilesDatabase.get(appContext).userProfileTemplateDao().deleteByName(name);
                success = true;
            } catch (RuntimeException e) {
                success = false;
            }
            if (!isAdded()) return;
            boolean result = success;
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        result ? R.string.Success : R.string.Failed, Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            });
        }).start();
    }
}
