package workers.works;

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
                        return ProjectListFragment.newInstance("pending");
                    case 1:
                        return ProjectListFragment.newInstance("active");
                    case 2:
                        return ProjectListFragment.newInstance("completed");
                    case 3:
                        return ProjectListFragment.newInstance("cancelled");
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
                    tab.setText("Pending");
                    break;
                case 1:
                    tab.setText("Active");
                    break;
                case 2:
                    tab.setText("Completed");
                    break;
                case 3:
                    tab.setText("Cancelled");
                    break;
                case 4:
                    tab.setText("All");
                    break;
            }
        }).attach();

        return v;
    }
}

