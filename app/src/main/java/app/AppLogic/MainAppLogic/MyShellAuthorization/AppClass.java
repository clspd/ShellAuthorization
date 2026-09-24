package app.AppLogic.MainAppLogic.MyShellAuthorization;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AppNativeStartupInitialization;

public class AppClass extends android.app.Application {
    @Override public void onCreate() {
        super.onCreate();

        AppNativeStartupInitialization.InitializeLibrary();
        AppNativeStartupInitialization.Initialize(this);
        MyDataDirectory.init(this);
    }
}
