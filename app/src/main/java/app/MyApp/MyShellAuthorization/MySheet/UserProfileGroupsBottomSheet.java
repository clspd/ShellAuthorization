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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import top.clspd.shellauthorization.R;

public class UserProfileGroupsBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_KEY = "profile_groups";
    public static final String RESULT_SELECTED = "selected";

    private static final String ARG_SELECTED = "selected";

    private static final GroupEntry[] SYSTEM_GROUPS = {
            new GroupEntry(0, "root", "traditional unix root user"),
            new GroupEntry(1, "daemon", "Traditional unix daemon owner."),
            new GroupEntry(2, "bin", "Traditional unix binaries owner."),
            new GroupEntry(3, "sys", "A group with the same gid on Linux/macOS/Android."),
            new GroupEntry(1000, "system", "system server"),
            new GroupEntry(1001, "radio", "telephony subsystem, RIL"),
            new GroupEntry(1002, "bluetooth", "bluetooth subsystem"),
            new GroupEntry(1003, "graphics", "graphics devices"),
            new GroupEntry(1004, "input", "input devices"),
            new GroupEntry(1005, "audio", "audio devices"),
            new GroupEntry(1006, "camera", "camera devices"),
            new GroupEntry(1007, "log", "log devices"),
            new GroupEntry(1008, "compass", "compass device"),
            new GroupEntry(1009, "mount", "mountd socket"),
            new GroupEntry(1010, "wifi", "wifi subsystem"),
            new GroupEntry(1011, "adb", "android debug bridge (adbd)"),
            new GroupEntry(1012, "install", "group for installing packages"),
            new GroupEntry(1013, "media", "mediaserver process"),
            new GroupEntry(1014, "dhcp", "dhcp client"),
            new GroupEntry(1015, "sdcard_rw", "external storage write access"),
            new GroupEntry(1016, "vpn", "vpn system"),
            new GroupEntry(1017, "keystore", "keystore subsystem"),
            new GroupEntry(1018, "usb", "USB devices"),
            new GroupEntry(1019, "drm", "DRM server"),
            new GroupEntry(1020, "mdnsr", "MulticastDNSResponder (service discovery)"),
            new GroupEntry(1021, "gps", "GPS daemon"),
            new GroupEntry(1022, "unused1", "deprecated, DO NOT USE"),
            new GroupEntry(1023, "media_rw", "internal media storage write access"),
            new GroupEntry(1024, "mtp", "MTP USB driver access"),
            new GroupEntry(1025, "unused2", "deprecated, DO NOT USE"),
            new GroupEntry(1026, "drmrpc", "group for drm rpc"),
            new GroupEntry(1027, "nfc", "nfc subsystem"),
            new GroupEntry(1028, "sdcard_r", "external storage read access"),
            new GroupEntry(1029, "clat", "clat part of nat464"),
            new GroupEntry(1030, "loop_radio", "loop radio devices"),
            new GroupEntry(1031, "media_drm", "MediaDrm plugins"),
            new GroupEntry(1032, "package_info", "access to installed package details"),
            new GroupEntry(1033, "sdcard_pics", "external storage photos access"),
            new GroupEntry(1034, "sdcard_av", "external storage audio/video access"),
            new GroupEntry(1035, "sdcard_all", "access all users external storage"),
            new GroupEntry(1036, "logd", "log daemon"),
            new GroupEntry(1037, "shared_relro", "creator of shared GNU RELRO files"),
            new GroupEntry(1038, "dbus", "dbus-daemon IPC broker process"),
            new GroupEntry(1039, "tlsdate", "tlsdate unprivileged user"),
            new GroupEntry(1040, "media_ex", "mediaextractor process"),
            new GroupEntry(1041, "audioserver", "audioserver process"),
            new GroupEntry(1042, "metrics_coll", "metrics_collector process"),
            new GroupEntry(1043, "metricsd", "metricsd process"),
            new GroupEntry(1044, "webserv", "webservd process"),
            new GroupEntry(1045, "debuggerd", "debuggerd unprivileged user"),
            new GroupEntry(1046, "media_codec", "media_codec process"),
            new GroupEntry(1047, "cameraserver", "cameraserver process"),
            new GroupEntry(1048, "firewall", "firewall process"),
            new GroupEntry(1049, "trunks", "trunksd process"),
            new GroupEntry(1050, "nvram", "nvram daemon"),
            new GroupEntry(1051, "dns", "DNS resolution daemon (system: netd)"),
            new GroupEntry(1052, "dns_tether", "DNS resolution daemon (tether: dnsmasq)"),
            new GroupEntry(1053, "webview_zygote", "WebView zygote process"),
            new GroupEntry(1054, "vehicle_network", "Vehicle network service"),
            new GroupEntry(1055, "media_audio", "GID for audio files on internal media storage"),
            new GroupEntry(1056, "media_video", "GID for video files on internal media storage"),
            new GroupEntry(1057, "media_image", "GID for image files on internal media storage"),
            new GroupEntry(1058, "tombstoned", "tombstoned user"),
            new GroupEntry(1059, "media_obb", "GID for OBB files on internal media storage"),
            new GroupEntry(1060, "ese", "embedded secure element (eSE) subsystem"),
            new GroupEntry(1061, "ota_update", "resource tracking UID for OTA updates"),
            new GroupEntry(1062, "automotive_evs", "Automotive rear and surround view system"),
            new GroupEntry(1063, "lowpan", "LoWPAN subsystem"),
            new GroupEntry(1064, "lowpan", "hardware security module subsystem"),
            new GroupEntry(1065, "reserved_disk", "GID that has access to reserved disk space"),
            new GroupEntry(1066, "statsd", "statsd daemon"),
            new GroupEntry(1067, "incidentd", "incidentd daemon"),
            new GroupEntry(1068, "secure_element", "secure element subsystem"),
            new GroupEntry(1069, "lmkd", "low memory killer daemon"),
            new GroupEntry(1070, "llkd", "live lock daemon"),
            new GroupEntry(1071, "iorapd", "input/output readahead and pin daemon"),
            new GroupEntry(1072, "gpu_service", "GPU service daemon"),
            new GroupEntry(1073, "network_stack", "network stack service"),
            new GroupEntry(1074, "GSID", "GSI service daemon"),
            new GroupEntry(1075, "fsverity_cert", "fs-verity key ownership in keystore"),
            new GroupEntry(1076, "credstore", "identity credential manager service"),
            new GroupEntry(1077, "external_storage", "Full external storage access including USB OTG volumes"),
            new GroupEntry(1078, "ext_data_rw", "GID for app-private data directories on external storage"),
            new GroupEntry(1079, "ext_obb_rw", "GID for OBB directories on external storage"),
            new GroupEntry(1080, "context_hub", "GID for access to the Context Hub"),
            new GroupEntry(1081, "virtualizationservice", "VirtualizationService daemon"),
            new GroupEntry(1082, "artd", "ART Service daemon"),
            new GroupEntry(1083, "uwb", "UWB subsystem"),
            new GroupEntry(1084, "thread_network", "Thread Network subsystem"),
            new GroupEntry(1085, "diced", "Android's DICE daemon"),
            new GroupEntry(1086, "dmesgd", "dmesg parsing daemon for kernel report collection"),
            new GroupEntry(1087, "jc_weaver", "Javacard Weaver HAL - to manage omapi ARA rules"),
            new GroupEntry(1088, "jc_strongbox", "Javacard Strongbox HAL - to manage omapi ARA rules"),
            new GroupEntry(1089, "jc_identitycred", "Javacard Identity Cred HAL - to manage omapi ARA rules"),
            new GroupEntry(1090, "sdk_sandbox", "SDK sandbox virtual UID"),
            new GroupEntry(1091, "security_log_writer", "write to security log"),
            new GroupEntry(1092, "prng_seeder", "PRNG seeder daemon"),
            new GroupEntry(1093, "uprobestats", "uid for uprobestats"),
            new GroupEntry(1094, "cros_ec", "uid for accessing ChromeOS EC (cros_ec)"),
            new GroupEntry(1095, "mmd", "uid for memory management daemon"),
            new GroupEntry(2000, "shell", "adb and debug shell user"),
            new GroupEntry(2001, "cache", "cache access"),
            new GroupEntry(2002, "diag", "access to diagnostic resources"),
            new GroupEntry(3001, "net_bt_admin", "bluetooth: create any socket"),
            new GroupEntry(3002, "net_bt", "bluetooth: create sco, rfcomm or l2cap sockets"),
            new GroupEntry(3003, "inet", "can create AF_INET and AF_INET6 sockets"),
            new GroupEntry(3004, "net_raw", "can create raw INET sockets"),
            new GroupEntry(3005, "net_admin", "can configure interfaces and routing tables."),
            new GroupEntry(3006, "net_bw_stats", "read bandwidth statistics"),
            new GroupEntry(3007, "net_bw_acct", "change bandwidth statistics accounting"),
            new GroupEntry(3008, "net_bt_stack", "access to various bluetooth management functions"),
            new GroupEntry(3009, "readproc", "Allow /proc read access"),
            new GroupEntry(3010, "wakelock", "Allow system wakelock read/write access"),
            new GroupEntry(3011, "uhid", "Allow read/write to /dev/uhid node"),
            new GroupEntry(3012, "readtracefs", "Allow tracefs read"),
            new GroupEntry(3013, "virtualmachine", "Allows VMs to tune for performance"),
            new GroupEntry(9997, "everybody", "Shared external storage read/write"),
            new GroupEntry(9998, "misc", "Access to misc storage"),
            new GroupEntry(9999, "nobody", "Reserved"),
            new GroupEntry(10000, "app", "Access to app data"),
    };

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

        entries.clear();
        for (GroupEntry entry : SYSTEM_GROUPS) entries.add(entry);
        appendMissingSelected();
        adapter.notifyDataSetChanged();
    }

    private void appendMissingSelected() {
        Set<Long> known = new LinkedHashSet<>();
        for (GroupEntry entry : entries) known.add(entry.gid);
        for (Long gid : selected) {
            if (gid != null && !known.contains(gid)) {
                entries.add(new GroupEntry(gid, getString(R.string.edit_user_profile_group_unknown, gid), ""));
            }
        }
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

    private static final class GroupEntry {
        final long gid;
        final String name;
        final String description;

        GroupEntry(long gid, String name, String description) {
            this.gid = gid;
            this.name = name;
            this.description = description;
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
            holder.name.setText(holder.itemView.getContext()
                    .getString(R.string.edit_user_profile_group_entry, entry.name, entry.gid));
            holder.description.setText(entry.description);
            holder.checkBox.setOnCheckedChangeListener(null);
            holder.checkBox.setChecked(selected.contains(entry.gid));
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selected.add(entry.gid);
                } else {
                    selected.remove(entry.gid);
                }
            });
            holder.itemView.setOnClickListener(v -> holder.checkBox.toggle());
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        final class ViewHolder extends RecyclerView.ViewHolder {
            final MaterialCheckBox checkBox;
            final TextView name;
            final TextView description;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                checkBox = itemView.findViewById(R.id.checkBox_group);
                name = itemView.findViewById(R.id.textView_groupName);
                description = itemView.findViewById(R.id.textView_groupDesc);
            }
        }
    }
}
