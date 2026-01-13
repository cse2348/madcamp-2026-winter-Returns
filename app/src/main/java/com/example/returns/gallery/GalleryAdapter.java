package com.example.returns.gallery;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.returns.DB.Item;
import com.example.returns.R;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private List<Item> displayItems = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }
    private OnItemClickListener listener;
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    public void updateData(List<Item> newItems) {
        this.displayItems = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    public void filter(Context context,String query, String type, String category) {
        Log.e("GalleryAdapter", "filter called with query: " + query + ", type: " + type + ", category: " + category);
        Item.queryItems(query,type,category,new Item.ListItemCallback(){
            @Override
            public void onSuccess(List<Item> list) {
                displayItems=list;
                notifyDataSetChanged();
                Log.e("Galleryfilter", "Filtered list size: " + (list != null ? list.size() : 0));
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(context,"서버 연결에 실패했습니다.",Toast.LENGTH_SHORT).show();
                Log.e("GalleryAdapter", "서버와 연결 실패", e);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = displayItems.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvLocation.setText(item.getLocation());
        holder.tvCategoryBadge.setText(item.getCategory());
        holder.tvDate.setText(item.getDateOccurred());

        if ("FOUND".equalsIgnoreCase(item.getType())) {
            holder.tvTypeBadge.setText("습득");
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_found);
        } else {
            holder.tvTypeBadge.setText("분실");
            holder.tvTypeBadge.setBackgroundResource(R.drawable.bg_badge_lost);
        }

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUriString())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.itemImageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });

    }

    @Override
    public int getItemCount() { return displayItems.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView tvTitle, tvLocation, tvTypeBadge, tvCategoryBadge, tvDate;

        ViewHolder(View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvTypeBadge = itemView.findViewById(R.id.tvTypeBadge);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}