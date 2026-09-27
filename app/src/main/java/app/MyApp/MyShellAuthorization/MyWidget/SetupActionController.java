package app.MyApp.MyShellAuthorization.MyWidget;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import top.clspd.shellauthorization.Shared.ShellProvider.data.ShellProviderSetupAction;
import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;

public class SetupActionController {

    public interface Listener {
        void onActionFinished(int actionId, boolean success);
        void onActionsReady();
    }

    private final ShellProviderInterface provider;
    private final List<ShellProviderSetupAction> actions;
    private final Listener listener;
    private final Set<Integer> completed = new HashSet<>();
    private final ViewGroup buttonHost;
    private final Runnable beforeAction;

    public SetupActionController(ShellProviderInterface provider, ViewGroup buttonHost,
                                 Runnable beforeAction, Listener listener) {
        this.provider = provider;
        this.actions = provider.getRequiredSetupActions();
        this.buttonHost = buttonHost;
        this.beforeAction = beforeAction;
        this.listener = listener;
        buildButtons();
        if (actions.isEmpty() && listener != null) {
            listener.onActionsReady();
        }
    }

    private void buildButtons() {
        Context ctx = buttonHost.getContext();
        for (ShellProviderSetupAction action : actions) {
            LinearLayout block = new LinearLayout(ctx);
            block.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams blockLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            blockLp.bottomMargin = dp(ctx, 12);
            block.setLayoutParams(blockLp);

            String desc = action.getDescription();
            if (desc != null && !desc.isEmpty()) {
                TextView tv = new TextView(ctx);
                tv.setText(desc);
                tv.setTextSize(14f);
                block.addView(tv);
            }

            Button btn = new Button(ctx);
            btn.setText(action.getActionButtonText());
            btn.setAllCaps(false);
            btn.setOnClickListener(v -> runAction(action, btn, block));
            LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            btnLp.topMargin = dp(ctx, 8);
            block.addView(btn, btnLp);

            buttonHost.addView(block);
        }
    }

    private void runAction(ShellProviderSetupAction action, Button btn, ViewGroup block) {
        if (beforeAction != null) beforeAction.run();

        btn.setEnabled(false);
        Context ctx = buttonHost.getContext();
        ProgressBar bar = new ProgressBar(ctx, null, android.R.attr.progressBarStyleSmall);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barLp.topMargin = dp(ctx, 8);
        block.addView(bar, barLp);

        CompletableFuture<Boolean> future = provider.performSetupAction(action.getId());
        future.whenComplete((ok, err) -> btn.post(() -> {
            block.removeView(bar);
            boolean success = err == null && Boolean.TRUE.equals(ok);
            if (success) {
                completed.add(action.getId());
                btn.setEnabled(false);
                btn.setText("✓ " + btn.getText());
            } else {
                btn.setEnabled(true);
            }
            if (listener != null) {
                listener.onActionFinished(action.getId(), success);
                if (isAllCompleted()) {
                    listener.onActionsReady();
                }
            }
        }));
    }

    public boolean isAllCompleted() {
        return completed.size() >= actions.size();
    }

    private static int dp(Context ctx, int v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }
}
