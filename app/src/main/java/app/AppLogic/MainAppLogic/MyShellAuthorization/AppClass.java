package app.AppLogic.MainAppLogic.MyShellAuthorization;

public class AppClass extends android.app.Application {
    @Override public void onCreate() {
        super.onCreate();

        System.loadLibrary("shellauthorization");
    }
}
