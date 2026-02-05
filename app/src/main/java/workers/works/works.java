package workers.works;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ConstructionApp.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import clients.works.ProjectListFragment;

public class works extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_works, container, false);

        ViewPager2 viewPager = v.findViewById(R.id.viewPager);
        TabLayout tabLayout = v.findViewById(R.id.tabLayout);

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int pos) {
                switch (pos) {
                    case 0:
                        return workers.works.ProjectListFragment.newInstance("active");
                    case 1:
                        return workers.works.ProjectListFragment.newInstance("completed");
                    case 2:
                        return workers.works.ProjectListFragment.newInstance("cancelled");
                    case 3:
                        return workers.works.ProjectListFragment.newInstance("pending");
                    default:
                        return ProjectListFragment.newInstance("all");
                }
            }

            @Override
            public int getItemCount() {
                return 4;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, pos) -> {
            switch (pos) {
                case 0:
                    tab.setText("Active");
                    break;
                case 1:
                    tab.setText("Completed");
                    break;
                case 2:
                    tab.setText("Cancelled");
                    break;
                case 3:
                    tab.setText("Pending");
                    break;
            }
        }).attach();

        return v;
    }
}