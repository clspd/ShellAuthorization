package app.MyApp.MyShellAuthorization.MyFragment;

import static app.MyApp.MyCommon.MyUtilities.RawResourceReader.readRawResource;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyDataDirectory;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.DataHelper;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.ModeChanger;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AddShellProviderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddShellProviderFragment extends Fragment {

    private WebView w;

    public AddShellProviderFragment() {
        // Required empty public constructor
    }

    public static AddShellProviderFragment newInstance(Bundle bundle) {
        AddShellProviderFragment fragment = new AddShellProviderFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_add_shell_provider_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual2(982691035, 1082376, 1868812, 7035844);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_shell_provider, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        w = view.findViewById(R.id.webview);
        w.getSettings().setJavaScriptEnabled(true);
        w.getSettings().setDomStorageEnabled(true);
        w.getSettings().setMediaPlaybackRequiresUserGesture(false);
        w.getSettings().setAllowContentAccess(true);
        w.loadDataWithBaseURL("http://127.0.0.1/", readRawResource(requireContext(), R.raw.add_shell_provider_fragment_page), "text/html", "utf-8", null);
        w.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.startsWith("https://commit-form.com/")) {
                Uri uri = Uri.parse(url);
                String type = uri.getQueryParameter("type");
                if (type != null) switch (type) {
                    case "close":
                        break;
                    case "root":
                    {
                        String su = uri.getQueryParameter("su");
                        if (null == su) break;
                        if (!new File(su).exists()) {
                            requireActivity().runOnUiThread(() -> new AlertDialog.Builder(requireContext())
                                .setTitle(requireContext().getString(R.string.shell_root_fail_auth_notfound_title))
                                .setMessage(requireContext().getString(R.string.shell_root_fail_auth_notfound_content))
                                .setPositiveButton(requireContext().getString(R.string.shell_root_fail_auth_notfound_giveup), null)
                                .show());
                            return true;
                        }
                        new Thread(() -> {
                            try {
                                Process p = Runtime.getRuntime().exec(su);
                                DataOutputStream os = new DataOutputStream(p.getOutputStream());

                                os.writeBytes("/system/bin/id -u\n");
                                os.flush();
                                os.close();

                                BufferedReader r = new BufferedReader(
                                        new InputStreamReader(p.getInputStream()));
                                String line;
                                StringBuilder total = new StringBuilder();
                                while ((line = r.readLine()) != null) {
                                    total.append(line);
                                }
                                p.waitFor();

                                if (!total.toString().trim().equals("0")) throw new Exception("Cannot root.");

                                requireActivity().runOnUiThread(() -> saveRootProviderAndFinish(su));
                            }
                            catch (Exception e) {
                                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_LONG).show());
                            }
                        }).start();
                        return true;
                    }
                    case "shizuku":
                        saveShizukuProviderAndFinish();
                        return true;
                }
                requireActivity().finish();
                return true;
            }
            if (url.startsWith("https://auth-shizuku.com/")) {
                doHandleShizukuAuthLogic();
                return true;
            }
            return false;
            }
        });
    }

    private void doHandleShizukuAuthLogic() {
        PackageManager pm = requireContext().getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(DataHelper.getShizukuPackageName(), 0);
            String apk = appInfo.sourceDir;
            String dataDir = MyDataDirectory.get();
            File shizuku_support = new File(dataDir, "shizuku_support");
            if (!shizuku_support.exists()) if (!shizuku_support.mkdir()) throw new Exception("cannot mkdir");
            // extract assets/rish and assets/rish_shizuku.dex to support dir
            extractFromApk(apk, "assets/rish", new File(shizuku_support, "rish_wrapper"));
            File RSD = new File(shizuku_support, "rish_shizuku.dex");
            if (RSD.exists()) ModeChanger.ChangeMode(RSD.getAbsolutePath(), 0600);
            extractFromApk(apk, "assets/rish_shizuku.dex", RSD);
            ModeChanger.ChangeMode(RSD.getAbsolutePath(), 0400);
            // run it to check permission
            new Thread(() -> {
                Process process = null;
                try {
                    ProcessBuilder pb = new ProcessBuilder("/system/bin/app_process",
                        "-Djava.class.path=" + RSD.getAbsolutePath(), "/system/bin",
                        "--nice-name=rish_client", DataHelper.getShizukuShellMainClass(), "/system/bin/id");
                    Map<String, String> env = pb.environment();
                    env.put("RISH_APPLICATION_ID", requireContext().getPackageName());
                    Log.d("AddShellProviderFragment", "Launching : " + String.join(" ", pb.command()));
                    process = pb.start();

                    int exitCode = process.waitFor();

                    requireActivity().runOnUiThread(() -> {
                        if (exitCode == 0) {
                            Toast.makeText(requireContext(), getString(R.string.shell_shizuku_success_auth_toast), Toast.LENGTH_SHORT).show();
                            w.evaluateJavascript("shizuku_success()", result -> {});
                            return;
                        }
                        new AlertDialog.Builder(requireContext())
                            .setTitle(requireContext().getString(R.string.shell_shizuku_fail_auth_noauth_title))
                            .setMessage(requireContext().getString(R.string.shell_shizuku_fail_auth_noauth_content))
                            .setPositiveButton(requireContext().getString(R.string.shell_shizuku_fail_auth_noauth_giveup), null)
                            .show();
                        w.evaluateJavascript("shizuku_fail(2)", result -> {});
                    });
                } catch (Exception e) {
                    Log.e("AddShellProviderFragment", e.toString());
                    w.evaluateJavascript("shizuku_fail(0)", result -> {});
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }
            }).start();
        } catch (PackageManager.NameNotFoundException e) {
            requireActivity().runOnUiThread(() -> {
                new AlertDialog.Builder(requireContext())
                    .setTitle(requireContext().getString(R.string.shell_shizuku_fail_auth_noinstall_title))
                    .setMessage(requireContext().getString(R.string.shell_shizuku_fail_auth_noinstall_content))
                    .setPositiveButton(requireContext().getString(R.string.shell_shizuku_fail_auth_noinstall_install), (dialog, which) -> {
                        String myStr = DataHelper.getShizukuDownloadPage();
                        Uri webpage = Uri.parse(myStr);
                        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
                            startActivity(intent);
                        } else {
                            Toast.makeText(requireContext(), getString(R.string.cannot_open_page), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(requireContext().getString(R.string.shell_shizuku_fail_auth_noinstall_giveup), null)
                    .show();
            });
            w.evaluateJavascript("shizuku_fail(1)", result -> {});
        } catch (Exception e) {
            Log.e("AddShellProviderFragment", e.toString());
            w.evaluateJavascript("shizuku_fail(0)", result -> {});
        }
    }

    private void saveRootProviderAndFinish(String su) {
        File sp = new File(MyDataDirectory.get(), "shells");
        if (!sp.exists()) if (!sp.mkdir()) throw new RuntimeException("Cannot mkdir");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "root");
        jsonObject.addProperty("su", su);
        String jsonContent = new Gson().toJson(jsonObject);
        File r = new File(sp, "root");
        try (FileWriter writer = new FileWriter(r)) {
            writer.write(jsonContent);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file", e);
        }
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }

    private void saveShizukuProviderAndFinish() {
        long sv = 0;
        PackageManager pm = requireContext().getPackageManager();
        try {
            PackageInfo appInfo = pm.getPackageInfo(DataHelper.getShizukuPackageName(), 0);
            sv = appInfo.getLongVersionCode();
        } catch (PackageManager.NameNotFoundException e) {
            return;
        }

        File sp = new File(MyDataDirectory.get(), "shells");
        if (!sp.exists()) if (!sp.mkdir()) throw new RuntimeException("Cannot mkdir");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("type", "shizuku");
        jsonObject.addProperty("shizuku_version", sv);
        String jsonContent = new Gson().toJson(jsonObject);
        File r = new File(sp, "shizuku");
        try (FileWriter writer = new FileWriter(r)) {
            writer.write(jsonContent);
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file", e);
        }
        requireActivity().setResult(Activity.RESULT_OK);
        requireActivity().finish();
    }

    private void extractFromApk(String apkPath, String entryPath, File outFile) throws Exception {
        try (ZipFile zipFile = new ZipFile(apkPath)) {
            ZipEntry entry = zipFile.getEntry(entryPath);
            if (entry == null || entry.isDirectory()) {
                throw new Exception("file not found " + entryPath);
            }
            try (InputStream is = zipFile.getInputStream(entry);
                 FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
            }
        }
    }
}