package app.MyApp.MyShellAuthorization.MyDataStructures;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A shell provider declaration, stored in the shells.db database.
 */
@Entity(tableName = "shell_providers")
public class ShellProviderDeclaration {

    public static final String TYPE_ROOT = "root";
    public static final String TYPE_SHIZUKU = "shizuku";

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "su")
    public String su;

    @ColumnInfo(name = "shizuku_version")
    public Long shizukuVersion;

    public static ShellProviderDeclaration createRoot(String su) {
        ShellProviderDeclaration d = new ShellProviderDeclaration();
        d.name = "Root";
        d.type = TYPE_ROOT;
        d.su = su;
        return d;
    }

    public static ShellProviderDeclaration createShizuku(long shizukuVersion) {
        ShellProviderDeclaration d = new ShellProviderDeclaration();
        d.name = "Shizuku";
        d.type = TYPE_SHIZUKU;
        d.shizukuVersion = shizukuVersion;
        return d;
    }

    public long getShizukuVersion() {
        return shizukuVersion == null ? 0 : shizukuVersion;
    }
}
