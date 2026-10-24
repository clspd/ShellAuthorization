package app.MyApp.MyShellAuthorization.MyDataStructures;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "templates")
public class UserProfileTemplate {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name = "";

    @ColumnInfo(name = "profile_json")
    public String profileJson = "";

    public UserProfileConfig toConfig() {
        UserProfileConfig config = UserProfileConfig.fromJson(profileJson);
        return config == null ? UserProfileConfig.createDefault() : config;
    }

    public static UserProfileTemplate create(String name, UserProfileConfig config) {
        UserProfileTemplate template = new UserProfileTemplate();
        template.name = name;
        template.profileJson = UserProfileConfig.toJson(config);
        return template;
    }
}
