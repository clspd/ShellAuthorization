package app.AppLogic.MainAppLogic.MyShellAuthorization;

import android.content.Context;

public class MyDataDirectory {
    private native String _get(String base);
    private static String _Mydata;
    public static void init(Context ctx) {
        _Mydata = new MyDataDirectory()._get(ctx.getFilesDir().getAbsolutePath());
    }
    public static String get() {
        return _Mydata;
    }
}
