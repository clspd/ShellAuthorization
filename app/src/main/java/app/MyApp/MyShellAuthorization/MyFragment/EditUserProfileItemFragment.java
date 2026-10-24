package app.MyApp.MyShellAuthorization.MyFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import app.MyApp.MyShellAuthorization.MyDataStructures.UserProfileConfig;
import app.MyApp.MyShellAuthorization.MySheet.UserProfileFlagsBottomSheet;
import app.MyApp.MyShellAuthorization.MySheet.UserProfileGroupsBottomSheet;
import top.clspd.shellauthorization.R;

public class EditUserProfileItemFragment extends Fragment {

    private static final String ARG_CONFIG = "config";
    private static final String STATE_GROUPS = "state_groups";
    private static final String STATE_NO_NEW_PRIVS = "state_no_new_privs";

    private final Set<Long> selectedGroups = new LinkedHashSet<>();
    private boolean noNewPrivs;

    private TextInputLayout userLayout;
    private TextInputLayout groupLayout;
    private TextInputLayout capabilitiesLayout;
    private TextInputLayout contextLayout;
    private TextInputLayout unshareLayout;
    private TextInputEditText userInput;
    private TextInputEditText groupInput;
    private TextInputEditText capabilitiesInput;
    private TextInputEditText contextInput;
    private TextInputEditText unshareInput;
    private TextView groupsSummary;

    private UserProfileConfig initialConfig = UserProfileConfig.createDefault();

    public EditUserProfileItemFragment() {
        // Required empty public constructor
    }

    public static EditUserProfileItemFragment newInstance(UserProfileConfig config) {
        Bundle args = new Bundle();
        args.putString(ARG_CONFIG, UserProfileConfig.toJson(config));
        EditUserProfileItemFragment fragment = new EditUserProfileItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String json = args == null ? null : args.getString(ARG_CONFIG);
        UserProfileConfig fromArgs = UserProfileConfig.fromJson(json);
        if (fromArgs != null) initialConfig = fromArgs;

        selectedGroups.addAll(initialConfig.groups);
        noNewPrivs = initialConfig.flags.noNewPrivs;

        if (savedInstanceState != null) {
            selectedGroups.clear();
            long[] groups = savedInstanceState.getLongArray(STATE_GROUPS);
            if (groups != null) {
                for (long gid : groups) selectedGroups.add(gid);
            }
            noNewPrivs = savedInstanceState.getBoolean(STATE_NO_NEW_PRIVS, noNewPrivs);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_user_profile_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userLayout = view.findViewById(R.id.textInputLayout_user);
        groupLayout = view.findViewById(R.id.textInputLayout_group);
        capabilitiesLayout = view.findViewById(R.id.textInputLayout_capabilities);
        contextLayout = view.findViewById(R.id.textInputLayout_context);
        unshareLayout = view.findViewById(R.id.textInputLayout_unshare);

        userInput = view.findViewById(R.id.editText_user);
        groupInput = view.findViewById(R.id.editText_group);
        capabilitiesInput = view.findViewById(R.id.editText_capabilities);
        contextInput = view.findViewById(R.id.editText_context);
        unshareInput = view.findViewById(R.id.editText_unshare);

        groupsSummary = view.findViewById(R.id.textView_groupsSummary);

        if (savedInstanceState == null) {
            applyConfig(initialConfig);
        } else {
            updateGroupsSummary();
        }

        Button selectGroups = view.findViewById(R.id.button_selectGroups);
        selectGroups.setOnClickListener(v -> openGroupsSheet());
        Button editFlags = view.findViewById(R.id.button_editFlags);
        editFlags.setOnClickListener(v -> openFlagsSheet());

        getChildFragmentManager().setFragmentResultListener(
                UserProfileGroupsBottomSheet.RESULT_KEY, getViewLifecycleOwner(),
                (key, bundle) -> {
                    long[] groups = bundle.getLongArray(UserProfileGroupsBottomSheet.RESULT_SELECTED);
                    selectedGroups.clear();
                    if (groups != null) {
                        for (long gid : groups) selectedGroups.add(gid);
                    }
                    updateGroupsSummary();
                });

        getChildFragmentManager().setFragmentResultListener(
                UserProfileFlagsBottomSheet.RESULT_KEY, getViewLifecycleOwner(),
                (key, bundle) -> {
                    noNewPrivs = bundle.getBoolean(UserProfileFlagsBottomSheet.RESULT_NO_NEW_PRIVS, false);
                });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLongArray(STATE_GROUPS, toArray(selectedGroups));
        outState.putBoolean(STATE_NO_NEW_PRIVS, noNewPrivs);
    }

    public void applyConfig(UserProfileConfig config) {
        if (config == null) config = UserProfileConfig.createDefault();
        initialConfig = config;
        selectedGroups.clear();
        selectedGroups.addAll(config.groups);
        noNewPrivs = config.flags.noNewPrivs;

        if (userInput == null) return;
        userInput.setText(String.valueOf(config.user));
        groupInput.setText(String.valueOf(config.group));
        capabilitiesInput.setText(UserProfileConfig.formatCapabilities(config.capabilities));
        contextInput.setText(config.context);
        unshareInput.setText(String.valueOf(config.unshare));
        clearErrors();
        updateGroupsSummary();
    }

    public UserProfileConfig readConfig(Map<String, String> errors) {
        clearErrors();
        UserProfileConfig config = new UserProfileConfig();
        config.user = readUint32(userInput, userLayout, UserProfileConfig.FIELD_USER, errors);
        config.group = readUint32(groupInput, groupLayout, UserProfileConfig.FIELD_GROUP, errors);
        config.capabilities = readCapabilities(errors);
        config.context = textOf(contextInput);
        config.unshare = readUint32(unshareInput, unshareLayout, UserProfileConfig.FIELD_UNSHARE, errors);
        config.groups = new ArrayList<>(selectedGroups);
        config.flags.noNewPrivs = noNewPrivs;
        return config;
    }

    public void showErrors(Map<String, String> errors) {
        if (errors == null) return;
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            TextInputLayout target = layoutForKey(entry.getKey());
            if (target != null) target.setError(entry.getValue());
        }
    }

