package app.MyApp.MyShellAuthorization.MyFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderRepository;
import app.MyApp.MyShellAuthorization.MyWidget.ShellProviderSetupPanel;
import top.clspd.shellauthorization.R;
import top.clspd.shellauthorization.Shared.ShellProvider.intf.ShellProviderInterface;
import top.clspd.shellauthorization.Shared.ShellProvider.registry.ShellProvidersList;

public class AddShellProviderFragment extends Fragment {

    public AddShellProviderFragment() {
    }

    public static AddShellProviderFragment newInstance(Bundle bundle) {
        AddShellProviderFragment fragment = new AddShellProviderFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.fragment_add_shell_provider_title);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual2(0x36b141bb, 0x293f10a2, 0x283f10ae,
                    AntiTamper.AntiTamper_ComputeValue2(51966, 16777216, 0));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_shell_provider, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView subtitle = view.findViewById(R.id.textView_addShellProviderSubtitle);
        subtitle.setText(R.string.add_shell_provider_subtitle);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_availableProviders);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        List<ShellProviderInterface> providers = new ArrayList<>();
        Context host = requireContext();
        for (Class<? extends ShellProviderInterface> c : ShellProvidersList.getList()) {
            try {
                ShellProviderInterface p = c.getDeclaredConstructor().newInstance();
                p.setContext(host);
                providers.add(p);
            } catch (Exception ignored) {
            }
        }

        recyclerView.setAdapter(new AvailableProviderAdapter(providers, this::showSetupDialog));
    }

    private void showSetupDialog(ShellProviderInterface provider) {
        Context ctx = requireContext();
        ShellProviderSetupPanel panel = new ShellProviderSetupPanel(provider, this::onProviderSaved);
        View content = panel.build(ctx);

        new AlertDialog.Builder(ctx)
                .setTitle(provider.getProviderFriendlyName())
                .setView(content)
                .setNegativeButton(R.string.Cancel, null)
                .show();
    }

    private void onProviderSaved(String identifier, String providerName, String friendlyName,
                                 ShellProviderInterface provider) {
        Context appContext = requireContext().getApplicationContext();
        Activity activity = requireActivity();
        new Thread(() -> {
            ShellProviderRepository.save(appContext, identifier, providerName, friendlyName, provider);
            activity.runOnUiThread(() -> {
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            });
        }).start();
    }

    private static class AvailableProviderAdapter
            extends RecyclerView.Adapter<AvailableProviderAdapter.ViewHolder> {

        private final List<ShellProviderInterface> items;
        private final java.util.function.Consumer<ShellProviderInterface> onUse;

        AvailableProviderAdapter(List<ShellProviderInterface> items,
                                 java.util.function.Consumer<ShellProviderInterface> onUse) {
            this.items = items;
            this.onUse = onUse;
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_add_shell_provider, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ShellProviderInterface item = items.get(position);
            Context ctx = holder.itemView.getContext();
            holder.icon.setImageResource(item.getProviderIconResourceIdentifier());
            holder.title.setText(item.getProviderFriendlyName());
            holder.desc.setText(item.getProviderDescription());
            holder.use.setText(R.string.add_shell_provider_use_this);
            holder.use.setOnClickListener(v -> onUse.accept(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title;
            final TextView desc;
            final Button use;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.imageView_providerIcon);
                title = itemView.findViewById(R.id.textView_providerTitle);
                desc = itemView.findViewById(R.id.textView_providerDesc);
                use = itemView.findViewById(R.id.button_useProvider);
            }
        }
    }
}
