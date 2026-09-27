package app.MyApp.MyShellAuthorization.MyDataStructures;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ShellProviderDeclaration.class}, version = 1, exportSchema = false)
public abstract class ShellsDatabase extends RoomDatabase {

    private static ShellsDatabase instance;

    public abstract ShellProviderDao shellProviderDao();

    public static synchronized ShellsDatabase get(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), ShellsDatabase.class, "shells.db").build();
        }
        return instance;
    }
}
