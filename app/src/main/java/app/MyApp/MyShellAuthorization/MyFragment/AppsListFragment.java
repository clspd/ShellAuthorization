package app.MyApp.MyShellAuthorization.MyFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;

import app.AppLogic.MainAppLogic.MyShellAuthorization.MyNative.AntiTamper;
import top.clspd.shellauthorization.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AppsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AppsListFragment extends Fragment {

    private static final String PREF_SORT_BY = "apps_sort_by";
    private static final String PREF_SORT_REVERSE = "apps_sort_reverse";
    private static final String PREF_EXCLUDE_SYSTEM = "apps_exclude_system";
    private static final String PREF_EXCLUDE_NON_LAUNCHABLE = "apps_exclude_non_launchable";

    private static final String SORT_BY_NAME = "name";
    private static final String SORT_BY_PACKAGE = "package";
    private static final String SORT_BY_INSTALL_TIME = "install_time";
    private static final String SORT_BY_UPDATE_TIME = "update_time";

    private static final String DEFAULT_SORT_BY = SORT_BY_UPDATE_TIME;
    private static final boolean DEFAULT_SORT_REVERSE = true;
    private static final boolean DEFAULT_EXCLUDE_SYSTEM = true;
    private static final boolean DEFAULT_EXCLUDE_NON_LAUNCHABLE = false;

    private final List<AppItem> allItems = new ArrayList<>();
    private final List<AppItem> items = new ArrayList<>();
    private AppListAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private EditText filter;

    public AppsListFragment() {
        // Required empty public constructor
    }

    public static AppsListFragment newInstance() {
        AppsListFragment fragment = new AppsListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            AntiTamper.AntiTamper_ValueMustEqual(13085, 429, 513626);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apps_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView_appsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AppListAdapter(items, this::showApplicationDetail);
        recyclerView.setAdapter(adapter);
        swipeRefresh = view.findViewById(R.id.swipeRefreshLayout_appsList);
        swipeRefresh.setOnRefreshListener(this::reloadInstalledApps);

        filter = view.findViewById(R.id.editText_appsFilter);
        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilterAndSort();
            }
        });

        view.findViewById(R.id.button_appsOptions).setOnClickListener(this::showOptionsMenu);
    }

    private void showApplicationDetail(AppItem item) {
        Bundle args = new Bundle();
        args.putString(ApplicationDetailFragment.ARG_PACKAGE, item.packageName);
        startActivity(app.MyApp.MyCommon.MySupport.MyFragmentContainer.FragmentDialogContainerActivity.createIntent(requireContext(),
                ApplicationDetailFragment.class, args,
                ApplicationDetailFragment.getTitle(requireContext()), true));
    }

    private void showOptionsMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.apps_list_options, popup.getMenu());
        Menu menu = popup.getMenu();
        SharedPreferences prefs = appPrefs();
        menu.findItem(menuItemOfSortBy(prefs.getString(PREF_SORT_BY, DEFAULT_SORT_BY))).setChecked(true);
        menu.findItem(R.id.apps_options_reverse_order).setChecked(prefs.getBoolean(PREF_SORT_REVERSE, DEFAULT_SORT_REVERSE));
        menu.findItem(R.id.apps_options_exclude_system).setChecked(prefs.getBoolean(PREF_EXCLUDE_SYSTEM, DEFAULT_EXCLUDE_SYSTEM));
        menu.findItem(R.id.apps_options_exclude_non_launchable).setChecked(prefs.getBoolean(PREF_EXCLUDE_NON_LAUNCHABLE, DEFAULT_EXCLUDE_NON_LAUNCHABLE));
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            SharedPreferences.Editor editor = prefs.edit();
            if (id == R.id.apps_options_sort_name || id == R.id.apps_options_sort_package
                    || id == R.id.apps_options_sort_install_time || id == R.id.apps_options_sort_update_time) {
                item.setChecked(true);
                editor.putString(PREF_SORT_BY, sortByOf(id));
            } else if (id == R.id.apps_options_reverse_order) {
                item.setChecked(!item.isChecked());
                editor.putBoolean(PREF_SORT_REVERSE, item.isChecked());
            } else if (id == R.id.apps_options_exclude_system) {
                item.setChecked(!item.isChecked());
                editor.putBoolean(PREF_EXCLUDE_SYSTEM, item.isChecked());
            } else if (id == R.id.apps_options_exclude_non_launchable) {
                item.setChecked(!item.isChecked());
                editor.putBoolean(PREF_EXCLUDE_NON_LAUNCHABLE, item.isChecked());
            } else {
                return false;
            }
            editor.apply();
            applyFilterAndSort();
            return true;
        });
        popup.show();
    }

    private static String sortByOf(int menuItemId) {
        if (menuItemId == R.id.apps_options_sort_package) return SORT_BY_PACKAGE;
        if (menuItemId == R.id.apps_options_sort_install_time) return SORT_BY_INSTALL_TIME;
        if (menuItemId == R.id.apps_options_sort_update_time) return SORT_BY_UPDATE_TIME;
        return SORT_BY_NAME;
    }

    private static int menuItemOfSortBy(String sortBy) {
        if (SORT_BY_PACKAGE.equals(sortBy)) return R.id.apps_options_sort_package;
        if (SORT_BY_INSTALL_TIME.equals(sortBy)) return R.id.apps_options_sort_install_time;
        if (SORT_BY_UPDATE_TIME.equals(sortBy)) return R.id.apps_options_sort_update_time;
        return R.id.apps_options_sort_name;
    }

    private SharedPreferences appPrefs() {
        return requireContext().getSharedPreferences(getString(R.string.app_prefs_name), Context.MODE_PRIVATE);
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadInstalledApps();
    }

    private void reloadInstalledApps() {
        swipeRefresh.post(() -> swipeRefresh.setRefreshing(true));
        PackageManager pm = requireContext().getPackageManager();
        new Thread(() -> {
            List<AppItem> loaded = loadInstalledApps(pm);
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                allItems.clear();
                allItems.addAll(loaded);
                applyFilterAndSort();
                swipeRefresh.setRefreshing(false);
            });
        }).start();
    }

    private static List<AppItem> loadInstalledApps(PackageManager pm) {
        Set<String> launchable = new HashSet<>();
        for (ResolveInfo ri : pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
            if (ri.activityInfo != null) launchable.add(ri.activityInfo.packageName);
        }
        List<AppItem> result = new ArrayList<>();
        for (PackageInfo pkg : pm.getInstalledPackages(0)) {
            ApplicationInfo appInfo = pkg.applicationInfo;
            if (appInfo == null) continue;
            result.add(new AppItem(
                    appInfo.loadLabel(pm).toString(),
                    pkg.packageName,
                    appInfo.loadIcon(pm),
                    pkg.firstInstallTime,
                    pkg.lastUpdateTime,
                    (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0,
                    launchable.contains(pkg.packageName)));
        }
        return result;
    }

    private void applyFilterAndSort() {
        SharedPreferences prefs = appPrefs();
        String sortBy = prefs.getString(PREF_SORT_BY, DEFAULT_SORT_BY);
        boolean reverse = prefs.getBoolean(PREF_SORT_REVERSE, DEFAULT_SORT_REVERSE);
        boolean excludeSystem = prefs.getBoolean(PREF_EXCLUDE_SYSTEM, DEFAULT_EXCLUDE_SYSTEM);
        boolean excludeNonLaunchable = prefs.getBoolean(PREF_EXCLUDE_NON_LAUNCHABLE, DEFAULT_EXCLUDE_NON_LAUNCHABLE);
        String query = filter.getText().toString().trim().toLowerCase(Locale.ROOT);

        items.clear();
        for (AppItem item : allItems) {
            if (excludeSystem && item.system) continue;
            if (excludeNonLaunchable && !item.launchable) continue;
            if (!query.isEmpty()
                    && !item.name.toLowerCase(Locale.ROOT).contains(query)
                    && !item.packageName.toLowerCase(Locale.ROOT).contains(query)) continue;
            items.add(item);
        }

        Comparator<AppItem> comparator;
        switch (sortBy) {
            case SORT_BY_PACKAGE:
                comparator = Comparator.comparing(a -> a.packageName);
                break;
            case SORT_BY_INSTALL_TIME:
                comparator = Comparator.comparingLong(a -> a.installTime);
                break;
            case SORT_BY_UPDATE_TIME:
                comparator = Comparator.comparingLong(a -> a.updateTime);
                break;
            case SORT_BY_NAME:
            default: {
                Collator collator = Collator.getInstance();
                comparator = Comparator.comparing(a -> a.name, collator);
                break;
            }
        }
        if (reverse) comparator = comparator.reversed();
        items.sort(comparator);
        adapter.notifyDataSetChanged();
    }

    private static class AppItem {
        final String name;
        final String packageName;
        final Drawable icon;
        final long installTime;
        final long updateTime;
        final boolean system;
        final boolean launchable;

        AppItem(String name, String packageName, Drawable icon, long installTime, long updateTime, boolean system, boolean launchable) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
            this.installTime = installTime;
            this.updateTime = updateTime;
            this.system = system;
            this.launchable = launchable;
        }
    }

    private static class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

        private final List<AppItem> items;
        private final Consumer<AppItem> onItemClick;

        AppListAdapter(List<AppItem> items, Consumer<AppItem> onItemClick) {
            this.items = items;
            this.onItemClick = onItemClick;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            AppItem item = items.get(position);
            holder.icon.setImageDrawable(item.icon);
            holder.name.setText(item.name);
            holder.packageName.setText(item.packageName);
            holder.itemView.setOnClickListener(v -> onItemClick.accept(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView name;
            final TextView packageName;

            ViewHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.imageView_appIcon);
                name = itemView.findViewById(R.id.textView_appName);
                packageName = itemView.findViewById(R.id.textView_appPackage);
            }
        }
    }
}
