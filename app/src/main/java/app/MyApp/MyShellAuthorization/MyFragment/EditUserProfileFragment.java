package app.MyApp.MyShellAuthorization.MyFragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.radiobutton.MaterialRadioButton;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyShellAuthorization.MyDataStructures.CommandTemplate;
import app.MyApp.MyShellAuthorization.MyDataStructures.ProfilesDatabase;
import app.MyApp.MyShellAuthorization.MyDataStructures.UserProfileConfig;
import app.MyApp.MyShellAuthorization.MyDataStructures.UserProfileTemplate;
import top.clspd.shellauthorization.R;

public class EditUserProfileFragment extends Fragment {

    public static final String ARG_PROFILE = "profile";
    public static final String RESULT_PROFILE = "profile";

    private static final String STATE_MODE = "state_mode";
    private static final String STATE_TEMPLATE = "state_template";

    private static final String MODE_DEFAULT = "default";
    private static final String MODE_TEMPLATE = "template";
    private static final String MODE_CUSTOM = "custom";

    private String mode = MODE_DEFAULT;
    private CommandTemplate.Profile initialProfile = new CommandTemplate.Profile();
    private String selectedTemplate;
    private UserProfileConfig initialCustomConfig = UserProfileConfig.createDefault();

    private RadioGroup modeGroup;
    private View templateSection;
    private RecyclerView templateList;
    private TextView noTemplates;
    private View customContainer;
    private Button saveButton;
    private TemplateOptionAdapter templateAdapter;
    private final List<String> templateNames = new ArrayList<>();

    private EditUserProfileItemFragment itemFragment;

