package app.MyApp.MyShellAuthorization.MyFragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyDataDirectory;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyShellAuthorization.MyDataStructures.ShellProviderDeclaration;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellProvidersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellProvidersFragment extends Fragment {

    private final List<ShellProviderDeclaration> items = new ArrayList<>();
    private ShellProviderAdapter adapter;

    public ShellProvidersFragment() {
        // Required empty public constructor
    }

    public static ShellProvidersFragment newInstance() {
        ShellProvidersFragment fragment = new ShellProvidersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual2(1638, 938, -935, 2184);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shell_providers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_shellProviders);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ShellProviderAdapter(items, this::showShellProviderDetails);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.floatingActionButton_addShellProvider).setOnClickListener(v -> {
            startActivity(app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentContainerActivity.createIntent(requireContext(), AddShellProviderFragment.class, new Bundle()));
        });
    }

    private void showShellProviderDetails(ShellProviderDeclaration item) {
        Bundle args = new Bundle();
        args.putString(ShellProviderDetailsFragment.ARG_FILE, item.file.getAbsolutePath());
        startActivity(app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity.createIntent(requireContext(),
                ShellProviderDetailsFragment.class, args,
                ShellProviderDetailsFragment.getTitle(requireContext()), true));
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadShellProviders();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void reloadShellProviders() {
        items.clear();
        File dir = new File(MyDataDirectory.get(), "shells");
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File f : files) {
                if (!f.isFile()) continue;
                ShellProviderDeclaration item = ShellProviderDeclaration.read(f);
                if (item != null) items.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static class ShellProviderAdapter extends RecyclerView.Adapter<ShellProviderAdapter.ViewHolder> {

        private final List<ShellProviderDeclaration> items;
        private final Consumer<ShellProviderDeclaration> onItemClick;

        ShellProviderAdapter(List<ShellProviderDeclaration> items, Consumer<ShellProviderDeclaration> onItemClick) {
            this.items = items;
            this.onItemClick = onItemClick;
        }

        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shell_provider, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ShellProviderDeclaration item = items.get(position);
            Context ctx = holder.itemView.getContext();
            switch (item.type) {
                case ShellProviderDeclaration.TYPE_ROOT:
                    holder.title.setText(ctx.getString(R.string.shell_provider_type_root));
                    holder.subtitle.setText(ctx.getString(R.string.shell_provider_root_su_format,
                            item.su == null ? "" : item.su));
                    break;
                case ShellProviderDeclaration.TYPE_SHIZUKU:
                    holder.title.setText(ctx.getString(R.string.shell_provider_type_shizuku));
                    holder.subtitle.setText(ctx.getString(R.string.shell_provider_shizuku_version_format,
                            item.getShizukuVersion()));
                    break;
                default:
                    holder.title.setText(item.type);
                    holder.subtitle.setText("");
                    break;
            }
            holder.itemView.setOnClickListener(v -> onItemClick.accept(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView subtitle;

            ViewHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.textView_shellProviderTitle);
                subtitle = itemView.findViewById(R.id.textView_shellProviderSubtitle);
            }
        }
    }
}
