package com.example.returns.items;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide; // Glide 추가
import com.example.returns.MainActivity;
import com.example.returns.R;
import com.example.returns.DB.Item;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ItemDetailFragment extends BottomSheetDialogFragment {

    private Item item;
    private TextView badgeType, badgeCategory, badgeStatus;
    private TextView tvTitle, tvLocation, tvDate, tvFeatureContent, tvCommentHeader;
    private ImageView ivItemImage;
    private LinearLayout layoutCommentsList;
    private EditText etCommentInput;
    private ImageButton btnSendComment;
    private int commentCount = 0;

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
        // 바텀시트 테마 설정 (둥근 모서리 적용을 위해)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme);
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

        initViews(view);
        if (item != null) {
            displayItemData();
        }

        btnSendComment.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (!commentText.isEmpty()) {
                addCommentUI("익명1", commentText); // 실제 로그인 기능이 있다면 닉네임 변수 넣기
                etCommentInput.setText("");

                // 액티비티에 알림 팝업 요청
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).handleCommentAdded(
                            item.getId(),
                            item.getTitle(),
                            "익명1"
                    );
                }
            }
        });
    }

    private void initViews(View v) {
        badgeType = v.findViewById(R.id.badgeType);
        badgeCategory = v.findViewById(R.id.badgeCategory);
        badgeStatus = v.findViewById(R.id.badgeStatus);
        tvTitle = v.findViewById(R.id.tvTitle);
        tvLocation = v.findViewById(R.id.tvLocation);
        tvDate = v.findViewById(R.id.tvDate);
        tvFeatureContent = v.findViewById(R.id.tvFeatureContent);
        ivItemImage = v.findViewById(R.id.ivItemImage);

        tvCommentHeader = v.findViewById(R.id.tvCommentHeader);
        layoutCommentsList = v.findViewById(R.id.layoutCommentsList);
        etCommentInput = v.findViewById(R.id.etCommentInput);
        btnSendComment = v.findViewById(R.id.btnSendComment);

        v.findViewById(R.id.btnClose).setOnClickListener(view -> dismiss());
    }

    // 댓글 UI 추가 로직 수정
    private void addCommentUI(String name, String message) {
        // 인플레이트 시 부모 뷰(layoutCommentsList)를 지정하여 레이아웃 속성 유지
        View commentView = getLayoutInflater().inflate(R.layout.item_comment, layoutCommentsList, false);

        TextView tvName = commentView.findViewById(R.id.tvCommentName);
        TextView tvDate = commentView.findViewById(R.id.tvCommentDate);
        TextView tvMsg = commentView.findViewById(R.id.tvCommentMessage);

        tvName.setText(name);
        tvMsg.setText(message);
        tvDate.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date()));

        layoutCommentsList.addView(commentView);
        commentCount++;
        tvCommentHeader.setText("댓글 (" + commentCount + ")");
    }

    private void displayItemData() {
        // 1. 타입 설정 (분실/습득)
        if ("LOST".equalsIgnoreCase(item.getType())) {
            badgeType.setText("분실물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_lost);
        } else {
            badgeType.setText("습득물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_primary);
        }

        // 2. 기본 텍스트 정보
        badgeCategory.setText(item.getCategory());
        tvTitle.setText(item.getTitle());
        tvLocation.setText(item.getLocation());
        tvDate.setText(item.getDateOccurred());
        tvFeatureContent.setText(item.getNotes());

        // 3. 이미지 로딩
        Glide.with(this)
                .load(item.getImageUriString())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(ivItemImage);

        // 4. 상태 배지 설정
        String status = item.getStatus();
        badgeStatus.setText(status != null ? status : "미해결");

        if ("찾아감".equals(status) || "해결".equals(status)) {
            badgeStatus.setTextColor(Color.parseColor("#000000"));
            badgeStatus.getBackground().setTint(Color.parseColor("#FFFFFF"));
            badgeStatus.setBackgroundResource(R.drawable.bg_badge_outline);
        } else {
            badgeStatus.setTextColor(Color.parseColor("#999999"));
            badgeStatus.setBackgroundResource(R.drawable.bg_badge_outline);
        }
    }
}