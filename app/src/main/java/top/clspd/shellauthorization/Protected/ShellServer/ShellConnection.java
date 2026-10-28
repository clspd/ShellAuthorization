package top.clspd.shellauthorization.Protected.ShellServer;

import android.content.Context;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Pair;

import java.io.IOException;

import app.MyApp.MyShellAuthorization.MyDataStructures.UidAuthorizationStore;
import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;

public class ShellConnection {
    private Context context;
    private final int uid;
    private final IBinder client;

    private ShellProviderInterface shell;

    public ShellConnection(Context context, int uid, IBinder client) {
        this.context = context;
        this.uid = uid;
        this.client = client;
    }

    public int getUid() {
        return uid;
    }

    public Pair<Boolean, String> start(String templateName, String[] args, ParcelFileDescriptor parcelFileDescriptor) {
        // TODO: query app allowance, query template, determine which provider to use, ...

        // query app allowance
        UidAuthorizationStore store = UidAuthorizationStore.get();
        if (!store.isAuthorized(context.getPackageManager(), getUid())) {
            return new Pair<Boolean, String>(false, "Internal error occurred"); // hide detailed error message for unauthorized applications
        }

        if (false) new Thread(() -> {
            try (
                    ParcelFileDescriptor socket = parcelFileDescriptor;
                    java.io.InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(socket);
                    java.io.OutputStream out = new ParcelFileDescriptor.AutoCloseOutputStream(socket)
            ) {
                byte[] buffer = new byte[8192];
                while (shell != null /*&& shell.*/) {
                    int count = in.read(buffer);
                    if (count == -1) {
                        break;
                    }
                    byte[] received = java.util.Arrays.copyOf(
                            buffer, count
                    );
                    processBytes(received);
                    byte[] response = "OK\n".getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    );
                    out.write(response);
                    out.flush();
                }
            } catch (IOException e) {
                android.util.Log.i(
                        "BinderServer",
                        "Pipe disconnected",
                        e
                );
            }
        }, "PipelineWorker").start();

        return new Pair<Boolean, String>(false, "Not implemented");
    }

    private void processBytes(byte[] data) {
        // forward to shell

    }
}
