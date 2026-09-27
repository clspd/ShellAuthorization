package app.MyApp.MyShellAuthorization.MyWidget;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderParameter;

public class ParameterFormBuilder {

    private static final int KEY_TAG = 0x70617261;

    public static ViewGroup build(Context ctx, List<ShellProviderParameter> schema) {
        LinearLayout form = new LinearLayout(ctx);
        form.setOrientation(LinearLayout.VERTICAL);
        for (ShellProviderParameter p : schema) {
            form.addView(buildField(ctx, p));
        }
        return form;
    }

    private static View buildField(Context ctx, ShellProviderParameter p) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(ctx, 12);
        row.setLayoutParams(rowLp);

        TextView label = new TextView(ctx);
        String text = p.getLabel() == null ? p.getKey() : p.getLabel();
        if (p.isRequired()) text = text + " *";
        label.setText(text);
        label.setTextSize(12.5f);
        label.setLetterSpacing(0.03f);
        row.addView(label);

        Object current = p.getValue();

        switch (p.getType()) {
            case BOOLEAN: {
                Switch sw = new Switch(ctx);
                sw.setTag(KEY_TAG, p.getKey());
                sw.setChecked(current instanceof Boolean && (Boolean) current);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(ctx, 4);
                row.addView(sw, lp);
                break;
            }
            case INT: {
                EditText et = new EditText(ctx);
                et.setTag(KEY_TAG, p.getKey());
                et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                if (current != null) et.setText(String.valueOf(current));
                row.addView(et, editParams(ctx));
                break;
            }
            case STRING:
            default: {
                EditText et = new EditText(ctx);
                et.setTag(KEY_TAG, p.getKey());
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                if (current != null) et.setText(String.valueOf(current));
                row.addView(et, editParams(ctx));
                break;
            }
        }
        return row;
    }

    private static LinearLayout.LayoutParams editParams(Context ctx) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(ctx, 4);
        return lp;
    }

    private static int dp(Context ctx, int v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }

    public static List<ShellProviderParameter> collect(ViewGroup form, List<ShellProviderParameter> schema) {
        Map<String, Object> values = readValues(form, schema);
        List<ShellProviderParameter> result = new ArrayList<>();
        for (ShellProviderParameter p : schema) {
            result.add(p.withValue(values.get(p.getKey())));
        }
        return result;
    }

    public static boolean validate(ViewGroup form, List<ShellProviderParameter> schema) {
        Map<String, Object> values = readValues(form, schema);
        boolean ok = true;
        for (int i = 0; i < schema.size(); i++) {
            ShellProviderParameter p = schema.get(i);
            View input = findInput(form.getChildAt(i));
            boolean valid = true;
            if (p.isRequired()) {
                Object v = values.get(p.getKey());
                if (v == null) valid = false;
                else if (v instanceof String && ((String) v).trim().isEmpty()) valid = false;
            }
            if (input instanceof EditText) {
                ((EditText) input).setError(valid ? null : "Required");
            }
            if (!valid) ok = false;
        }
        return ok;
    }

    private static View findInput(View row) {
        if (!(row instanceof ViewGroup)) return null;
        ViewGroup g = (ViewGroup) row;
        for (int i = 0; i < g.getChildCount(); i++) {
            View c = g.getChildAt(i);
            if (c instanceof EditText || c instanceof Switch) return c;
        }
        return null;
    }

    private static Map<String, Object> readValues(ViewGroup form, List<ShellProviderParameter> schema) {
        Map<String, Object> values = new HashMap<>();
        for (int i = 0; i < schema.size(); i++) {
            ShellProviderParameter p = schema.get(i);
            View input = findInput(form.getChildAt(i));
            if (input == null) {
                values.put(p.getKey(), p.getValue());
                continue;
            }
            if (input instanceof Switch) {
                values.put(p.getKey(), ((Switch) input).isChecked());
            } else if (input instanceof EditText) {
                String text = ((EditText) input).getText().toString().trim();
                if (text.isEmpty()) {
                    values.put(p.getKey(), null);
                    continue;
                }
                if (p.getType() == ShellProviderParameter.Type.INT) {
                    try {
                        values.put(p.getKey(), Long.parseLong(text));
                    } catch (NumberFormatException e) {
                        values.put(p.getKey(), null);
                    }
                } else {
                    values.put(p.getKey(), text);
                }
            }
        }
        return values;
    }
}
