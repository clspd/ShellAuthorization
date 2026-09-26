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
 * A shell provider declaration, stored as a JSON file in the shells/ directory.
 */
public class ShellProviderDeclaration {

    public static final String TYPE_ROOT = "root";
    public static final String TYPE_SHIZUKU = "shizuku";

    @SerializedName("type")
    public String type;

    @SerializedName("su")
    public String su;

    // boxed so the key is left out of the JSON when it is not set
    @SerializedName("shizuku_version")
    public Long shizukuVersion;

    // the file this declaration was read from or written to, not part of the JSON
    public transient File file;

    public static ShellProviderDeclaration createRoot(String su) {
        ShellProviderDeclaration d = new ShellProviderDeclaration();
        d.type = TYPE_ROOT;
        d.su = su;
        return d;
    }

    public static ShellProviderDeclaration createShizuku(long shizukuVersion) {
        ShellProviderDeclaration d = new ShellProviderDeclaration();
        d.type = TYPE_SHIZUKU;
        d.shizukuVersion = shizukuVersion;
        return d;
    }

    /**
     * Reads a declaration file.
     *
     * @return null if the file is unreadable or does not declare a type.
     */
    public static ShellProviderDeclaration read(File file) {
        try (Reader reader = new FileReader(file)) {
            ShellProviderDeclaration d = new Gson().fromJson(reader, ShellProviderDeclaration.class);
            if (d == null || d.type == null) return null;
            d.file = file;
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public long getShizukuVersion() {
        return shizukuVersion == null ? 0 : shizukuVersion;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    public void write(File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(toJson());
            writer.flush();
        }
        this.file = file;
    }
}
