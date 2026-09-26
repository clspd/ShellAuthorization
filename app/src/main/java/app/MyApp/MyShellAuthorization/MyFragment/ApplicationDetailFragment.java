package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
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

import java.io.File;
import java.io.IOException;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyDataDirectory;
import app.MyApp.MyShellAuthorization.MyDataStructures.AllowDeclaration;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ApplicationDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ApplicationDetailFragment extends Fragment {

    public static final String ARG_PACKAGE = "package";

    private String packageName;

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
            packageName = getArguments().getString(ARG_PACKAGE);
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
        ApplicationInfo appInfo = null;
        if (packageName != null) {
            try {
                appInfo = pm.getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        if (appInfo == null) {
            Toast.makeText(requireContext(), getString(R.string.invalid_parameter_error), Toast.LENGTH_SHORT).show();
            requireActivity().finish();
            return;
        }

        ImageView icon = view.findViewById(R.id.imageView_appIcon);
        TextView name = view.findViewById(R.id.textView_appName);
        TextView packageView = view.findViewById(R.id.textView_appPackage);
        icon.setImageDrawable(appInfo.loadIcon(pm));
        name.setText(appInfo.loadLabel(pm));
        packageView.setText(packageName);

        SwitchCompat allow = view.findViewById(R.id.switch_allow);
        allow.setChecked(allowFile().exists());
        allow.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) restoreOrCreateAllowFile();
            else recycleAllowFile();
            btn.setChecked(allowFile().exists());
        });
    }

    private File allowFile() {
        return new File(new File(MyDataDirectory.get(), "allowlist"), packageName);
    }

    private File recycledAllowFile() {
        return new File(new File(new File(MyDataDirectory.get(), "Recycle.Bin"), "allowlist"), packageName);
    }

    private void restoreOrCreateAllowFile() {
        File allow = allowFile();
        File recycled = recycledAllowFile();
        // restore the recycled allow file so that its content is kept
        if (recycled.exists()) {
            if (!recycled.renameTo(allow)) Log.e("ApplicationDetailFragment", "Cannot restore " + recycled);
            return;
        }
        File dir = allow.getParentFile();
        if (!dir.exists()) if (!dir.mkdirs()) {
            Log.e("ApplicationDetailFragment", "Cannot mkdir " + dir);
            return;
        }
        try {
            AllowDeclaration.create(packageName).write(allow);
        } catch (IOException e) {
            Log.e("ApplicationDetailFragment", e.toString());
        }
    }

    private void recycleAllowFile() {
        File allow = allowFile();
        if (!allow.exists()) return;
        File recycled = recycledAllowFile();
        File dir = recycled.getParentFile();
        if (!dir.exists()) if (!dir.mkdirs()) {
            Log.e("ApplicationDetailFragment", "Cannot mkdir " + dir);
            return;
        }
        if (!allow.renameTo(recycled)) Log.e("ApplicationDetailFragment", "Cannot recycle " + allow);
    }
}
