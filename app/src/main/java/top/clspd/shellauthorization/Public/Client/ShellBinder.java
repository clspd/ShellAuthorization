package top.clspd.shellauthorization.Public.Client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import top.clspd.shellauthorization.Protected.ShellServer.ShellTransactionService;

public class ShellBinder {
    protected volatile IBinder binder;
    private ServiceConnection conn;

    public void setBinder(IBinder binder1) {
        binder = binder1;
    }

    public IBinder getBinder() {
        return binder;
    }

    public boolean bind(Context context, long timeout) {
        final CountDownLatch latch = new CountDownLatch(1);
        final IBinder[] resultBinder = new IBinder[1];

        conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                resultBinder[0] = service;
                latch.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };

        Intent intent = new Intent(context, ShellTransactionService.class);

        if (!context.bindService(intent, conn, Context.BIND_AUTO_CREATE)) return false;

        try {
            if (!latch.await(timeout, TimeUnit.MILLISECONDS)) throw new InterruptedException();
        } catch (InterruptedException e) {
            context.unbindService(conn);
            return false;
        }

        binder = resultBinder[0];
        return true;
    }

    public void unbind(Context context) {
        if (conn != null) {
            try {
                context.unbindService(conn);
            } catch (Exception ignored) {}
            conn = null;
        }
        binder = null;
    }
}
