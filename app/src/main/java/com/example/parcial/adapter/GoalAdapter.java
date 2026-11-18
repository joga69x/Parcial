package com.example.parcial.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parcial.R;
import com.example.parcial.databinding.ItemGoalBinding;
import com.example.parcial.model.SavingGoal;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    public interface OnGoalActionListener {
        void onSelectActive(SavingGoal goal);
        void onAddDeposit(SavingGoal goal);
        void onDeleteGoal(SavingGoal goal);
    }

    private List<SavingGoal> goals = new ArrayList<>();
    private String activeGoalId = "";
    private final OnGoalActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));

    public GoalAdapter(OnGoalActionListener listener) {
        this.listener = listener;
        currencyFormat.setMaximumFractionDigits(0);
    }

    public void setGoals(List<SavingGoal> goals, String activeGoalId) {
        this.goals = goals;
        this.activeGoalId = activeGoalId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGoalBinding binding = ItemGoalBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GoalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        holder.bind(goals.get(position));
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    class GoalViewHolder extends RecyclerView.ViewHolder {
        private final ItemGoalBinding binding;

        GoalViewHolder(ItemGoalBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SavingGoal goal) {
            binding.tvGoalTitle.setText(goal.getTitle());
            binding.tvGoalCategory.setText("Categoría: " + goal.getCategory());
            binding.tvCurrentAmount.setText(currencyFormat.format(goal.getCurrentAmount()));
            binding.tvTargetAmount.setText("de " + currencyFormat.format(goal.getTargetAmount()));

            int pct = goal.getProgressPercentage();
            binding.tvProgressPct.setText(pct + "%");
            binding.progressGoal.setProgress(pct);

            binding.tvSuggestedDeposit.setText("Sugerido: " + currencyFormat.format(goal.getSuggestedDepositDaily()) + " / día");
            binding.tvDaysLeft.setText(goal.getRemainingDays() + " días restantes");

            boolean isActive = goal.getId().equals(activeGoalId);
            binding.tvActiveBadge.setVisibility(isActive ? View.VISIBLE : View.GONE);
            if (isActive) {
                binding.btnSelectActive.setText("✓ Meta Principal");
                binding.btnSelectActive.setEnabled(false);
            } else {
                binding.btnSelectActive.setText("Fijar como Principal");
                binding.btnSelectActive.setEnabled(true);
            }

            // Category Icon mapping
            int iconRes = R.drawable.ic_piggy_bank;
            if ("Emergencia".equalsIgnoreCase(goal.getCategory())) {
                iconRes = R.drawable.ic_emergency;
            } else if ("Viajes".equalsIgnoreCase(goal.getCategory())) {
                iconRes = R.drawable.ic_travel;
            } else if ("Vehículo".equalsIgnoreCase(goal.getCategory())) {
                iconRes = R.drawable.ic_car;
            } else if ("Tecnología".equalsIgnoreCase(goal.getCategory())) {
                iconRes = R.drawable.ic_tech;
            } else if ("Educación".equalsIgnoreCase(goal.getCategory())) {
                iconRes = R.drawable.ic_education;
            }
            binding.ivCategoryIcon.setImageResource(iconRes);

            binding.btnSelectActive.setOnClickListener(v -> {
                if (listener != null) listener.onSelectActive(goal);
            });

            binding.btnAddDeposit.setOnClickListener(v -> {
                if (listener != null) listener.onAddDeposit(goal);
            });

            binding.btnDeleteGoal.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteGoal(goal);
            });
        }
    }
}