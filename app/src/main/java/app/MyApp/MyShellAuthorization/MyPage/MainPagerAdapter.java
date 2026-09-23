package app.MyApp.MyShellAuthorization.MyPage;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import app.MyApp.MyShellAuthorization.MyFragment.AppsListFragment;
import app.MyApp.MyShellAuthorization.MyFragment.HomeFragment;
import app.MyApp.MyShellAuthorization.MyFragment.SettingsFragment;
import app.MyApp.MyShellAuthorization.MyFragment.TemplatesFragment;

/** Pages for ViewPager2. Order MUST match res/menu/main_page_bottom_nav.xml. */
public class MainPagerAdapter extends FragmentStateAdapter {

    public static final int PAGE_COUNT = 4;

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:  return new AppsListFragment();
            case 2:  return new TemplatesFragment();
            case 3:  return new SettingsFragment();
            case 0:
            default: return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return PAGE_COUNT;
    }
}
