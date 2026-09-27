package top.clspd.shellauthorization.Shared.ShellProvider.implementation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import com.android.apksig.ApkVerifier;
import com.android.apksig.apk.ApkFormatException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyDataDirectory;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.DataHelper;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.ModeChanger;
import top.clspd.shellauthorization.R;
import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderParameter;
import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderSetupAction;

public class ShizukuProvider extends ProcessShellProvider {

    public static final String PROVIDER_NAME = "shizuku";
    public static final String PARAM_SHIZUKU_VERSION = "shizuku_version";
    public static final int ACTION_AUTHORIZE = 1;

    private Long shizukuVersion;

    @Override
    public String getProviderName() {
        return PROVIDER_NAME;
    }

    @Override
    public String generateProviderIdentifier() {
        return PROVIDER_NAME;
    }

    @Override
    @DrawableRes
    public int getProviderIconResourceIdentifier() {
        return R.drawable.ic_provider_shizuku;
    }

    @Override
    public String getProviderFriendlyName() {
        return context.getString(R.string.shell_provider_type_shizuku);
    }

    @Override
    public boolean isProviderFriendlyNameEditableByUser() {
        return false;
    }

    @Override
    public String getProviderDescription() {
        return context.getString(R.string.shell_provider_shizuku_description);
    }

    @Override
    public String getProviderDetailedDescription() {
        return context.getString(R.string.shell_provider_shizuku_detailed_description);
    }

    @Override
    public List<ShellProviderParameter> getRequiredParameters() {
        return Collections.emptyList();
    }

    @Override
    public List<ShellProviderParameter> getParameters() {
        if (shizukuVersion == null) return Collections.emptyList();
        return Collections.singletonList(
                new ShellProviderParameter(PARAM_SHIZUKU_VERSION, "Shizuku version",
                        ShellProviderParameter.Type.INT, false, 0L)
                        .withValue(shizukuVersion));
    }

    @Override
    public ShellProviderParameter getParameter(String key) {
        for (ShellProviderParameter p : getParameters()) {
            if (p.getKey().equals(key)) return p;
        }
        return null;
    }

    @Override
    public void setParameters(List<ShellProviderParameter> parameters) {
        if (parameters == null) return;
        for (ShellProviderParameter p : parameters) {
            setParameter(p.getKey(), p);
        }
    }

    @Override
    public void setParameter(String key, ShellProviderParameter value) {
        if (PARAM_SHIZUKU_VERSION.equals(key) && value != null && value.getValue() != null) {
            this.shizukuVersion = ((Number) value.getValue()).longValue();
        }
    }

    @Override
    public List<ShellProviderSetupAction> getRequiredSetupActions() {
        return Collections.singletonList(new ShellProviderSetupAction(
                ACTION_AUTHORIZE,
                context.getString(R.string.shell_provider_shizuku_detailed_description),
                context.getString(R.string.shell_provider_action_authorize)));
    }

    @Override
    public CompletableFuture<Boolean> performSetupAction(int actionId) {
        if (actionId != ACTION_AUTHORIZE) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.supplyAsync(this::authorize);
    }

