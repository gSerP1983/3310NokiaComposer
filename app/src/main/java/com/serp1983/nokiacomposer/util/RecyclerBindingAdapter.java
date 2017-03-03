package com.serp1983.nokiacomposer.util;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;

public class RecyclerBindingAdapter<T>
        extends RecyclerView.Adapter<RecyclerBindingAdapter.BindingHolder> {

    private final int holderLayout, variableId;
    private ArrayList<T> items;
    private ArrayList<T> itemsCopy;
    private OnItemClickListener<T> onItemClickListener;
    private OnAfterBindViewHolderListener<T> onAfterBindViewHolderListener;

    public RecyclerBindingAdapter(int holderLayout, int variableId, ArrayList<T> items) {
        this.holderLayout = holderLayout;
        this.variableId = variableId;
        this.items = items;
        this.itemsCopy = (ArrayList<T>) items.clone();
    }

    @Override
    public RecyclerBindingAdapter.BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(holderLayout, parent, false);
        return new BindingHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerBindingAdapter.BindingHolder holder, final int position) {
        final T item = items.get(position);
        holder.getBinding().getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null)
                    onItemClickListener.onItemClick(position, item);
            }
        });
        holder.getBinding().setVariable(variableId, item);

        if (onAfterBindViewHolderListener != null)
            onAfterBindViewHolderListener.onAfterBindViewHolder(holder, item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnAfterBindViewHolderListener(OnAfterBindViewHolderListener<T> onAfterBindViewHolderListener) {
        this.onAfterBindViewHolderListener = onAfterBindViewHolderListener;
    }

    public void filter(String text) {
        items.clear();
        if(TextUtils.isEmpty(text)){
            items.addAll(itemsCopy);
        } else{
            text = text.toLowerCase();
            for(T item: itemsCopy){
                if(item.toString().toLowerCase().contains(text)){
                    items.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(int position, T item);
    }

    public interface OnAfterBindViewHolderListener<T> {
        void onAfterBindViewHolder(BindingHolder bindingHolder, T item);
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private final ViewDataBinding binding;

        BindingHolder(View v) {
            super(v);
            binding = DataBindingUtil.bind(v);
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }
}
