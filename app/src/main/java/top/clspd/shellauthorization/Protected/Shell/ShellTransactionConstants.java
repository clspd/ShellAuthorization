package top.clspd.shellauthorization.Protected.Shell;

import android.os.IBinder;

public final class ShellTransactionConstants {
    public static final int PROTOCOL_VERSION = 0x10001;

    public static final int CODE_PING = IBinder.FIRST_CALL_TRANSACTION + 1;
    public static final int CODE_START = IBinder.FIRST_CALL_TRANSACTION + 2;

    public static final String INTERFACE_NAME = "top.clspd.shellauthorization.Protected.ShellServer.ShellTransactionService.V_1";
}
