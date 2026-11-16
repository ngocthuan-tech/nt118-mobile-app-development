package com.aicoach.ui.challenge;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Binds immutable data snapshots into the existing module layouts. */
public final class SocialRows<T> extends ListAdapter<SocialRows.Row<T>, SocialRows.Holder> {
    public interface Binder<T> { void bind(View view, T item, int position); }
    private final int layout;
    private final Binder<T> binder;
    public SocialRows(int layout, Function<T, String> key, Binder<T> binder) {
        super(new DiffUtil.ItemCallback<Row<T>>() {
            @Override public boolean areItemsTheSame(@NonNull Row<T> oldItem, @NonNull Row<T> newItem) {
                return key.apply(oldItem.item).equals(key.apply(newItem.item));
            }
            @Override public boolean areContentsTheSame(@NonNull Row<T> oldItem, @NonNull Row<T> newItem) {
                return oldItem.position == newItem.position && Objects.equals(oldItem.item, newItem.item);
            }
        });
        this.layout = layout; this.binder = binder;
    }
    public void submit(List<T> items) {
        List<Row<T>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) rows.add(new Row<>(items.get(i), i));
        submitList(rows);
    }
    public static final class Row<T> {
        final T item;
        final int position;
        Row(T item, int position) { this.item = item; this.position = position; }
    }
    @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
    }
    @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
        binder.bind(holder.itemView, getItem(position).item, position);
    }
    public static final class Holder extends RecyclerView.ViewHolder {
        Holder(View view) { super(view); }
    }
}
