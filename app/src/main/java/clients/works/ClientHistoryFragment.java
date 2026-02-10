package clients.works;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.ConstructionApp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Main history fragment for clients showing all their projects/requests
 * Displays tabs: All, Active, Completed, Cancelled, Pending
 */
public class ClientHistoryFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_works, container, false);

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupViewPager();
        setupTabs();

        return view;
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return ClientProjectListFragment.newInstance("all");
                    case 1:
                        return ClientProjectListFragment.newInstance("pending");
                    case 2:
                        return ClientProjectListFragment.newInstance("active");
                    case 3:
                        return ClientProjectListFragment.newInstance("completed");
                    case 4:
                        return ClientProjectListFragment.newInstance("cancelled");
                    default:
                        return ClientProjectListFragment.newInstance("all");
                }
            }

            @Override
            public int getItemCount() {
                return 5; // All, Pending, Active, Completed, Cancelled
            }
        });
    }

    private void setupTabs() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("All");
                    break;
                case 1:
                    tab.setText("Pending");
                    break;
                case 2:
                    tab.setText("Active");
                    break;
                case 3:
                    tab.setText("Completed");
                    break;
                case 4:
                    tab.setText("Cancelled");
                    break;
            }
        }).attach();
    }
}