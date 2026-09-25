package app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative;

public class SignalSender {
    public static native int SendSignal(int pid, int sig);
}
