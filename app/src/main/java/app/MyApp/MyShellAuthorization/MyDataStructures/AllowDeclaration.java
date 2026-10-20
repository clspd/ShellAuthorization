package app.MyApp.MyShellAuthorization.MyDataStructures;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * An authorization file, stored as a JSON file in the allowlist/ directory, named after the UID it
 * authorizes.
 *
 * <p>The file records the UID and the earliest {@code firstInstallTime} among all packages sharing
 * that UID at the time the authorization was granted. A later check recomputes the same value; if it
 * differs, the UID must be treated as no longer belonging to the same application and the file is
 * discarded.</p>
 */
public class AllowDeclaration {

    @SerializedName("uid")
    public Integer uid;

    @SerializedName("earliest_first_install_time")
    public Long earliestFirstInstallTime;

    public static AllowDeclaration create(int uid, long earliestFirstInstallTime) {
        AllowDeclaration d = new AllowDeclaration();
        d.uid = uid;
        d.earliestFirstInstallTime = earliestFirstInstallTime;
        return d;
    }

    /**
     * Reads an allow file.
     *
     * @return null if the file is unreadable or does not contain a valid UID and install time.
     */
    public static AllowDeclaration read(File file) {
        try (Reader reader = new FileReader(file)) {
            AllowDeclaration d = new Gson().fromJson(reader, AllowDeclaration.class);
            if (d == null || d.uid == null || d.earliestFirstInstallTime == null) return null;
            if (d.uid <= 0 || d.earliestFirstInstallTime < 0) return null;
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks whether this declaration still describes the same UID: the recorded earliest
     * firstInstallTime must equal the currently computed one.
     */
    public boolean isStillValidFor(int uid, long currentEarliestFirstInstallTime) {
        return this.uid != null && this.uid == uid
                && this.earliestFirstInstallTime != null
                && this.earliestFirstInstallTime == currentEarliestFirstInstallTime;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public void write(File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(toJson());
            writer.flush();
        }
    }
}
