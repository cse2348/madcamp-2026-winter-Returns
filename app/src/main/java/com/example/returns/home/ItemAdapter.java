package com.example.returns.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.returns.R;
import com.example.returns.DB.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private List<Item> itemList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Item item);
    }

    public ItemAdapter(List<Item> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = itemList.get(position);

        // 1. 텍스트 데이터 바인딩
        holder.txtItemTitle.setText(item.getTitle());
        holder.txtItemLocation.setText(item.getLocation());
        holder.txtCategoryBadge.setText(item.getCategory());

        // 2. 타입 배지 설정 (습득/분실)
        if ("FOUND".equals(item.getType())) {
            holder.txtTypeBadge.setText("습득");
            holder.txtTypeBadge.getBackground().setTint(Color.parseColor("#1E3A8A"));
        } else {
            holder.txtTypeBadge.setText("분실");
            holder.txtTypeBadge.getBackground().setTint(Color.parseColor("#EF4444"));
        }

        // 3. 상태 배지 설정 (미해결, 보관중 / 찾아감, 해결)
        String status = item.getStatus();
        holder.txtStatusBadge.setText(status);

        if ("찾아감".equals(status) || "해결".equals(status)) {
            holder.txtStatusBadge.getBackground().setTint(Color.parseColor("#F3F4F6"));
            holder.txtStatusBadge.setTextColor(Color.parseColor("#6B7280"));
        } else {
            holder.txtStatusBadge.getBackground().setTint(Color.parseColor("#E5E7EB"));
            holder.txtStatusBadge.setTextColor(Color.parseColor("#6B7280"));
        }

        // 4. 이미지 로딩 로직
        Glide.with(holder.itemView.getContext())
                .load(item.getImageUriString())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(holder.imgItem);

        // 5. 아이템 클릭 리스너 연결
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgItem;
        TextView txtTypeBadge, txtCategoryBadge, txtItemTitle, txtItemLocation, txtStatusBadge;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgItem = itemView.findViewById(R.id.img_item);
            txtTypeBadge = itemView.findViewById(R.id.txt_type_badge);
            txtCategoryBadge = itemView.findViewById(R.id.txt_category_badge);
            txtItemTitle = itemView.findViewById(R.id.txt_item_title);
            txtItemLocation = itemView.findViewById(R.id.txt_item_location);
            txtStatusBadge = itemView.findViewById(R.id.txt_status_badge);
        }
    }
}