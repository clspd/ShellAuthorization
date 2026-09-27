package top.clspd.shellauthorization.Shared.ShellProvider.intf;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ShellInterface extends Closeable {
    void startup(String startupCommand) throws IOException;

    OutputStream getStdin();

    InputStream getStdout();

    InputStream getStderr();

    boolean isAlive();

    int waitFor() throws InterruptedException;

    int getExitCode();

    @Override
    void close() throws IOException;
}
