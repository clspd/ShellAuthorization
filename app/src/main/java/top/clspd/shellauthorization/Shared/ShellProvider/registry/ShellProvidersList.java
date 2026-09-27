package top.clspd.shellauthorization.Shared.ShellProvider.registry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import top.clspd.shellauthorization.Shared.ShellProvider.implementation.RootProvider;
import top.clspd.shellauthorization.Shared.ShellProvider.implementation.ShizukuProvider;
import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;

public class ShellProvidersList {

    private static final List<Class<? extends ShellProviderInterface>> PROVIDERS = Collections.unmodifiableList(Arrays.asList(
            RootProvider.class,
            ShizukuProvider.class
    ));

    public static List<Class<? extends ShellProviderInterface>> getList() {
        return PROVIDERS;
    }

    public static Class<? extends ShellProviderInterface> findByProviderName(String name) {
        if (name == null) return null;
        for (Class<? extends ShellProviderInterface> c : PROVIDERS) {
            try {
                if (name.equals(c.getField("PROVIDER_NAME").get(null))) {
                    return c;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
