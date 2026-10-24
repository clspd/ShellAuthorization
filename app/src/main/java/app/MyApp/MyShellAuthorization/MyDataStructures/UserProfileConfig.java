package app.MyApp.MyShellAuthorization.MyDataStructures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class UserProfileConfig {

    public static final long MAX_UNSIGNED_INT = 0xFFFFFFFFL;
    public static final int MAX_GROUPS = 64;
    public static final int MAX_CONTEXT_BYTES = 255;
    public static final BigInteger MAX_CAPABILITIES =
            BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);

    public static final String FIELD_USER = "user";
    public static final String FIELD_GROUP = "group";
    public static final String FIELD_GROUPS = "groups";
    public static final String FIELD_CAPABILITIES = "capabilities";
    public static final String FIELD_CONTEXT = "context";
    public static final String FIELD_UNSHARE = "unshare";

    private static final Gson GSON = new GsonBuilder().create();

    @SerializedName("user")
    public long user;

    @SerializedName("group")
    public long group;

    @SerializedName("groups")
    public List<Long> groups = new ArrayList<>();

    @SerializedName("capabilities")
    public BigInteger capabilities = BigInteger.ZERO;

    @SerializedName("context")
    public String context = "";

    @SerializedName("unshare")
    public long unshare;

    @SerializedName("flags")
    public Flags flags = new Flags();

    public static class Flags {
        @SerializedName("no_new_privs")
        public boolean noNewPrivs;

        public Flags copy() {
            Flags f = new Flags();
            f.noNewPrivs = noNewPrivs;
            return f;
        }
    }

    public static UserProfileConfig createDefault() {
        return new UserProfileConfig();
    }

    public UserProfileConfig copy() {
        UserProfileConfig c = new UserProfileConfig();
        c.user = user;
        c.group = group;
        c.groups = new ArrayList<>(groups);
        c.capabilities = capabilities;
        c.context = context;
        c.unshare = unshare;
        c.flags = flags.copy();
        return c;
    }

    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (user < 0 || user > MAX_UNSIGNED_INT) {
            errors.put(FIELD_USER, "UID must be between 0 and " + MAX_UNSIGNED_INT + ".");
        }
        if (group < 0 || group > MAX_UNSIGNED_INT) {
            errors.put(FIELD_GROUP, "GID must be between 0 and " + MAX_UNSIGNED_INT + ".");
        }
        if (groups == null) {
            errors.put(FIELD_GROUPS, "Cannot read the selected groups.");
        } else if (groups.size() > MAX_GROUPS) {
            errors.put(FIELD_GROUPS, "At most " + MAX_GROUPS + " groups are supported.");
        } else {
            for (Long gid : groups) {
                if (gid == null || gid < 0 || gid > MAX_UNSIGNED_INT) {
                    errors.put(FIELD_GROUPS, "Invalid group ID.");
                    break;
                }
            }
        }
        if (capabilities == null || capabilities.signum() < 0
                || capabilities.compareTo(MAX_CAPABILITIES) > 0) {
            errors.put(FIELD_CAPABILITIES, "Capabilities must fit in an unsigned 64-bit integer.");
        }
        if (context == null) {
            errors.put(FIELD_CONTEXT, "SELinux context cannot be null.");
        } else if (context.getBytes(StandardCharsets.UTF_8).length > MAX_CONTEXT_BYTES) {
            errors.put(FIELD_CONTEXT, "SELinux context is longer than " + MAX_CONTEXT_BYTES + " bytes.");
        }
        return errors;
    }

    public void normalize() {
        if (groups == null) {
            groups = new ArrayList<>();
        } else if (!groups.isEmpty()) {
            groups = new ArrayList<>(new LinkedHashSet<>(groups));
        }
        while (groups.size() > MAX_GROUPS) {
            groups.remove(groups.size() - 1);
        }
        if (capabilities == null) capabilities = BigInteger.ZERO;
        if (context == null) context = "";
        if (flags == null) flags = new Flags();
    }

    public static String toJson(UserProfileConfig config) {
        if (config == null) return null;
        config.normalize();
        return GSON.toJson(config);
    }

    public static UserProfileConfig fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            UserProfileConfig config = GSON.fromJson(json, UserProfileConfig.class);
            if (config == null) return null;
            config.normalize();
            return config;
        } catch (Exception e) {
            return null;
        }
    }

    public static long parseUnsigned32(String text) {
        BigInteger value = parseUnsigned(text);
        if (value.bitLength() > 32) {
            throw new NumberFormatException("Value does not fit in 32 bits.");
        }
        return value.longValue();
    }

    public static BigInteger parseUnsigned(String text) {
        if (text == null) throw new NumberFormatException("Empty value.");
        if (text.isEmpty()) throw new NumberFormatException("Empty value.");
        int radix = 10;
        String digits = text;
        if (digits.startsWith("0x") || digits.startsWith("0X")) {
            radix = 16;
            digits = digits.substring(2);
            if (digits.isEmpty()) throw new NumberFormatException("Missing digits after 0x.");
        }
        BigInteger value = new BigInteger(digits, radix);
        if (value.signum() < 0) throw new NumberFormatException("Value must not be negative.");
        return value;
    }

    public static String formatCapabilities(BigInteger capabilities) {
        return capabilities == null ? "0" : capabilities.toString(10);
    }
}
