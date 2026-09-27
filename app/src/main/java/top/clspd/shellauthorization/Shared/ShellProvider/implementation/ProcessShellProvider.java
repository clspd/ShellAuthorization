package top.clspd.shellauthorization.Shared.ShellProvider.implementation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;

public abstract class ProcessShellProvider implements ShellProviderInterface {

    protected Context context;
    private Process process;
    private Integer exitCode;

    @Override
    public void setContext(Context ctx) {
        this.context = ctx;
    }

    protected void showDialog(Consumer<AlertDialog.Builder> config) {
        Runnable task = () -> {
            Activity activity = resolveActivity();
            if (activity == null) return;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            config.accept(builder);
            builder.show();
        };
        if (Looper.myLooper() == Looper.getMainLooper()) task.run();
        else new Handler(Looper.getMainLooper()).post(task);
    }

    private Activity resolveActivity() {
        Context ctx = this.context;
        while (ctx instanceof ContextWrapper) {
            if (ctx instanceof Activity) {
                Activity a = (Activity) ctx;
                return (a.isFinishing() || a.isDestroyed()) ? null : a;
            }
            ctx = ((ContextWrapper) ctx).getBaseContext();
        }
        return null;
    }

    protected void runOnMain(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) r.run();
        else new Handler(Looper.getMainLooper()).post(r);
    }

    protected abstract Process startProcess(String startupCommand) throws IOException;

    @Override
    public void startup(String startupCommand) throws IOException {
        if (process != null) {
            throw new IllegalStateException("already started");
        }
        if (startupCommand == null || startupCommand.isBlank()) {
            throw new IllegalArgumentException("empty startupCommand");
        }
        this.process = startProcess(startupCommand);
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
                if (!process.waitFor(2, TimeUnit.SECONDS)) {
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
}
