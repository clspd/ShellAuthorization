package app.MyApp.MyShellAuthorization.MyFragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyShellAuthorization.MySecurity.OpenOnlineContentHelper;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustWithin(1245668, 0xcafebabe, 48, 60);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_getStarted).setOnClickListener(v -> {
            List<String> pkg = Arrays.asList(requireContext().getPackageName().split("\\."));
            Collections.reverse(pkg);
            String reversed = String.join(".", pkg);
            Uri u = Uri.parse("https://" + reversed + "/quick-start");
            OpenOnlineContentHelper.OpenOnlineContent(requireContext(), u.toString(), (dialog, which) -> {
                new CustomTabsIntent.Builder().setShowTitle(true).setCloseButtonEnabled(true).setUrlBarHidingEnabled(true).build().launchUrl(requireContext(), u);
            }, null);
        });

        view.findViewById(R.id.button_getShell).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.get_runner_dialog_title)
                    .setMessage(R.string.get_runner_dialog_content)
                    .setPositiveButton(R.string.Continue, ((dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/octet-stream");
                        intent.putExtra(Intent.EXTRA_TITLE, "ShellRun.dex");
                        exportShellRunner_stage1.launch(intent);
                    }))
                    .setNegativeButton(R.string.Cancel, null)
                    .show();
        });
    }

    private final ActivityResultLauncher<Intent> exportShellRunner_stage2 =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (null == uri) {
                        Toast.makeText(requireContext(), requireContext().getString(R.string.Failed), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try (InputStream is = requireContext().getAssets().open("ShellRun.sh");
                         OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                        if (os == null) throw new Exception("Cannot open file");

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.flush();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Toast.makeText(requireContext(), requireContext().getString(R.string.Success), Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> exportShellRunner_stage1 =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (null == uri) {
                        Toast.makeText(requireContext(), requireContext().getString(R.string.Failed), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try (InputStream is = requireContext().getAssets().open("ShellRun.dex");
                         OutputStream os = requireContext().getContentResolver().openOutputStream(uri)) {
                        if (os == null) throw new Exception("Cannot open file");

                        byte[] buffer = new byte[8192];
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            os.write(buffer, 0, len);
                        }
                        os.flush();
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/octet-stream");
                    intent.putExtra(Intent.EXTRA_TITLE, "ShellRun.sh");
                    exportShellRunner_stage2.launch(intent);
                }
            });

}