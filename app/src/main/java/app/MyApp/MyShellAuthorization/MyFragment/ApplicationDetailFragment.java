package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyShellAuthorization.MyDataStructures.UidAuthorizationStore;
import app.MyApp.MyShellAuthorization.MyDataStructures.UidPackages;
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

        SwitchCompat allow = view.findViewById(R.id.switch_allow);
        allow.setChecked(UidAuthorizationStore.get().isAuthorized(pm, uid));
        allow.setOnCheckedChangeListener((btn, checked) -> {
            boolean success;
            if (checked) {
                success = UidAuthorizationStore.get().authorize(pm, uid);
                if (!success) {
                    Log.e("ApplicationDetailFragment", "Cannot authorize UID " + uid);
                    Toast.makeText(requireContext(), getString(R.string.Failed), Toast.LENGTH_SHORT).show();
                }
            } else {
                UidAuthorizationStore.get().revoke(uid);
                success = true;
            }
            btn.setChecked(success && UidAuthorizationStore.get().isAuthorized(pm, uid));
        });
    }

    private PackageInfo findPreferred(List<PackageInfo> packages) {
        if (preferredPackageName != null) {
            for (PackageInfo pkg : packages) {
                if (preferredPackageName.equals(pkg.packageName)) return pkg;
            }
        }
        return UidPackages.primary(packages);
    }
}
