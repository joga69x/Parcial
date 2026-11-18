package com.example.parcial.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parcial.databinding.ItemContributionBinding;
import com.example.parcial.model.Contribution;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContributionAdapter extends RecyclerView.Adapter<ContributionAdapter.ContributionViewHolder> {

    public interface OnContributionActionListener {
        void onDeleteContribution(Contribution contribution);
    }

    private List<Contribution> contributions = new ArrayList<>();
    private final OnContributionActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, new Locale("es", "CO"));

    public ContributionAdapter(OnContributionActionListener listener) {
        this.listener = listener;
        currencyFormat.setMaximumFractionDigits(0);
    }

    public void setContributions(List<Contribution> list) {
        this.contributions = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContributionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContributionBinding binding = ItemContributionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContributionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ContributionViewHolder holder, int position) {
        holder.bind(contributions.get(position));
    }

    @Override
    public int getItemCount() {
        return contributions.size();
    }

    class ContributionViewHolder extends RecyclerView.ViewHolder {
        private final ItemContributionBinding binding;

        ContributionViewHolder(ItemContributionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Contribution contrib) {
            binding.tvContribGoalTitle.setText(contrib.getGoalTitle());
            binding.tvContribNote.setText(contrib.getNote().isEmpty() ? "Aporte a la meta" : contrib.getNote());
            binding.tvContribDate.setText(dateFormat.format(new Date(contrib.getDate())));
            binding.tvContribAmount.setText("+" + currencyFormat.format(contrib.getAmount()));

            binding.btnDeleteContrib.setOnClickListener(v -> {
                if (listener != null) listener.onDeleteContribution(contrib);
            });
        }
    }
}