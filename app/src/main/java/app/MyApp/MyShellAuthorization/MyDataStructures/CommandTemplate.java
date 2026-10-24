package app.MyApp.MyShellAuthorization.MyDataStructures;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class CommandTemplate {

    public static final int VERSION = 1;

    private static final Gson GSON = new GsonBuilder().create();

    @SerializedName("version")
    public int version = VERSION;

    @SerializedName("provider")
    public String provider;

    @SerializedName("profile")
    public Profile profile = new Profile();

    @SerializedName("command")
    public String command = "";

    public static class Profile {

        public static final String TYPE_DEFAULT = "default";
        public static final String TYPE_TEMPLATE = "template";
        public static final String TYPE_CUSTOM = "custom";

        @SerializedName("type")
        public String type = TYPE_DEFAULT;

        /** Name of the persistent user profile template; only set for {@link #TYPE_TEMPLATE}. */
        @SerializedName("template")
        public String template;

        /** Inline profile; only set for {@link #TYPE_CUSTOM}. */
        @SerializedName("data")
        public UserProfileConfig data;

        public Profile copy() {
            Profile p = new Profile();
            p.type = type;
            p.template = template;
            p.data = data == null ? null : data.copy();
            return p;
        }

        public static Profile defaultProfile() {
            return new Profile();
        }

        public static Profile ofTemplate(String templateName) {
            Profile p = new Profile();
            p.type = TYPE_TEMPLATE;
            p.template = templateName;
            return p;
        }

        public static Profile ofCustom(UserProfileConfig config) {
            Profile p = new Profile();
            p.type = TYPE_CUSTOM;
            p.data = config.copy();
            return p;
        }

        public static String toJson(Profile profile) {
            return profile == null ? null : GSON.toJson(profile);
        }

        public static Profile fromJson(String json) {
            if (json == null || json.isEmpty()) return null;
            try {
                Profile p = GSON.fromJson(json, Profile.class);
                if (p == null) return null;
                if (p.type == null) p.type = TYPE_DEFAULT;
                return p;
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static String toJson(CommandTemplate template) {
        return template == null ? null : GSON.toJson(template);
    }

    public static CommandTemplate fromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        try {
            CommandTemplate template = GSON.fromJson(json, CommandTemplate.class);
            if (template == null) return null;
            if (template.profile == null) template.profile = new Profile();
            if (template.command == null) template.command = "";
            return template;
        } catch (Exception e) {
            return null;
        }
    }
}
