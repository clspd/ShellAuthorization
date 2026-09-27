package top.clspd.shellauthorization.Shared.ShellProvider.implementation;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;

public class ExampleProvider implements ShellProviderInterface {

    private Process process;
    private Integer exitCode;

    @Override
    public void startup(String startupCommand) throws IOException {
        if (process != null) {
            throw new IllegalStateException("already started");
        }
        if (startupCommand == null || startupCommand.isBlank()) {
            throw new IllegalArgumentException("empty startupCommand");
        }

        ProcessBuilder builder = new ProcessBuilder("/system/bin/sh", "-c", startupCommand);
        builder.redirectErrorStream(false);
        this.process = builder.start();
    }

    private Process requireProcess() {
        Process p = this.process;
        if (p == null) {
            throw new IllegalStateException("not started");
        }
        return p;
    }

    @Override
    public OutputStream getStdin() {
        return requireProcess().getOutputStream();
    }

    @Override
    public InputStream getStdout() {
        return requireProcess().getInputStream();
    }

    @Override
    public InputStream getStderr() {
        return requireProcess().getErrorStream();
    }

    @Override
    public boolean isAlive() {
        return process != null && process.isAlive();
    }

    @Override
    public int waitFor() throws InterruptedException {
        int code = requireProcess().waitFor();
        this.exitCode = code;
        return code;
    }

    @Override
    public int getExitCode() {
        Process p = requireProcess();

        if (exitCode == null) {
            if (!p.isAlive()) {
                exitCode = p.exitValue();
            } else {
                throw new IllegalStateException("not exited");
            }
        }
        return exitCode;
    }

    @Override
    public void close() throws IOException {
        if (process == null) {
            return;
        }

        try {
            process.getOutputStream().close();
        } catch (IOException ignored) {

        }

        try {
            process.getInputStream().close();
        } catch (IOException ignored) {

        }

        try {
            process.getErrorStream().close();
        } catch (IOException ignored) {

        }

        if (process.isAlive()) {
            process.destroy();
            try {
                if (!process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                process.destroyForcibly();
            }
        }

        if (exitCode == null && !process.isAlive()) {
            exitCode = process.exitValue();
        }

        process = null;
    }

    // TODO: implement more...
}
