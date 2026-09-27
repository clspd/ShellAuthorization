package app.MyApp.MyShellAuthorization.MyDataStructures;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderParameter;
import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;
import top.clspd.shellauthorization.Shared.ShellProvider.registry.ShellProviderFactory;

public class ShellProviderRepository {

    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    public static void save(Context appContext, String identifier, String providerName,
                            String friendlyName, ShellProviderInterface provider) {
        ShellProviderDeclaration d = new ShellProviderDeclaration();
        d.name = identifier;
        d.type = providerName;
        d.friendlyName = friendlyName;
        d.parametersJson = encodeParameters(provider.getParameters());
        ShellsDatabase.get(appContext).shellProviderDao().insert(d);
    }

    public static ShellProviderInterface load(Context appContext, ShellProviderDeclaration d) throws Exception {
        List<ShellProviderParameter> params = decodeParameters(d.parametersJson);
        return ShellProviderFactory.restore(d.type, appContext, params);
    }

    public static String toDisplaySummary(ShellProviderDeclaration d) {
        Map<String, Object> map = decodeMap(d.parametersJson);
        if (map.isEmpty()) return "";
        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            parts.add(e.getKey() + ": " + e.getValue());
        }
        return String.join("\n", parts);
    }

    public static String displayName(ShellProviderDeclaration d) {
        if (d.friendlyName != null && !d.friendlyName.isEmpty()) return d.friendlyName;
        return d.name;
    }

    private static String encodeParameters(List<ShellProviderParameter> params) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (params != null) {
            for (ShellProviderParameter p : params) {
                map.put(p.getKey(), p.getValue());
            }
        }
        return GSON.toJson(map);
    }

    private static Map<String, Object> decodeMap(String json) {
        if (json == null || json.isEmpty()) return new LinkedHashMap<>();
        Map<String, Object> map = GSON.fromJson(json, MAP_TYPE);
        return map == null ? new LinkedHashMap<>() : map;
    }

    private static List<ShellProviderParameter> decodeParameters(String json) {
        Map<String, Object> map = decodeMap(json);
        List<ShellProviderParameter> list = new ArrayList<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            Object v = e.getValue();
            ShellProviderParameter.Type t;
            if (v instanceof Boolean) t = ShellProviderParameter.Type.BOOLEAN;
            else if (v instanceof Number) t = ShellProviderParameter.Type.INT;
            else t = ShellProviderParameter.Type.STRING;
            list.add(new ShellProviderParameter(e.getKey(), e.getKey(), t, false, null).withValue(v));
        }
        return list;
    }
}
