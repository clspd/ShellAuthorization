package app.MyApp.MyShellAuthorization.MyPage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import app.MyApp.MyShellAuthorization.MyFragment.AppsListFragment;
import app.MyApp.MyShellAuthorization.MyFragment.HomeFragment;
import app.MyApp.MyShellAuthorization.MyFragment.SettingsFragment;
import app.MyApp.MyShellAuthorization.MyFragment.ShellProvidersFragment;
import app.MyApp.MyShellAuthorization.MyFragment.TemplatesFragment;
import top.clspd.shellauthorization.R;

/** Pages for ViewPager2. Order MUST match res/menu/main_page_bottom_nav.xml. */
public class MainPagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_COUNT = 5;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:  return ShellProvidersFragment.newInstance();
            case 2:  return AppsListFragment.newInstance();
            case 3:  return TemplatesFragment.newInstance();
            case 4:  return SettingsFragment.newInstance();
            case 0:
            default: return HomeFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }

    public static int indexOfMenuItem(int itemId) {
        if (itemId == R.id.main_page_bottom_nav_homepage) return 0;
        if (itemId == R.id.main_page_bottom_nav_shells) return 1;
        if (itemId == R.id.main_page_bottom_nav_apps) return 2;
        if (itemId == R.id.main_page_bottom_nav_templates) return 3;
        if (itemId == R.id.main_page_bottom_nav_settings) return 4;
        return -1;
    }
}
