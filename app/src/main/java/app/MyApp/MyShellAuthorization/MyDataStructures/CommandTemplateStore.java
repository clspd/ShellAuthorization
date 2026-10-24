package app.MyApp.MyShellAuthorization.MyDataStructures;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyDataDirectory;

public class CommandTemplateStore {

    public static final String DIRECTORY_NAME = "templates";

    private final File directory;

    public CommandTemplateStore(File directory) {
        this.directory = directory;
    }

    public static CommandTemplateStore get() {
        String root = MyDataDirectory.get();
        if (root == null) {
            throw new IllegalStateException("Data directory is not initialized");
        }
        return new CommandTemplateStore(new File(root, DIRECTORY_NAME));
    }

    public File fileFor(String name) {
        if (name == null || name.isEmpty()) return null;
        try {
            File dir = directory.getCanonicalFile();
            File file = new File(dir, name).getCanonicalFile();
            return dir.equals(file.getParentFile()) ? file : null;
        } catch (IOException e) {
            return null;
        }
    }

    public boolean exists(String name) {
        File file = fileFor(name);
        return file != null && file.isFile();
    }

    public List<String> listNames() {
        File[] files = directory.listFiles();
        List<String> names = new ArrayList<>();
        if (files == null) return names;
        for (File file : files) {
            if (file.isFile()) names.add(file.getName());
        }
        Collections.sort(names);
        return names;
    }

    public CommandTemplate read(String name) throws IOException {
        File file = fileFor(name);
        if (file == null) throw new IOException("Invalid template name");
        if (!file.isFile()) throw new IOException("Template does not exist");
        CommandTemplate template = CommandTemplate.fromJson(readFile(file));
        if (template == null) throw new IOException("Cannot parse the template file");
        return template;
    }

    public void write(String name, CommandTemplate template) throws IOException {
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IOException("Cannot create the templates directory");
        }
        File file = fileFor(name);
        if (file == null) throw new IOException("Invalid template name");
        byte[] payload = CommandTemplate.toJson(template).getBytes(StandardCharsets.UTF_8);
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(payload);
        }
    }

    public boolean delete(String name) {
        File file = fileFor(name);
        return file != null && (!file.exists() || file.delete());
    }

    private static String readFile(File file) throws IOException {
        try (FileInputStream in = new FileInputStream(file)) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
}
