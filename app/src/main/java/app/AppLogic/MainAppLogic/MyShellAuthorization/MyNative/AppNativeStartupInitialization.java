package app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;

public class AppNativeStartupInitialization {
    public static void InitializeLibrary() {
        System.loadLibrary("shellauthorization");
    }
    private native int DoInitialize(String jni, String lib, String apk, String data);
    public static void Initialize(Context ctx) {
        PackageManager pm = ctx.getPackageManager();
        ApplicationInfo app;
        try {
            app = pm.getApplicationInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        Log.i("AppNativeStartupInitialization", "Initializing native application, please wait...");
        // ensure "files" subdirectory exists
        File myDataDir = ctx.getFilesDir();
        if (!myDataDir.exists()) if (!myDataDir.mkdir()) throw new RuntimeException("Cannot create directory");
        Log.d("AppNativeStartupInitialization", "dataDir = " + app.dataDir);
        Log.d("AppNativeStartupInitialization", "myDataDir = " + myDataDir.getAbsolutePath());
        Log.d("AppNativeStartupInitialization", "nativeLib = " + app.nativeLibraryDir);
        int error = new AppNativeStartupInitialization().DoInitialize(
                new File(app.nativeLibraryDir, "libshellauthorization.so").getAbsolutePath(),
                new File(app.nativeLibraryDir, "libsa.so").getAbsolutePath(),
                app.sourceDir,
                myDataDir.getAbsolutePath()
        );
        if (error != 0) {
            String msg = "Fatal error: cannot initialize: error " + error;
            Log.wtf("AppNativeStartupInitialization", msg);
            throw new RuntimeException(msg);
        }
        Log.i("AppNativeStartupInitialization", "Application initialization was successfully completed");
    }
}
