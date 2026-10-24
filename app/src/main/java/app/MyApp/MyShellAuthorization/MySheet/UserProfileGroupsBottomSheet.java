package app.MyApp.MyShellAuthorization.MySheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import top.clspd.shellauthorization.R;

public class UserProfileGroupsBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "profile_groups";
    public static final String RESULT_SELECTED = "selected";

    private static final String ARG_SELECTED = "selected";
    private static final String GROUP_FILE = "/system/etc/group";

    private final List<GroupEntry> entries = new ArrayList<>();
    private final Set<Long> selected = new LinkedHashSet<>();
    private GroupAdapter adapter;

    public static UserProfileGroupsBottomSheet newInstance(long[] selectedGids) {
        Bundle args = new Bundle();
        args.putLongArray(ARG_SELECTED, selectedGids);
        UserProfileGroupsBottomSheet sheet = new UserProfileGroupsBottomSheet();
        sheet.setArguments(args);
        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_user_profile_groups, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        long[] initial = args == null ? null : args.getLongArray(ARG_SELECTED);
        if (initial != null) {
            for (long gid : initial) selected.add(gid);
        }

        TextView title = view.findViewById(R.id.textView_groupsTitle);
        title.setText(R.string.edit_user_profile_groups_sheet_title);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_groupOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new GroupAdapter();
        recyclerView.setAdapter(adapter);

        Button ok = view.findViewById(R.id.button_ok);
        ok.setOnClickListener(v -> deliverResult());
        Button cancel = view.findViewById(R.id.button_cancel);
        cancel.setOnClickListener(v -> dismiss());

        loadGroups();
    }

    private void loadGroups() {
        new Thread(() -> {
            List<GroupEntry> loaded = readSystemGroups();
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                entries.clear();
                entries.addAll(loaded);
                appendMissingSelected();
                sortEntries();
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void appendMissingSelected() {
        Set<Long> known = new LinkedHashSet<>();
        for (GroupEntry entry : entries) known.add(entry.gid);
        for (Long gid : selected) {
            if (gid != null && !known.contains(gid)) {
                entries.add(new GroupEntry(gid, getString(R.string.edit_user_profile_group_unknown, gid)));
            }
        }
    }

    private void sortEntries() {
        Collections.sort(entries, (left, right) -> {
            int byName = left.name.compareToIgnoreCase(right.name);
            return byName != 0 ? byName : Long.compare(left.gid, right.gid);
        });
    }

    private void deliverResult() {
        long[] result = new long[selected.size()];
        int index = 0;
        for (Long gid : selected) {
            result[index++] = gid;
        }
        Bundle bundle = new Bundle();
        bundle.putLongArray(RESULT_SELECTED, result);
        getParentFragmentManager().setFragmentResult(RESULT_KEY, bundle);
        dismiss();
    }

    private static List<GroupEntry> readSystemGroups() {
        List<GroupEntry> result = new ArrayList<>();
        File file = new File(GROUP_FILE);
        if (file.isFile()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                Set<Long> seen = new LinkedHashSet<>();
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty() || line.charAt(0) == '#') continue;
                    String[] parts = line.split(":", 4);
                    if (parts.length < 3) continue;
                    long gid;
                    try {
                        gid = Long.parseLong(parts[2]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if (gid < 0 || !seen.add(gid)) continue;
                    result.add(new GroupEntry(gid, parts[0]));
                }
            } catch (Exception ignored) {
                result.clear();
            }
        }
        if (result.isEmpty()) {
            result.addAll(builtInGroups());
        }
        return result;
    }

    private static List<GroupEntry> builtInGroups() {
        List<GroupEntry> result = new ArrayList<>();
        result.add(new GroupEntry(0, "root"));
        result.add(new GroupEntry(1000, "system"));
        result.add(new GroupEntry(1003, "graphics"));
        result.add(new GroupEntry(1004, "input"));
        result.add(new GroupEntry(1005, "audio"));
        result.add(new GroupEntry(1006, "camera"));
        result.add(new GroupEntry(1007, "log"));
        result.add(new GroupEntry(1011, "adb"));
        result.add(new GroupEntry(1012, "install"));
        result.add(new GroupEntry(1013, "media"));
        result.add(new GroupEntry(1015, "sdcard_rw"));
        result.add(new GroupEntry(1028, "sdcard_r"));
        result.add(new GroupEntry(2000, "shell"));
        result.add(new GroupEntry(3001, "net_bt_admin"));
        result.add(new GroupEntry(3002, "net_bt"));
        result.add(new GroupEntry(3003, "inet"));
        result.add(new GroupEntry(3004, "net_raw"));
        result.add(new GroupEntry(3005, "net_admin"));
        result.add(new GroupEntry(3006, "net_bw_stats"));
        result.add(new GroupEntry(3007, "net_bw_acct"));
        return result;
    }

    private static final class GroupEntry {
        final long gid;
        final String name;

        GroupEntry(long gid, String name) {
            this.gid = gid;
            this.name = name;
        }
    }

    private final class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile_group, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            GroupEntry entry = entries.get(position);
            MaterialCheckBox checkBox = holder.checkBox;
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setText(holder.itemView.getContext()
                    .getString(R.string.edit_user_profile_group_entry, entry.name, entry.gid));
            checkBox.setChecked(selected.contains(entry.gid));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selected.add(entry.gid);
                } else {
                    selected.remove(entry.gid);
                }
            });
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        final class ViewHolder extends RecyclerView.ViewHolder {
            final MaterialCheckBox checkBox;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBox_group);
            }
        }
    }
}
