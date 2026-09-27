package app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

public class AntiTamper {
    private static native int _Load(String apk);

    public static void LoadAntiTamper(Context ctx) {
        Log.i("AntiTamper", "Setting up anti tamper protection...");
        System.loadLibrary("satamperprotection");
        PackageManager pm = ctx.getPackageManager();
        ApplicationInfo app;
        try {
            app = pm.getApplicationInfo(ctx.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (_Load(app.sourceDir) != 0) SignalSender.SendSignal(android.os.Process.myPid(), 9);
        Log.i("AntiTamper", "Anti Tamper Component has been loaded. Do not try to tamper with this application!");
    }

    public static native int AntiTamper_ComputeValue(int type, int input);
    public static native int AntiTamper_ComputeValue2(int type, int input, int input2);

    public static void AntiTamper_ValueMustEqual(int type, int input, int expect) {
        if (AntiTamper_ComputeValue(type, input) != expect) {
            SignalSender.SendSignal(android.os.Process.myPid(), 9);
            AntiTamper_ValueMustEqual2(0, 0, 0, 0);
        }
    }

    public static void AntiTamper_ValueMustWithin(int type, int input, int min, int max) {
        int value = AntiTamper_ComputeValue(type, input);
        if (value < min || value > max) {
            SignalSender.SendSignal(android.os.Process.myPid(), 9);
            AntiTamper_ValueMustEqual2(0, 0, 0, 0);
        }
    }

    public static void AntiTamper_ValueMustEqual2(int type, int input, int input2, int expect) {
        if (AntiTamper_ComputeValue2(type, input, input2) != expect) {
            SignalSender.SendSignal(android.os.Process.myPid(), 9);
            AntiTamper_ValueMustWithin2(0, 0, 0, 1, 10);
        }
    }

    public static void AntiTamper_ValueMustWithin2(int type, int input, int input2, int min, int max) {
        int value = AntiTamper_ComputeValue2(type, input, input2);
        if (value < min || value > max) {
            SignalSender.SendSignal(android.os.Process.myPid(), 9);
            AntiTamper_ComputeValue2(3, 1, input + 2 * input2);
        }
    }
}