    public EditUserProfileFragment() {
        // Required empty public constructor
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_edit_user_profile_title);
    }

    public static EditUserProfileFragment newInstance(Bundle args) {
        EditUserProfileFragment fragment = new EditUserProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustWithin2(1755873346, Process.myTid(), Process.myPid(),
                    Process.FIRST_APPLICATION_UID, Process.LAST_APPLICATION_UID);
            CommandTemplate.Profile profile =
                    CommandTemplate.Profile.fromJson(getArguments().getString(ARG_PROFILE));
            if (profile != null) initialProfile = profile;
        }
        if (CommandTemplate.Profile.TYPE_TEMPLATE.equals(initialProfile.type)) {
            selectedTemplate = initialProfile.template;
        }
        mode = normalizeMode(initialProfile.type);
        if (initialProfile.data != null) initialCustomConfig = initialProfile.data;

        if (savedInstanceState != null) {
            String savedMode = savedInstanceState.getString(STATE_MODE);
            if (savedMode != null) mode = savedMode;
            if (savedInstanceState.containsKey(STATE_TEMPLATE)) {
                selectedTemplate = savedInstanceState.getString(STATE_TEMPLATE);
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_user_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        modeGroup = view.findViewById(R.id.radioGroup_profileMode);
        templateSection = view.findViewById(R.id.container_templateOptions);
        templateList = view.findViewById(R.id.recyclerView_profileTemplates);
        noTemplates = view.findViewById(R.id.textView_noTemplates);
        customContainer = view.findViewById(R.id.container_profileItem);
        saveButton = view.findViewById(R.id.button_save);

        templateList.setLayoutManager(new LinearLayoutManager(requireContext()));
        templateAdapter = new TemplateOptionAdapter(this::selectTemplate);
        templateList.setAdapter(templateAdapter);

        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton_useTemplate) {
                mode = MODE_TEMPLATE;
            } else if (checkedId == R.id.radioButton_custom) {
                mode = MODE_CUSTOM;
            } else {
                mode = MODE_DEFAULT;
            }
            updateModeViews();
        });

        if (MODE_TEMPLATE.equals(mode)) {
            modeGroup.check(R.id.radioButton_useTemplate);
        } else if (MODE_CUSTOM.equals(mode)) {
            modeGroup.check(R.id.radioButton_custom);
        } else {
            modeGroup.check(R.id.radioButton_default);
        }

        view.findViewById(R.id.button_cancel).setOnClickListener(v -> requireActivity().finish());
        saveButton.setOnClickListener(v -> save());

        updateModeViews();
        loadTemplates();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_MODE, mode);
        outState.putString(STATE_TEMPLATE, selectedTemplate);
    }

    private static String normalizeMode(String type) {
        if (CommandTemplate.Profile.TYPE_TEMPLATE.equals(type)) return MODE_TEMPLATE;
        if (CommandTemplate.Profile.TYPE_CUSTOM.equals(type)) return MODE_CUSTOM;
        return MODE_DEFAULT;
    }

    private void updateModeViews() {
        boolean templateMode = MODE_TEMPLATE.equals(mode);
        boolean customMode = MODE_CUSTOM.equals(mode);

        boolean hasTemplates = !templateNames.isEmpty();
        templateSection.setVisibility(templateMode ? View.VISIBLE : View.GONE);
        templateList.setVisibility(hasTemplates ? View.VISIBLE : View.GONE);
        noTemplates.setVisibility(hasTemplates ? View.GONE : View.VISIBLE);
        customContainer.setVisibility(customMode ? View.VISIBLE : View.GONE);

        if (customMode) ensureItemFragment();

        boolean canSave = !templateMode || selectedTemplate != null;
        saveButton.setEnabled(canSave);
    }

    private void ensureItemFragment() {
        if (itemFragment != null) return;
        Fragment existing = getChildFragmentManager().findFragmentById(R.id.container_profileItem);
        if (existing instanceof EditUserProfileItemFragment) {
            itemFragment = (EditUserProfileItemFragment) existing;
            return;
        }
        itemFragment = EditUserProfileItemFragment.newInstance(initialCustomConfig);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.container_profileItem, itemFragment)
                .commit();
    }

    private void selectTemplate(String name) {
        selectedTemplate = name;
        templateAdapter.setSelected(name);
        updateModeViews();
    }

    private void loadTemplates() {
        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            List<UserProfileTemplate> loaded;
            try {
                loaded = ProfilesDatabase.get(appContext).userProfileTemplateDao().getAll();
            } catch (RuntimeException e) {
                loaded = new ArrayList<>();
            }
            List<String> names = new ArrayList<>();
            for (UserProfileTemplate template : loaded) {
                names.add(template.name);
            }
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                templateNames.clear();
                templateNames.addAll(names);
                if (selectedTemplate != null && !templateNames.contains(selectedTemplate)) {
                    selectedTemplate = null;
                }
                templateAdapter.setSelected(selectedTemplate);
                templateAdapter.notifyDataSetChanged();
                updateModeViews();
            });
        }).start();
    }

    private void save() {
        CommandTemplate.Profile profile;
        if (MODE_TEMPLATE.equals(mode)) {
            if (selectedTemplate == null) {
                Toast.makeText(requireContext(), R.string.edit_user_profile_select_template,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            profile = CommandTemplate.Profile.ofTemplate(selectedTemplate);
        } else if (MODE_CUSTOM.equals(mode)) {
            ensureItemFragment();
            getChildFragmentManager().executePendingTransactions();
            if (itemFragment == null || itemFragment.getView() == null) return;
            Map<String, String> errors = new LinkedHashMap<>();
            UserProfileConfig config = itemFragment.readConfig(errors);
            if (errors.isEmpty()) errors.putAll(config.validate());
            itemFragment.showErrors(errors);
            if (!errors.isEmpty()) {
                Toast.makeText(requireContext(), R.string.edit_user_profile_validation_failed,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            profile = CommandTemplate.Profile.ofCustom(config);
        } else {
            profile = CommandTemplate.Profile.defaultProfile();
        }

        Intent result = new Intent();
        result.putExtra(RESULT_PROFILE, CommandTemplate.Profile.toJson(profile));
        requireActivity().setResult(Activity.RESULT_OK, result);
        requireActivity().finish();
    }

    private final class TemplateOptionAdapter extends RecyclerView.Adapter<TemplateOptionAdapter.ViewHolder> {

        private final java.util.function.Consumer<String> onSelected;

        TemplateOptionAdapter(java.util.function.Consumer<String> onSelected) {
            this.onSelected = onSelected;
        }

        void setSelected(String name) {
            selectedTemplate = name;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile_template_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = templateNames.get(position);
            holder.radioButton.setText(name);
            holder.radioButton.setChecked(name.equals(selectedTemplate));
            holder.itemView.setOnClickListener(v -> onSelected.accept(name));
        }

        @Override
        public int getItemCount() {
            return templateNames.size();
        }

        final class ViewHolder extends RecyclerView.ViewHolder {
            final MaterialRadioButton radioButton;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                radioButton = itemView.findViewById(R.id.radioButton_templateOption);
            }
        }
    }
}
