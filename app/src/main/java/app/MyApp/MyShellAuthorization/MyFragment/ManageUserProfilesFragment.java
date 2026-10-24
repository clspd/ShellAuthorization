package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity;
import app.MyApp.MyShellAuthorization.MyDataStructures.ProfilesDatabase;
import app.MyApp.MyShellAuthorization.MyDataStructures.UserProfileConfig;
import app.MyApp.MyShellAuthorization.MyDataStructures.UserProfileTemplate;
import top.clspd.shellauthorization.R;

public class ManageUserProfilesFragment extends Fragment {

    private final List<UserProfileTemplate> items = new ArrayList<>();
    private TemplateAdapter adapter;
    private TextView emptyView;

    public ManageUserProfilesFragment() {
        // Required empty public constructor
    }

    public static String getTitle(Context ctx) {
        return ctx.getString(R.string.user_profiles_page_title);
    }

    public static ManageUserProfilesFragment newInstance(Bundle args) {
        ManageUserProfilesFragment fragment = new ManageUserProfilesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_user_profiles, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyView = view.findViewById(R.id.textView_noUserProfiles);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_userProfiles);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TemplateAdapter(items, this::openEditor);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.floatingActionButton_addUserProfile)
                .setOnClickListener(v -> openEditor(null));
        view.findViewById(R.id.button_back)
                .setOnClickListener(v -> requireActivity().finish());
    }

    @Override
    public void onResume() {
        super.onResume();
        reload();
    }

    private void reload() {
        Context appContext = requireContext().getApplicationContext();
        new Thread(() -> {
            List<UserProfileTemplate> loaded;
            try {
                loaded = ProfilesDatabase.get(appContext).userProfileTemplateDao().getAll();
            } catch (RuntimeException e) {
                loaded = new ArrayList<>();
            }
            final List<UserProfileTemplate> result = loaded;
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                items.clear();
                items.addAll(result);
                adapter.notifyDataSetChanged();
                emptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            });
        }).start();
    }

    private void openEditor(@Nullable String templateName) {
        Bundle args = new Bundle();
        if (templateName != null) {
            args.putString(EditUserProfileTemplateFragment.ARG_NAME, templateName);
        }
        startActivity(FragmentDialogContainerActivity.createIntent(requireContext(),
                EditUserProfileTemplateFragment.class, args,
                EditUserProfileTemplateFragment.getTitle(requireContext()), false));
    }

    private static final class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.ViewHolder> {

        private final List<UserProfileTemplate> items;
        private final Consumer<String> onItemClick;

        TemplateAdapter(List<UserProfileTemplate> items, Consumer<String> onItemClick) {
            this.items = items;
            this.onItemClick = onItemClick;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_profile_template, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UserProfileTemplate item = items.get(position);
            UserProfileConfig config = item.toConfig();
            holder.name.setText(item.name);
            holder.summary.setText(holder.itemView.getContext().getString(
                    R.string.user_profile_template_summary,
                    config.user, config.group, config.groups.size()));
            holder.itemView.setOnClickListener(v -> onItemClick.accept(item.name));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class ViewHolder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView summary;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.textView_userProfileName);
                summary = itemView.findViewById(R.id.textView_userProfileSummary);
            }
        }
    }
}