    public boolean isValid() {
        UserProfileConfig config = readConfig(new java.util.LinkedHashMap<>());
        return config.validate().isEmpty();
    }

    private TextInputLayout layoutForKey(String field) {
        if (UserProfileConfig.FIELD_USER.equals(field)) return userLayout;
        if (UserProfileConfig.FIELD_GROUP.equals(field)) return groupLayout;
        if (UserProfileConfig.FIELD_CAPABILITIES.equals(field)) return capabilitiesLayout;
        if (UserProfileConfig.FIELD_CONTEXT.equals(field)) return contextLayout;
        if (UserProfileConfig.FIELD_UNSHARE.equals(field)) return unshareLayout;
        return null;
    }

    private void clearErrors() {
        userLayout.setError(null);
        groupLayout.setError(null);
        capabilitiesLayout.setError(null);
        contextLayout.setError(null);
        unshareLayout.setError(null);
    }

    private long readUint32(TextInputEditText input, TextInputLayout layout, String field,
                            Map<String, String> errors) {
        String text = textOf(input);
        if (text.isEmpty()) {
            reportError(layout, field, getString(R.string.edit_user_profile_field_required), errors);
            return 0;
        }
        try {
            return UserProfileConfig.parseUnsigned32(text);
        } catch (NumberFormatException e) {
            reportError(layout, field, getString(R.string.edit_user_profile_invalid_number), errors);
            return 0;
        }
    }

    private BigInteger readCapabilities(Map<String, String> errors) {
        String text = textOf(capabilitiesInput);
        if (text.isEmpty()) {
            reportError(capabilitiesLayout, UserProfileConfig.FIELD_CAPABILITIES,
                    getString(R.string.edit_user_profile_field_required), errors);
            return BigInteger.ZERO;
        }
        try {
            return UserProfileConfig.parseUnsigned(text);
        } catch (NumberFormatException e) {
            reportError(capabilitiesLayout, UserProfileConfig.FIELD_CAPABILITIES,
                    getString(R.string.edit_user_profile_invalid_number), errors);
            return BigInteger.ZERO;
        }
    }

    private void reportError(TextInputLayout layout, String field, String message,
                             Map<String, String> errors) {
        errors.put(field, message);
        if (layout != null) layout.setError(message);
    }

    private void updateGroupsSummary() {
        if (groupsSummary == null) return;
        groupsSummary.setText(getString(R.string.edit_user_profile_groups_summary, selectedGroups.size()));
    }

    private void openGroupsSheet() {
        UserProfileGroupsBottomSheet.newInstance(toArray(selectedGroups))
                .show(getChildFragmentManager(), "profile_groups");
    }

    private void openFlagsSheet() {
        UserProfileFlagsBottomSheet.newInstance(noNewPrivs)
                .show(getChildFragmentManager(), "profile_flags");
    }

    private static String textOf(TextView view) {
        CharSequence text = view == null ? null : view.getText();
        return text == null ? "" : text.toString();
    }

    private static long[] toArray(Set<Long> groups) {
        long[] result = new long[groups.size()];
        int index = 0;
        for (Long gid : groups) {
            result[index++] = gid == null ? 0 : gid;
        }
        return result;
    }
}
