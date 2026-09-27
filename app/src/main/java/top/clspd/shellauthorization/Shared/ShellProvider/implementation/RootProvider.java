package top.clspd.shellauthorization.Shared.ShellProvider.implementation;

import androidx.annotation.DrawableRes;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import top.clspd.shellauthorization.R;
import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderParameter;
import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderSetupAction;

public class RootProvider extends ProcessShellProvider {

    public static final String PROVIDER_NAME = "root";
    public static final String PARAM_SU = "su";
    public static final int ACTION_CHECK_ROOT = 1;

    private String su = "/system/bin/su";

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
        return R.drawable.ic_provider_root;
    }

    @Override
    public String getProviderFriendlyName() {
        return context.getString(R.string.shell_provider_type_root);
    }

    @Override
    public boolean isProviderFriendlyNameEditableByUser() {
        return false;
    }

    @Override
    public String getProviderDescription() {
        return context.getString(R.string.shell_provider_root_description);
    }

    @Override
    public String getProviderDetailedDescription() {
        return context.getString(R.string.shell_provider_root_detailed_description);
    }

    @Override
    public List<ShellProviderParameter> getRequiredParameters() {
        return Collections.singletonList(
                new ShellProviderParameter(PARAM_SU,
                        context.getString(R.string.shell_provider_param_su),
                        ShellProviderParameter.Type.STRING,
                        true,
                        "/system/bin/su"));
    }

    @Override
    public List<ShellProviderParameter> getParameters() {
        List<ShellProviderParameter> list = new ArrayList<>();
        for (ShellProviderParameter p : getRequiredParameters()) {
            list.add(PARAM_SU.equals(p.getKey()) ? p.withValue(su) : p);
        }
        return list;
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
        if (PARAM_SU.equals(key) && value != null && value.getValue() != null) {
            this.su = String.valueOf(value.getValue());
        }
    }

    @Override
    public List<ShellProviderSetupAction> getRequiredSetupActions() {
        return Collections.singletonList(new ShellProviderSetupAction(
                ACTION_CHECK_ROOT,
                context.getString(R.string.shell_provider_action_root_check_desc),
                context.getString(R.string.shell_provider_action_authorize)));
    }

    @Override
    public CompletableFuture<Boolean> performSetupAction(int actionId) {
        if (actionId != ACTION_CHECK_ROOT) {
            return CompletableFuture.completedFuture(false);
        }
        final String suPath = this.su;
        return CompletableFuture.supplyAsync(() -> checkRoot(suPath));
    }

    private boolean checkRoot(String suPath) {
        if (!new File(suPath).exists()) {
            showError(R.string.shell_root_fail_auth_notfound_title,
                    R.string.shell_root_fail_auth_notfound_content,
                    R.string.shell_root_fail_auth_notfound_giveup);
            return false;
        }
        try {
            Process p = Runtime.getRuntime().exec(suPath);
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("/system/bin/id -u\n");
            os.flush();
            os.close();

            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            StringBuilder total = new StringBuilder();
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            p.waitFor();

            if (!total.toString().trim().equals("0")) {
                showError(R.string.shell_root_fail_auth_noroot_title,
                        R.string.shell_root_fail_auth_noroot_content,
                        R.string.shell_root_fail_auth_noroot_giveup);
                return false;
            }
            return true;
        } catch (Exception e) {
            showError(R.string.shell_root_fail_auth_noroot_title,
                    R.string.shell_root_fail_auth_noroot_content,
                    R.string.shell_root_fail_auth_noroot_giveup);
            return false;
        }
    }

    private void showError(int titleRes, int contentRes, int buttonRes) {
        showDialog(b -> b.setTitle(context.getString(titleRes))
                .setMessage(context.getString(contentRes))
                .setPositiveButton(context.getString(buttonRes), null));
    }

    @Override
    protected Process startProcess(String startupCommand) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(su, "-c", startupCommand);
        builder.redirectErrorStream(false);
        return builder.start();
    }
}
