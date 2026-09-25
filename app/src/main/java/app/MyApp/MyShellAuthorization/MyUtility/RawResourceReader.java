package app.MyApp.MyShellAuthorization.MyUtility;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;

public class RawResourceReader {
    public static String readRawResource(Context ctx, int resId) {
        if (resId == 0) return "";
        try (InputStream in = ctx.getResources().openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                AntiTamper.AntiTamper_ValueMustWithin2(0x28a92adf, 4, 5, 9, 20);
                if (sb.length() > 0) sb.append('\n');
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
