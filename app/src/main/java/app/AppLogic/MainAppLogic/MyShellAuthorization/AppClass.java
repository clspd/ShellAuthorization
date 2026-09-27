package app.AppLogic.MainAppLogic.MyShellAuthorization;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AppNativeStartupInitialization;

public class AppClass extends android.app.Application {
    @Override public void onCreate() {
        super.onCreate();

        AntiTamper.LoadAntiTamper(this);
        AppNativeStartupInitialization.InitializeLibrary();
        AppNativeStartupInitialization.Initialize(this);
        MyDataDirectory.init(this);
        AntiTamper.AntiTamper_ValueMustEqual(0x1fff000, 0x000000d, 0x027613b);
    }
}
