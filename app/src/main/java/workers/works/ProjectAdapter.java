package workers.works;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ConstructionApp.R;

import java.util.List;

public class ProjectAdapter
        extends RecyclerView.Adapter<ProjectAdapter.VH> {

    List<ProjectModel> list;
    Fragment fragment;

    public ProjectAdapter(List<ProjectModel> list, Fragment fragment) {
        this.list = list;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        ProjectModel p = list.get(i);

        h.name.setText(p.Name);
        h.status.setText(p.status);
        h.budget.setText("Budget: " + p.Budget);

        h.itemView.setOnClickListener(v -> {
            Bundle b = new Bundle();
            b.putString("projectId", p.id);

            ProjectDetailsFragment f = new ProjectDetailsFragment();
            f.setArguments(b);

            FragmentManager fm = fragment.getParentFragmentManager();
            if (!fm.isStateSaved()) {
                fm.beginTransaction()
                        .replace(R.id.main, f)
                        .addToBackStack(null)
                        .commit();
                h.name.setText(null);
                h.status.setText(null);
                h.budget.setText(null);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, budget, status;

        VH(View v) {
            super(v);
            name = v.findViewById(R.id.tvName);
            budget = v.findViewById(R.id.tvBudget);
            status = v.findViewById(R.id.tvStatus);
        }
    }
}

