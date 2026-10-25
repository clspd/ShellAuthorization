package top.clspd.shellauthorization.Protected.ShellServer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import app.MyApp.MyShellAuthorization.MyDataStructures.UidAuthorizationStore;
import top.clspd.shellauthorization.Protected.Shell.ShellTransactionConstants;

public class ShellTransactionService extends Service {
    private final Map<IBinder, ShellConnection> clientSessionMap = new ConcurrentHashMap<>();

    private final IBinder myBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, @NonNull Parcel data, Parcel reply, int flags) throws RemoteException {
            {
                // not checked, check it
                int callingUid = Binder.getCallingUid();
                // we do NOT give special bypass for root
                UidAuthorizationStore store = UidAuthorizationStore.get();
                if (!store.isAuthorized(getPackageManager(), callingUid)) {
                    return false;
                }
            }
            if (code == ShellTransactionConstants.CODE_PING) {
                data.enforceInterface(ShellTransactionConstants.INTERFACE_NAME);
                if (reply != null) {
                    reply.writeInt(ShellTransactionConstants.PROTOCOL_VERSION);
                }
                return true;
            }
            if (code == ShellTransactionConstants.CODE_START) {
                data.enforceInterface(ShellTransactionConstants.INTERFACE_NAME);
                IBinder clientBinder = data.readStrongBinder();
                String templateName = data.readString();
                String[] cmd = data.createStringArray();
                if (clientBinder == null || templateName == null || cmd == null) {
                    if (reply != null) {
                        reply.writeInt(22);
                        reply.writeString("Malformed arguments");
                    }
                    return true;
                }
                if (clientSessionMap.containsKey(clientBinder)) {
                    if (reply != null) {
                        reply.writeInt(5); // EIO
                        reply.writeString("Client is attempting to create duplicate connection");
                    }
                    return true;
                }
                ShellConnection conn = new ShellConnection(Binder.getCallingUid(), clientBinder);
                clientSessionMap.put(clientBinder, conn);
                try {
                    clientBinder.linkToDeath(() -> clientSessionMap.remove(clientBinder), 0);
                } catch (RemoteException e) {
                    Log.w("ShellTransactionService", "Cannot link client binder to death");
                }
                ParcelFileDescriptor[] pair;
                try {
                    pair = ParcelFileDescriptor.createReliableSocketPair();
                } catch (IOException e) {
                    reply.writeInt(1);
                    reply.writeString("Cannot create standard streams");
                    return true;
                }
                ParcelFileDescriptor clientSide = pair[0];
                ParcelFileDescriptor serverSide = pair[1];
                Pair<Boolean, String> result = conn.start(templateName, cmd, serverSide);
                if (reply != null) {
                    reply.writeInt(result.first ? 0 : 1);
                    reply.writeString(result.second);
                }
                if (result.first) {
                    reply.writeNoException();
                    reply.writeTypedObject(
                            clientSide,
                            Parcelable.PARCELABLE_WRITE_RETURN_VALUE
                    );
                }
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    };

    public ShellTransactionService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
}