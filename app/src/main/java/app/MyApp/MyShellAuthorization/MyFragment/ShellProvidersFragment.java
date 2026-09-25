package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyDataDirectory;
import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShellProvidersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShellProvidersFragment extends Fragment {

    private final List<ShellProviderItem> items = new ArrayList<>();
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
        adapter = new ShellProviderAdapter(items);
        recyclerView.setAdapter(adapter);
        // TODO: list item click action

        view.findViewById(R.id.floatingActionButton_addShellProvider).setOnClickListener(v -> {
//            startActivity(app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity.createIntent(requireContext(), AddShellProviderFragment.class, new Bundle(),
//                    AddShellProviderFragment.getTitle(requireContext()), false));
            startActivity(app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentContainerActivity.createIntent(requireContext(), AddShellProviderFragment.class, new Bundle()));
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadShellProviders();
    }

    private void reloadShellProviders() {
        items.clear();
        File dir = new File(MyDataDirectory.get(), "shells");
        File[] files = dir.listFiles();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File f : files) {
                if (!f.isFile()) continue;
                ShellProviderItem item = parseShellProvider(f);
                if (item != null) items.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private static ShellProviderItem parseShellProvider(File file) {
        try (Reader reader = new FileReader(file)) {
            JsonObject o = new Gson().fromJson(reader, JsonObject.class);
            if (o == null || !o.has("type")) return null;
            String type = o.get("type").getAsString();
            String su = o.has("su") ? o.get("su").getAsString() : null;
            long shizukuVersion = o.has("shizuku_version") ? o.get("shizuku_version").getAsLong() : 0;
            return new ShellProviderItem(type, su, shizukuVersion);
        } catch (Exception e) {
            return null;
        }
    }

    private static class ShellProviderItem {
        final String type;
        final String su;
        final long shizukuVersion;

        ShellProviderItem(String type, String su, long shizukuVersion) {
            this.type = type;
            this.su = su;
            this.shizukuVersion = shizukuVersion;
        }
    }

    private static class ShellProviderAdapter extends RecyclerView.Adapter<ShellProviderAdapter.ViewHolder> {

        private final List<ShellProviderItem> items;

        ShellProviderAdapter(List<ShellProviderItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shell_provider, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ShellProviderItem item = items.get(position);
            Context ctx = holder.itemView.getContext();
            switch (item.type) {
                case "root":
                    holder.title.setText(ctx.getString(R.string.shell_provider_type_root));
                    holder.subtitle.setText(ctx.getString(R.string.shell_provider_root_su_format,
                            item.su == null ? "" : item.su));
                    break;
                case "shizuku":
                    holder.title.setText(ctx.getString(R.string.shell_provider_type_shizuku));
                    holder.subtitle.setText(ctx.getString(R.string.shell_provider_shizuku_version_format,
                            item.shizukuVersion));
                    break;
                default:
                    holder.title.setText(item.type);
                    holder.subtitle.setText("");
                    break;
            }
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
