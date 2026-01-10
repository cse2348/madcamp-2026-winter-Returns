package com.example.returns.items;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.returns.R;
import com.example.returns.DB.Item;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ItemDetailFragment extends BottomSheetDialogFragment {

    private Item item;

    private TextView badgeType, badgeCategory, badgeStatus;
    private TextView tvTitle, tvLocation, tvDate, tvPhone, tvFeatureContent;
    private ImageView ivItemImage;
    private Button btnClaim;
    private View layoutDetailContent, layoutClaimForm;

    public static ItemDetailFragment newInstance(Item item) {
        ItemDetailFragment fragment = new ItemDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("item_data", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (Item) getArguments().getSerializable("item_data");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews(view);
        if (item != null) {
            displayItemData();
        }
    }

    private void initViews(View v) {
        badgeType = v.findViewById(R.id.badgeType);
        badgeCategory = v.findViewById(R.id.badgeCategory);
        badgeStatus = v.findViewById(R.id.badgeStatus);
        tvTitle = v.findViewById(R.id.tvTitle);
        tvLocation = v.findViewById(R.id.tvLocation);
        tvDate = v.findViewById(R.id.tvDate);
        tvPhone = v.findViewById(R.id.tvPhone);
        tvFeatureContent = v.findViewById(R.id.tvFeatureContent);
        ivItemImage = v.findViewById(R.id.ivItemImage);
        btnClaim = v.findViewById(R.id.btnClaim);
        layoutDetailContent = v.findViewById(R.id.layoutDetailContent);
        layoutClaimForm = v.findViewById(R.id.layoutClaimForm);

        v.findViewById(R.id.btnClose).setOnClickListener(view -> dismiss());
    }

    private void displayItemData() {
        if ("LOST".equals(item.getType())) {
            badgeType.setText("분실물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_lost);
        } else {
            badgeType.setText("습득물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_primary);
        }

        badgeCategory.setText(item.getCategory());
        tvTitle.setText(item.getTitle());
        tvLocation.setText(item.getLocation());
        tvDate.setText(item.getDateOccurred());
        tvPhone.setText(item.getContactPhone());
        tvFeatureContent.setText(item.getNotes());

        if (item.getImageUriString() != null && !item.getImageUriString().isEmpty()) {
            ivItemImage.setImageURI(Uri.parse(item.getImageUriString()));
        }

        String status = item.getStatus();
        badgeStatus.setText(status);

        if ("찾아감".equals(status)) {
            // 찾아감: 검정 글씨 + 흰색 배경
            badgeStatus.setTextColor(Color.parseColor("#000000"));
            badgeStatus.getBackground().setTint(Color.parseColor("#FFFFFF"));
            btnClaim.setVisibility(View.GONE);
        } else {
            // 미해결: 회색 글씨
            badgeStatus.setTextColor(Color.parseColor("#999999"));
            badgeStatus.getBackground().setTintList(null);
            btnClaim.setVisibility(View.VISIBLE);
        }
    }
}