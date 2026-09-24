package app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative;

public class SignalSender {
    public native int SendSignal(int pid, int sig);
}
