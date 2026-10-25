package app.MyApp.MyShellAuthorization.MyFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity;
import app.MyApp.MyShellAuthorization.MyDataStructures.AllowDeclaration;
import app.MyApp.MyShellAuthorization.MyDataStructures.CommandTemplate;
import app.MyApp.MyShellAuthorization.MyDataStructures.CommandTemplateStore;
import app.MyApp.MyShellAuthorization.MyDataStructures.UidAuthorizationStore;
import app.MyApp.MyShellAuthorization.MyDataStructures.UidPackages;
import app.MyApp.MyShellAuthorization.MyPermission.PermissionHelper;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ApplicationDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicationDetailFragment extends Fragment {

    public static final String ARG_UID = "uid";
    /**
     * The package the user tapped. Used only to decide which package is shown first; the
     * authorization itself is keyed by UID.
     */
    public static final String ARG_PACKAGE = "package";

    private int uid = -1;
    private String preferredPackageName;

    private SwitchCompat allow;
    private View authorizationContainer;
    private RecyclerView allowedTemplatesList;
    private TextView noAllowedTemplates;
    private MaterialCheckBox allowDirectCommands;
    private View directCommandOptions;
    private TextView directCommandProfileSummary;
    private Button editDirectCommandProfile;
    private MaterialCheckBox promptForConsent;

    private final List<String> availableTemplates = new ArrayList<>();
    private final Set<String> allowedTemplates = new LinkedHashSet<>();
    private CommandTemplate.Profile directCommandProfile = new CommandTemplate.Profile();
    private AllowedTemplateAdapter allowedTemplateAdapter;

    private boolean applyingSettings;
    private boolean changingAllowState;

    private final ActivityResultLauncher<Intent> profileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                CommandTemplate.Profile edited = CommandTemplate.Profile.fromJson(
                        result.getData().getStringExtra(EditUserProfileFragment.RESULT_PROFILE));
                if (edited == null) return;
                directCommandProfile = edited;
                updateDirectCommandProfileSummary();
                persistSettings();
            });

    public ApplicationDetailFragment() {
        // Required empty public constructor
    }

    public static ApplicationDetailFragment newInstance(Bundle args) {
        ApplicationDetailFragment fragment = new ApplicationDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_application_detail_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustWithin2(1324648023, android.os.Process.myTid(), -153716086, 409470690, 432123789);
            uid = getArguments().getInt(ARG_UID, -1);
            preferredPackageName = getArguments().getString(ARG_PACKAGE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_application_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PackageManager pm = requireContext().getPackageManager();
        List<PackageInfo> packages = uid > 0 ? UidPackages.packagesForUid(pm, uid) : null;
        PackageInfo primary = packages == null ? null : findPreferred(packages);
        if (packages == null || packages.isEmpty() || primary == null) {
            Toast.makeText(requireContext(), getString(R.string.invalid_parameter_error), Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        ImageView icon = view.findViewById(R.id.imageView_appIcon);
        TextView name = view.findViewById(R.id.textView_appName);
        TextView packageView = view.findViewById(R.id.textView_appPackage);

        ApplicationInfo appInfo = primary.applicationInfo;
        if (appInfo != null) {
            icon.setImageDrawable(appInfo.loadIcon(pm));
            name.setText(getString(R.string.application_detail_name_with_uid, appInfo.loadLabel(pm), uid));
        } else {
            icon.setImageResource(android.R.drawable.sym_def_app_icon);
            name.setText(getString(R.string.application_detail_name_with_uid, primary.packageName, uid));
        }

        int more = packages.size() - 1;
        if (more > 0) {
            packageView.setText(getString(R.string.application_detail_shared_packages_more, primary.packageName, more));
        } else {
            packageView.setText(primary.packageName);
        }

        allow = view.findViewById(R.id.switch_allow);
        authorizationContainer = view.findViewById(R.id.container_authorization);
        allowedTemplatesList = view.findViewById(R.id.recyclerView_allowedTemplates);
        noAllowedTemplates = view.findViewById(R.id.textView_noAllowedTemplates);
        allowDirectCommands = view.findViewById(R.id.checkBox_allowDirectCommands);
        directCommandOptions = view.findViewById(R.id.container_directCommandOptions);
        directCommandProfileSummary = view.findViewById(R.id.textView_directCommandProfileSummary);
        editDirectCommandProfile = view.findViewById(R.id.button_editDirectCommandProfile);
        promptForConsent = view.findViewById(R.id.checkBox_promptForConsent);

        allowedTemplatesList.setLayoutManager(new LinearLayoutManager(requireContext()));
        allowedTemplateAdapter = new AllowedTemplateAdapter(availableTemplates, allowedTemplates,
                this::setTemplateAllowed);
        allowedTemplatesList.setAdapter(allowedTemplateAdapter);

        loadTemplates();

        AllowDeclaration declaration = null;
        try {
            declaration = UidAuthorizationStore.get().readValidAuthorization(pm, uid);
        } catch (RuntimeException e) {
            Log.e("ApplicationDetailFragment", "Cannot read the authorization for UID " + uid, e);
        }

        bindDeclaration(declaration);
        allow.setChecked(declaration != null);
        updateAuthorizationVisibility(declaration != null);

        attachListeners(pm);
    }

    private void attachListeners(PackageManager pm) {
        allow.setOnCheckedChangeListener((btn, checked) -> {
            if (changingAllowState) return;
            if (checked) {
                enableAuthorization(pm);
            } else {
                revokeAuthorization();
            }
        });

        allowDirectCommands.setOnCheckedChangeListener((btn, checked) -> {
            if (applyingSettings) return;
            updateDirectCommandOptionsVisibility();
            if (checked) {
                // Turning direct commands on enables the consent prompt by default.
                applyingSettings = true;
                try {
                    promptForConsent.setChecked(true);
                } finally {
                    applyingSettings = false;
                }
            }
            persistSettings();
            if (checked && allowDirectCommands.isChecked()) {
                PermissionHelper.ensureOverlayPermission(this,
                        getString(R.string.application_detail_overlay_permission_message));
            }
        });
        promptForConsent.setOnCheckedChangeListener((btn, checked) -> {
            if (applyingSettings) return;
            persistSettings();
            if (checked && promptForConsent.isChecked()) {
                PermissionHelper.ensureOverlayPermission(this,
                        getString(R.string.application_detail_overlay_permission_message));
            }
        });
        editDirectCommandProfile.setOnClickListener(v -> openProfileEditor());
    }

    private void enableAuthorization(PackageManager pm) {
        boolean authorized;
        try {
            authorized = UidAuthorizationStore.get().authorize(pm, uid);
        } catch (RuntimeException e) {
            Log.e("ApplicationDetailFragment", "Cannot authorize UID " + uid, e);
            authorized = false;
        }
        if (!authorized) {
            Toast.makeText(requireContext(), getString(R.string.Failed), Toast.LENGTH_SHORT).show();
            setAllowChecked(false);
            updateAuthorizationVisibility(false);
            return;
        }

        AllowDeclaration declaration = null;
        try {
            declaration = UidAuthorizationStore.get().readValidAuthorization(pm, uid);
        } catch (RuntimeException e) {
            Log.e("ApplicationDetailFragment", "Cannot read the authorization for UID " + uid, e);
        }
        if (declaration == null) {
            Toast.makeText(requireContext(), getString(R.string.Failed), Toast.LENGTH_SHORT).show();
            setAllowChecked(false);
            updateAuthorizationVisibility(false);
            return;
        }

        bindDeclaration(declaration);
        updateAuthorizationVisibility(true);
    }

    private void revokeAuthorization() {
        try {
            UidAuthorizationStore.get().revoke(uid);
        } catch (RuntimeException e) {
            Log.e("ApplicationDetailFragment", "Cannot revoke UID " + uid, e);
        }
        updateAuthorizationVisibility(false);
    }

    private void loadTemplates() {
        List<String> names;
        try {
            names = CommandTemplateStore.get().listNames();
        } catch (RuntimeException e) {
            Log.e("ApplicationDetailFragment", "Cannot read the template list", e);
            names = new ArrayList<>();
        }
        availableTemplates.clear();
        availableTemplates.addAll(names);
        allowedTemplates.retainAll(availableTemplates);
        allowedTemplateAdapter.notifyDataSetChanged();

        boolean empty = availableTemplates.isEmpty();
        allowedTemplatesList.setVisibility(empty ? View.GONE : View.VISIBLE);
        noAllowedTemplates.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void bindDeclaration(@Nullable AllowDeclaration declaration) {
        applyingSettings = true;
        try {
            allowedTemplates.clear();
            if (declaration != null && declaration.allowedTemplates != null) {
                allowedTemplates.addAll(declaration.allowedTemplates);
            }
            allowedTemplates.retainAll(availableTemplates);

            allowDirectCommands.setChecked(declaration != null && declaration.allowDirectCommands);
            directCommandProfile = declaration == null || declaration.directCommandProfile == null
                    ? new CommandTemplate.Profile()
                    : declaration.directCommandProfile.copy();
            promptForConsent.setChecked(declaration == null
                    || declaration.promptForConsentBeforeDirectCommand);

            updateDirectCommandOptionsVisibility();
            updateDirectCommandProfileSummary();
            allowedTemplateAdapter.notifyDataSetChanged();
        } finally {
            applyingSettings = false;
        }
    }

    private void updateAuthorizationVisibility(boolean authorized) {
        authorizationContainer.setVisibility(authorized ? View.VISIBLE : View.GONE);
    }

    private void updateDirectCommandOptionsVisibility() {
        if (directCommandOptions == null || allowDirectCommands == null) return;
        directCommandOptions.setVisibility(
                allowDirectCommands.isChecked() ? View.VISIBLE : View.GONE);
    }

    private void setAllowChecked(boolean checked) {
        if (allow.isChecked() == checked) return;
        changingAllowState = true;
        try {
            allow.setChecked(checked);
        } finally {
            changingAllowState = false;
        }
    }

    private void setTemplateAllowed(String name, boolean allowed) {
        if (allowed) {
            allowedTemplates.add(name);
        } else {
            allowedTemplates.remove(name);
        }
        persistSettings();
    }

    private void persistSettings() {
        if (applyingSettings || allow == null || !allow.isChecked() || uid <= 0) return;

        List<String> ordered = new ArrayList<>();
        for (String name : availableTemplates) {
            if (allowedTemplates.contains(name)) ordered.add(name);
        }

        boolean saved;
        try {
            saved = UidAuthorizationStore.get().updateSettings(requireContext().getPackageManager(),
                    uid, ordered, allowDirectCommands.isChecked(), directCommandProfile,
                    promptForConsent.isChecked());
        } catch (RuntimeException e) {
            Log.e("ApplicationDetailFragment", "Cannot save the authorization settings", e);
            saved = false;
        }

        if (!saved) {
            Toast.makeText(requireContext(), R.string.application_detail_settings_save_failed,
                    Toast.LENGTH_SHORT).show();
            resyncAuthorization();
        }
    }

    private void resyncAuthorization() {
        PackageManager pm = requireContext().getPackageManager();
        AllowDeclaration declaration = null;
        try {
            declaration = UidAuthorizationStore.get().readValidAuthorization(pm, uid);
        } catch (RuntimeException e) {
            Log.e("ApplicationDetailFragment", "Cannot read the authorization for UID " + uid, e);
        }
        bindDeclaration(declaration);
        boolean authorized = declaration != null;
        setAllowChecked(authorized);
        updateAuthorizationVisibility(authorized);
    }

    private void updateDirectCommandProfileSummary() {
        if (directCommandProfileSummary == null) return;
        String type = directCommandProfile == null
                ? CommandTemplate.Profile.TYPE_DEFAULT : directCommandProfile.type;
        if (CommandTemplate.Profile.TYPE_TEMPLATE.equals(type)) {
            String name = directCommandProfile == null || directCommandProfile.template == null
                    ? "" : directCommandProfile.template;
            directCommandProfileSummary.setText(
                    getString(R.string.edit_template_user_profile_use_template, name));
        } else if (CommandTemplate.Profile.TYPE_CUSTOM.equals(type)) {
            directCommandProfileSummary.setText(R.string.edit_template_user_profile_custom);
        } else {
            directCommandProfileSummary.setText(R.string.edit_template_user_profile_default);
        }
    }

    private void openProfileEditor() {
        Bundle args = new Bundle();
        args.putString(EditUserProfileFragment.ARG_PROFILE,
                CommandTemplate.Profile.toJson(directCommandProfile));
        profileLauncher.launch(FragmentDialogContainerActivity.createIntent(requireContext(),
                EditUserProfileFragment.class, args,
                EditUserProfileFragment.getTitle(requireContext()), false));
    }

    private PackageInfo findPreferred(List<PackageInfo> packages) {
        if (preferredPackageName != null) {
            for (PackageInfo pkg : packages) {
                if (preferredPackageName.equals(pkg.packageName)) return pkg;
            }
        }
        return UidPackages.primary(packages);
    }

    private static final class AllowedTemplateAdapter
            extends RecyclerView.Adapter<AllowedTemplateAdapter.ViewHolder> {

        private final List<String> names;
        private final Set<String> selected;
        private final BiConsumer<String, Boolean> onChanged;

        AllowedTemplateAdapter(List<String> names, Set<String> selected,
                               BiConsumer<String, Boolean> onChanged) {
            this.names = names;
            this.selected = selected;
            this.onChanged = onChanged;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_allowed_template, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = names.get(position);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setText(name);
            holder.checkBox.setChecked(selected.contains(name));
            holder.checkBox.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> onChanged.accept(name, isChecked));
        }

        @Override
        public int getItemCount() {
            return names.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final MaterialCheckBox checkBox;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBox_allowedTemplate);
            }
        }
    }
}
