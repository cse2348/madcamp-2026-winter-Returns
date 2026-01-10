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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.returns.R;
import com.example.returns.DB.Item;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ItemDetailFragment extends BottomSheetDialogFragment {

    private Item item; // 전달받은 아이템 데이터

    // UI 컴포넌트
    private TextView badgeType, badgeCategory, badgeStatus;
    private TextView tvTitle, tvLocation, tvDate, tvPhone, tvOwnerName, tvFeatureContent;
    private ImageView ivItemImage;
    private Button btnClaim;
    private View layoutDetailContent, layoutClaimForm;

    // 1. 프래그먼트 생성 시 데이터를 전달받기 위한 static 메서드
    public static ItemDetailFragment newInstance(Item item) {
        ItemDetailFragment fragment = new ItemDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("item_data", item); // Item 클래스가 Serializable이므로 가능
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
        View v = inflater.inflate(R.layout.fragment_item_detail, container, false);

        initViews(v);
        if (item != null) {
            displayItemData();
        }

        return v;
    }

    private void initViews(View v) {
        badgeType = v.findViewById(R.id.badgeType);
        badgeCategory = v.findViewById(R.id.badgeCategory);
        badgeStatus = v.findViewById(R.id.badgeStatus);

        tvTitle = v.findViewById(R.id.tvTitle);
        tvLocation = v.findViewById(R.id.tvLocation);
        tvDate = v.findViewById(R.id.tvDate);
        tvPhone = v.findViewById(R.id.tvPhone);
        tvOwnerName = v.findViewById(R.id.tvOwnerName);
        tvFeatureContent = v.findViewById(R.id.tvFeatureContent);

        ivItemImage = v.findViewById(R.id.ivItemImage);
        btnClaim = v.findViewById(R.id.btnClaim);

        layoutDetailContent = v.findViewById(R.id.layoutDetailContent);
        layoutClaimForm = v.findViewById(R.id.layoutClaimForm);

        v.findViewById(R.id.btnClose).setOnClickListener(view -> dismiss());
    }

    private void displayItemData() {
        // 1. 타입 설정
        if ("LOST".equals(item.getType())) {
            badgeType.setText("분실물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_lost);
        } else {
            badgeType.setText("습득물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_primary);
        }

        // 2. 카테고리 및 제목
        badgeCategory.setText(item.getCategory());
        tvTitle.setText(item.getTitle());

        // 3. 상세 정보 박스
        tvLocation.setText(item.getLocation());
        tvDate.setText(item.getDateOccurred());
        tvPhone.setText(item.getContactPhone());
        tvOwnerName.setText(item.getContactName());

        // 4. 특징
        tvFeatureContent.setText(item.getNotes());

        // 5. 이미지 설정
        if (item.getImageUriString() != null && !item.getImageUriString().isEmpty()) {
            ivItemImage.setImageURI(Uri.parse(item.getImageUriString()));
        }

        // 6. 상태 설정 (미해결/찾아감) 및 버튼 처리
        String status = item.getStatus();
        badgeStatus.setText(status);

        if ("찾아감".equals(status)) {
            badgeStatus.setTextColor(Color.parseColor("#000000")); // 초록색 강조
            btnClaim.setVisibility(View.GONE);
        } else {
            badgeStatus.setTextColor(Color.parseColor("#999999")); // 미해결은 회색
            btnClaim.setVisibility(View.VISIBLE);
        }


    }
}