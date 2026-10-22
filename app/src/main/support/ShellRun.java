package top.clspd.shellauthorization.Public.Shell;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import dalvik.system.BaseDexClassLoader;

public class ShellRun {
    public static void main(String[] args) {
        try {
            System.exit(RealMain(args));
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            System.err.flush();
            throw new RuntimeException(e);
        }
    }


    private static String[] args;
    private static Handler handler;

    private static final Binder binder = new Binder() {

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                IBinder binder = data.readStrongBinder();

                String sourceDir = data.readString();
                if (binder != null && sourceDir != null) {
                    handler.post(() -> {
                        try {
                            onBinderReceived(binder, sourceDir);
                        } catch (Exception e) {
                            e.printStackTrace(System.err);
                            System.err.flush();
                            System.exit(1);
                        }
                    });
                } else {
                    System.err.println("Cannot connect");
                    System.err.flush();
                    System.exit(1);
                }
                return true;
            }
            return super.onTransact(code, data, reply, flags);
        }
    };

    private static int RealMain(String[] _args) throws Exception {
        args = _args;

        if (Looper.myLooper() == null) {
            Looper.prepare();
        }

        try { tryExemptHiddenApis(); } catch (Exception ignored) {}

        handler = new Handler(Objects.requireNonNull(Looper.myLooper()));

        requestBinder();

        handler.postDelayed(() -> abort(new TimeoutException("Timeout").toString()), 5000);

        Looper.loop();

        return 0;
    }

    @SuppressLint("DiscouragedPrivateApi")
    private static void tryExemptHiddenApis() throws Exception {
        Class<?> vmRuntimeClass = Class.forName("dalvik.system.VMRuntime");

        Method getRuntime = vmRuntimeClass.getDeclaredMethod("getRuntime");

        Object runtime = getRuntime.invoke(null);

        Method setExemptions =
                vmRuntimeClass.getDeclaredMethod(
                        "setHiddenApiExemptions",
                        String[].class
                );

        setExemptions.invoke(
                runtime,
                (Object) new String[]{"L"}
        );
    }

    @SuppressLint({"ObsoleteSdkInt", "PrivateApi", "DiscouragedPrivateApi", "SoonBlockedPrivateApi"})
    private static void requestBinder() throws Exception {
        Bundle data = new Bundle();
        data.putBinder("binder", binder);

        String pkg = System.getenv().getOrDefault("SHELL_AUTHORIZATION_APPLICATION_NAME", "top.clspd.shellauthorization");

        Intent intent = new Intent("top.clspd.shellauthorization.intent.action.REQUEST_BINDER")
                .setPackage(pkg)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .putExtra("data", data);

        // android.os.ServiceManager.getService("activity")
        Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
        Method getService = serviceManagerClass.getDeclaredMethod("getService", String.class);

        IBinder amBinder = (IBinder) getService.invoke(null, "activity");
        if (amBinder == null) {
            throw new IllegalStateException(
                    "ActivityManager service is unavailable"
            );
        }

        // android.app.IActivityManager.Stub.asInterface(IBinder)
        Class<?> stubClass = Class.forName("android.app.IActivityManager$Stub");
        Method asInterface = stubClass.getDeclaredMethod(
                "asInterface",
                IBinder.class
        );

        Object am = asInterface.invoke(null, amBinder);

        Class<?> activityManagerClass =
                Class.forName("android.app.IActivityManager");

        Method broadcastIntent = findBroadcastIntentMethod(
                activityManagerClass
        );

        // broadcastIntent will fail on Android 8.x but our minSdk is 28
        broadcastIntent.invoke(
                am,
                null,       // IApplicationThread caller
                intent,     // Intent intent
                null,       // String resolvedType
                null,       // IIntentReceiver resultTo
                0,          // int resultCode
                null,       // String resultData
                null,       // Bundle map
                null,       // String[] requiredPermissions
                -1,         // int appOp
                null,       // Bundle options
                true,       // boolean serialized
                false,      // boolean sticky
                0           // int userId
        );

    }

    private static Method findBroadcastIntentMethod(
            Class<?> activityManagerClass
    ) throws NoSuchMethodException {

        for (Method method : activityManagerClass.getDeclaredMethods()) {
            if (!"broadcastIntent".equals(method.getName())) {
                continue;
            }

            Class<?>[] p = method.getParameterTypes();

            if (p.length == 13
                    && p[1] == Intent.class
                    && p[4] == int.class
                    && p[8] == int.class
                    && p[10] == boolean.class
                    && p[11] == boolean.class
                    && p[12] == int.class) {
                return method;
            }
        }

        throw new NoSuchMethodException(
                "Cannot find compatible IActivityManager.broadcastIntent"
        );
    }

    private static void onBinderReceived(IBinder binder, String sourceDir) throws Exception {
        var base = sourceDir.substring(0, sourceDir.lastIndexOf('/'));
        String librarySearchPath = base + "/lib/" + getRuntimeInstructionSet();
        String systemLibrarySearchPath = System.getProperty("java.library.path");
        if (!TextUtils.isEmpty(systemLibrarySearchPath)) {
            librarySearchPath += File.pathSeparatorChar + systemLibrarySearchPath;
        }

        try {
            var classLoader = new BaseDexClassLoader(sourceDir, null, librarySearchPath, ClassLoader.getSystemClassLoader());
            Class<?> cls = classLoader.loadClass("top.clspd.shellauthorization.Public.Shell.ShellLauncher");
            cls.getDeclaredMethod("main", String[].class, IBinder.class, Handler.class)
                    .invoke(null, args, binder, handler);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
            System.err.flush();
            System.exit(1);
        } catch (Throwable e) {
            e.printStackTrace(System.err);
            System.err.flush();
            System.exit(1);
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    private static String getRuntimeInstructionSet() throws Exception {
        Class<?> vmRuntimeClass = Class.forName("dalvik.system.VMRuntime");

        Method getRuntime = vmRuntimeClass.getDeclaredMethod("getRuntime");

        Object runtime = getRuntime.invoke(null);

        Method vmInstructionSet = vmRuntimeClass.getDeclaredMethod("vmInstructionSet");

        return (String) vmInstructionSet.invoke(runtime);
    }

    private static void abort(String message) {
        System.err.println(message);
        System.err.flush();
        System.exit(1);
    }

}
