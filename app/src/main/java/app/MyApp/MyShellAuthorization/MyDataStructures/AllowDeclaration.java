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
 * An allow file, stored as a JSON file in the allowlist/ directory, named after the package it allows.
 */
public class AllowDeclaration {

    @SerializedName("package")
    public String packageName;

    public static AllowDeclaration create(String packageName) {
        AllowDeclaration d = new AllowDeclaration();
        d.packageName = packageName;
        return d;
    }

    /**
     * Reads an allow file.
     *
     * @return null if the file is unreadable or does not name a package.
     */
    public static AllowDeclaration read(File file) {
        try (Reader reader = new FileReader(file)) {
            AllowDeclaration d = new Gson().fromJson(reader, AllowDeclaration.class);
            return d == null || d.packageName == null ? null : d;
        } catch (Exception e) {
            return null;
        }
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
