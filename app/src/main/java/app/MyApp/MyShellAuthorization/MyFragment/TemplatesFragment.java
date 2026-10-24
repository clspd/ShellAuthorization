package app.MyApp.MyShellAuthorization.MyFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity;
import app.MyApp.MyShellAuthorization.MyDataStructures.CommandTemplateStore;
import top.clspd.shellauthorization.R;

public class TemplatesFragment extends Fragment {

    private final List<String> items = new ArrayList<>();
    private TemplateAdapter adapter;
    private TextView emptyView;

    public TemplatesFragment() {
        // Required empty public constructor
    }

    public static TemplatesFragment newInstance() {
        TemplatesFragment fragment = new TemplatesFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustWithin2(320480663, 19390801, 20260925, 310088640, 360720960);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_templates, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyView = view.findViewById(R.id.textView_noTemplates);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_templates);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TemplateAdapter(items, this::openTemplate);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.floatingActionButton_addTemplate)
                .setOnClickListener(v -> openTemplate(null));
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void openTemplate(@Nullable String templateName) {
        Bundle args = new Bundle();
        if (templateName != null) {
            args.putString(EditTemplateFragment.ARG_TEMPLATE_NAME, templateName);
        }
        startActivity(FragmentDialogContainerActivity.createIntent(requireContext(),
                EditTemplateFragment.class, args,
                EditTemplateFragment.getTitle(requireContext()), false));
    }

    private void reload() {
        new Thread(() -> {
            List<String> loaded;
            try {
                loaded = CommandTemplateStore.get().listNames();
            } catch (RuntimeException e) {
                loaded = new ArrayList<>();
            }
            final List<String> result = loaded;
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                items.clear();
                items.addAll(result);
                adapter.notifyDataSetChanged();
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private static final class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

        private final List<String> items;
        private final Consumer<String> onItemClick;

        TemplateAdapter(List<String> items, Consumer<String> onItemClick) {
            this.items = items;
            this.onItemClick = onItemClick;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_command_template, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = items.get(position);
            holder.name.setText(name);
            holder.itemView.setOnClickListener(v -> onItemClick.accept(name));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final TextView name;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.textView_templateName);
            }
        }
    }
}
