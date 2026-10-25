package top.clspd.shellauthorization.Public.Client;

import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;

import top.clspd.shellauthorization.Protected.Shell.ShellTransactionConstants;

public class ShellClient {
    protected IBinder binder;

    public ShellClient(IBinder binder1) {
        binder = binder1;
    }

    public boolean ping() {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        try {
            data.writeInterfaceToken(ShellTransactionConstants.INTERFACE_NAME);
            boolean success = binder.transact(ShellTransactionConstants.CODE_PING, data, reply, 0);
            if (success) {
                int resultCode = reply.readInt();
                if (resultCode != ShellTransactionConstants.PROTOCOL_VERSION) throw new UnsupportedOperationException("Protocol version not supported");
                return true;
            }
            return false;
        } catch (RemoteException e) {
            return false;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
