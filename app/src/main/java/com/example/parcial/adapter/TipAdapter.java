package com.example.parcial.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parcial.databinding.ItemTipBinding;
import com.example.parcial.model.SavingTip;

import java.util.ArrayList;
import java.util.List;

public class TipAdapter extends RecyclerView.Adapter<TipAdapter.TipViewHolder> {

    private List<SavingTip> tips = new ArrayList<>();

    public TipAdapter(List<SavingTip> tips) {
        this.tips = tips;
    }

    public void setTips(List<SavingTip> tips) {
        this.tips = tips;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTipBinding binding = ItemTipBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TipViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TipViewHolder holder, int position) {
        holder.bind(tips.get(position));
    }

    @Override
    public int getItemCount() {
        return tips.size();
    }

    class TipViewHolder extends RecyclerView.ViewHolder {
        private final ItemTipBinding binding;

        TipViewHolder(ItemTipBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SavingTip tip) {
            binding.tvTipTitle.setText(tip.getTitle());
            binding.tvTipSubtitle.setText(tip.getSubtitle());
            binding.tvTipDescription.setText(tip.getFullDescription());
            binding.ivTipIcon.setImageResource(tip.getIconResId());
        }
    }
}