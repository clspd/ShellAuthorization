package app.MyApp.MyShellAuthorization.MyDataStructures;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shell_providers")
public class ShellProviderDeclaration {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "type")
    public String type;

    @ColumnInfo(name = "friendly_name")
    public String friendlyName;

    @ColumnInfo(name = "parameters_json")
    public String parametersJson;
}
