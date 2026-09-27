package app.MyApp.MyShellAuthorization.MyWidget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import top.clspd.shellauthorization.R;
import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderParameter;
import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;

public class ShellProviderSetupPanel {

    public interface Callback {
        void onSave(String identifier, String providerName, String friendlyName, ShellProviderInterface provider);
    }

    private final ShellProviderInterface provider;
    private final Callback callback;
    private final List<ShellProviderParameter> schema;

    private ViewGroup formHost;
    private ViewGroup actionHost;
    private EditText friendlyNameInput;
    private Button saveButton;
    private SetupActionController actionController;

    public ShellProviderSetupPanel(ShellProviderInterface provider, Callback callback) {
        this.provider = provider;
        this.callback = callback;
        this.schema = provider.getRequiredParameters();
    }

    public ViewGroup build(Context ctx) {
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(20 * ctx.getResources().getDisplayMetrics().density);
        root.setPadding(pad, pad, pad, pad);

        String detailed = provider.getProviderDetailedDescription();
        if (detailed != null && !detailed.isEmpty()) {
            TextView desc = new TextView(ctx);
            desc.setText(detailed);
            desc.setTextSize(14f);
            LinearLayout.LayoutParams descLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            descLp.bottomMargin = pad;
            root.addView(desc, descLp);
        }

        if (!schema.isEmpty()) {
            formHost = ParameterFormBuilder.build(ctx, schema);
            root.addView(formHost);
        }

        if (provider.isProviderFriendlyNameEditableByUser()) {
            TextView nameLabel = new TextView(ctx);
            nameLabel.setText(R.string.shell_provider_friendly_name);
            nameLabel.setTextSize(12.5f);
            root.addView(nameLabel);

            friendlyNameInput = new EditText(ctx);
            friendlyNameInput.setSingleLine(true);
            friendlyNameInput.setText(provider.getProviderFriendlyName());
            root.addView(friendlyNameInput, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        actionHost = new LinearLayout(ctx);
        ((LinearLayout) actionHost).setOrientation(LinearLayout.VERTICAL);
        root.addView(actionHost);

        actionController = new SetupActionController(provider, actionHost,
                this::applyFormToProvider, new SetupActionController.Listener() {
            @Override
            public void onActionFinished(int actionId, boolean success) {
                updateSaveState();
            }

            @Override
            public void onActionsReady() {
                updateSaveState();
            }
        });

        saveButton = new Button(ctx);
        saveButton.setText(R.string.shell_provider_save);
        saveButton.setAllCaps(false);
        saveButton.setOnClickListener(v -> onSave());
        root.addView(saveButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (formHost != null) {
            watchFormChanges();
        }
        updateSaveState();
        return root;
    }

    private void watchFormChanges() {
        for (int i = 0; i < formHost.getChildCount(); i++) {
            ViewGroup row = (ViewGroup) formHost.getChildAt(i);
            for (int j = 0; j < row.getChildCount(); j++) {
                if (row.getChildAt(j) instanceof EditText) {
                    ((EditText) row.getChildAt(j)).addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int a, int b, int c) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int a, int b, int c) {
                            updateSaveState();
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                        }
                    });
                }
            }
        }
    }

    private void applyFormToProvider() {
        if (formHost == null) return;
        List<ShellProviderParameter> values = ParameterFormBuilder.collect(formHost, schema);
        provider.setParameters(values);
    }

    private boolean requiredFilled() {
        if (schema.isEmpty()) return true;
        if (formHost == null) return false;
        return ParameterFormBuilder.validate(formHost, schema);
    }

    private void updateSaveState() {
        if (saveButton == null) return;
        boolean ready = requiredFilled() && actionController.isAllCompleted();
        saveButton.setEnabled(ready);
    }

    private void onSave() {
        applyFormToProvider();
        if (!ParameterFormBuilder.validate(formHost, schema)) {
            updateSaveState();
            return;
        }
        String friendlyName = provider.getProviderFriendlyName();
        if (friendlyNameInput != null) {
            String typed = friendlyNameInput.getText().toString().trim();
            if (!typed.isEmpty()) friendlyName = typed;
        }
        callback.onSave(provider.generateProviderIdentifier(), provider.getProviderName(),
                friendlyName, provider);
    }
}
