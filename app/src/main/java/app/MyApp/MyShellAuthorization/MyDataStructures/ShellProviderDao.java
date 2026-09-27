package app.MyApp.MyShellAuthorization.MyDataStructures;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ShellProviderDao {

    @Query("SELECT * FROM shell_providers ORDER BY name")
    List<ShellProviderDeclaration> getAll();

    @Query("SELECT * FROM shell_providers WHERE name = :name")
    ShellProviderDeclaration getByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ShellProviderDeclaration declaration);

    @Query("DELETE FROM shell_providers WHERE name = :name")
    void deleteByName(String name);
}
