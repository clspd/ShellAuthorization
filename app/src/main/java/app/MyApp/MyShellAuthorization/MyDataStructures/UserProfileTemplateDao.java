package app.MyApp.MyShellAuthorization.MyDataStructures;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserProfileTemplateDao {

    @Query("SELECT * FROM templates ORDER BY name")
    List<UserProfileTemplate> getAll();

    @Query("SELECT * FROM templates WHERE name = :name")
    UserProfileTemplate getByName(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserProfileTemplate template);

    @Query("DELETE FROM templates WHERE name = :name")
    void deleteByName(String name);
}
