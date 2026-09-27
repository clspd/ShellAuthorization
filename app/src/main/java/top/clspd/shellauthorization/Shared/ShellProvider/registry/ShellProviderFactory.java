package top.clspd.shellauthorization.Shared.ShellProvider.registry;

import android.content.Context;

import java.util.List;

import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderParameter;
import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;

public class ShellProviderFactory {

    public static ShellProviderInterface create(String providerName, Context ctx) throws Exception {
        Class<? extends ShellProviderInterface> c = ShellProvidersList.findByProviderName(providerName);
        if (c == null) {
            throw new IllegalArgumentException("unknown provider: " + providerName);
        }
        ShellProviderInterface provider = c.getDeclaredConstructor().newInstance();
        provider.setContext(ctx);
        return provider;
    }

    public static ShellProviderInterface restore(String providerName, Context ctx,
                                                 List<ShellProviderParameter> params) throws Exception {
        ShellProviderInterface provider = create(providerName, ctx);
        if (params != null) {
            provider.setParameters(params);
        }
        return provider;
    }
}