    private boolean authorize() {
        Context ctx = this.context;
        PackageManager pm = ctx.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(DataHelper.getShizukuPackageName(), 0);
            String apk = appInfo.sourceDir;
            String dataDir = MyDataDirectory.get();
            File shizuku_support = new File(dataDir, "shizuku_support");
            if (!shizuku_support.exists() && !shizuku_support.mkdir()) {
                throw new Exception("cannot mkdir");
            }

            // verify whether the shizuku is legal
            if (!VerifyShizukuFileLegal(new File(apk))) {
                showDialog(b -> b.setTitle(context.getString(R.string.shell_shizuku_fail_auth_noauth_title))
                        .setMessage(context.getString(R.string.shell_shizuku_fail_illegal_shizuku_content))
                        .setPositiveButton(context.getString(R.string.shell_shizuku_fail_auth_noauth_giveup), null));
                return false;
            }

            extractFromApk(apk, "assets/rish", new File(shizuku_support, "rish_wrapper"));
            File rishDex = new File(shizuku_support, "rish_shizuku.dex");
            if (rishDex.exists()) ModeChanger.ChangeMode(rishDex.getAbsolutePath(), 0600);
            extractFromApk(apk, "assets/rish_shizuku.dex", rishDex);
            ModeChanger.ChangeMode(rishDex.getAbsolutePath(), 0400);

            ProcessBuilder pb = rishCommand(ctx, rishDex, "/system/bin/id");
            Log.d("ShizukuProvider", "Launching : " + String.join(" ", pb.command()));
            Process process = pb.start();
            int exitCode;
            try {
                exitCode = process.waitFor();
            } finally {
                process.destroy();
            }

            if (exitCode == 0) {
                long version = 0;
                try {
                    PackageInfo pi = pm.getPackageInfo(DataHelper.getShizukuPackageName(), 0);
                    version = pi.getLongVersionCode();
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                this.shizukuVersion = version;
                runOnMain(() -> Toast.makeText(context.getApplicationContext(),
                        context.getString(R.string.shell_shizuku_success_auth_toast), Toast.LENGTH_SHORT).show());
                return true;
            }

            showDialog(b -> b.setTitle(context.getString(R.string.shell_shizuku_fail_auth_noauth_title))
                    .setMessage(context.getString(R.string.shell_shizuku_fail_auth_noauth_content))
                    .setPositiveButton(context.getString(R.string.shell_shizuku_fail_auth_noauth_giveup), null));
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            showDialog(b -> b.setTitle(context.getString(R.string.shell_shizuku_fail_auth_noinstall_title))
                    .setMessage(context.getString(R.string.shell_shizuku_fail_auth_noinstall_content))
                    .setPositiveButton(context.getString(R.string.shell_shizuku_fail_auth_noinstall_install),
                            (dialog, which) -> openShizukuDownload(context.getApplicationContext()))
                    .setNegativeButton(context.getString(R.string.shell_shizuku_fail_auth_noinstall_giveup), null));
            return false;
        } catch (Exception e) {
            Log.e("ShizukuProvider", e.toString());
            return false;
        }
    }

    private boolean VerifyShizukuFileLegal(File file) {
        ApkVerifier.Builder builder = new ApkVerifier.Builder(file);
        ApkVerifier.Result verifyResult = null;
        try {
            verifyResult = builder.build().verify();
        } catch (Exception e) {
            return false;
        }

        List<X509Certificate> certList = verifyResult.getSignerCertificates();
        class HexUtil {
            public String bytesToHex(byte[] bytes) {
                StringBuilder sb = new StringBuilder();
                for (byte aByte : bytes) {
                    sb.append(String.format("%02x", aByte));
                }
                return sb.toString();
            }
        }
        HexUtil hexUtil = new HexUtil();

        for (X509Certificate cert : certList) {
            byte[] certRaw = null;
            try {
                certRaw = cert.getEncoded();
            } catch (CertificateEncodingException e) {
                continue;
            }
            MessageDigest sha256Digest = null;
            try {
                sha256Digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                continue;
            }
            byte[] hashBytes = sha256Digest.digest(certRaw);

            String fingerprint = hexUtil.bytesToHex(hashBytes);
            if (DataHelper.checkWhetherKnownShizukuSignature(fingerprint)) return true;
        }

        return false;
    }

    private static void openShizukuDownload(Context ctx) {
        Uri webpage = Uri.parse(DataHelper.getShizukuDownloadPage());
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
        if (intent.resolveActivity(ctx.getPackageManager()) != null) {
            ctx.startActivity(intent);
        } else {
            Toast.makeText(ctx, ctx.getString(R.string.cannot_open_page), Toast.LENGTH_SHORT).show();
        }
    }

    private static ProcessBuilder rishCommand(Context ctx, File rishDex, String command) {
        ProcessBuilder pb = new ProcessBuilder("/system/bin/app_process",
                "-Djava.class.path=" + rishDex.getAbsolutePath(), "/system/bin",
                "--nice-name=rish_client", DataHelper.getShizukuShellMainClass(), command);
        Map<String, String> env = pb.environment();
        env.put("RISH_APPLICATION_ID", ctx.getPackageName());
        return pb;
    }

    private static void extractFromApk(String apkPath, String entryPath, File outFile) throws Exception {
        try (java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(apkPath)) {
            java.util.zip.ZipEntry entry = zipFile.getEntry(entryPath);
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

    @Override
    protected Process startProcess(String startupCommand) throws IOException {
        File rishDex = new File(new File(MyDataDirectory.get(), "shizuku_support"), "rish_shizuku.dex");
        if (!rishDex.exists()) {
            throw new IOException("Shizuku is not set up");
        }
        return rishCommand(context, rishDex, startupCommand).start();
    }
}
