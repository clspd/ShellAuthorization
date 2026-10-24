package app.MyApp.MyShellAuthorization.MyDataStructures;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserProfileTemplate.class}, version = 1, exportSchema = false)
public abstract class ProfilesDatabase extends RoomDatabase {

    private static ProfilesDatabase instance;

    public abstract UserProfileTemplateDao userProfileTemplateDao();

    public static synchronized ProfilesDatabase get(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    ProfilesDatabase.class, "profiles.db").build();
        }
        return instance;
    }
}
