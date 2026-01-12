package com.example.returns.items;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.returns.MainActivity;
import com.example.returns.R;
import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.DB.Comment;
import com.example.returns.add.AddFoundFragment;
import com.example.returns.add.AddLostFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ItemDetailFragment extends BottomSheetDialogFragment {

    private Item item;
    private TextView badgeType, badgeCategory, badgeStatus;
    private TextView tvTitle, tvLocation, tvDate, tvFeatureContent, tvCommentHeader;
    private LinearLayout layoutFoundSpecific;
    private TextView tvHandledByContent, tvContactContent;

    private ImageView ivItemImage;
    private LinearLayout layoutCommentsList, layoutOwnerButtons;
    private Button btnEdit, btnDelete;
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
            checkOwnership();
            loadCommentsFromDB();
        }

        // 댓글 전송 로직
        btnSendComment.setOnClickListener(v -> {
            SharedPreferences pref = requireContext().getSharedPreferences("UserToken", Context.MODE_PRIVATE);
            String myNickname = pref.getString("nickName", "익명");
            String commentText = etCommentInput.getText().toString().trim();

            if (!commentText.isEmpty()) {
                String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(new Date());

                // 1. DB에 저장할 댓글 객체 생성
                Comment newComment = new Comment();
                newComment.itemId = item.getId();
                newComment.authorName = myNickname;
                newComment.message = commentText;
                newComment.timestamp = currentTime;

                // 2. DB 저장 (비동기 처리)
                new Thread(() -> {
                    AppDatabase.getInstance(requireContext()).commentDao().insert(newComment);

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // UI 업데이트
                            addCommentUI(myNickname, commentText, currentTime);
                            etCommentInput.setText("");

                            // 알림 로직
                            if (getActivity() instanceof MainActivity) {
                                if (item.getAuthorNickname() != null && item.getAuthorNickname().equals(myNickname)) {
                                    ((MainActivity) getActivity()).handleCommentAdded(item.getId(), item.getTitle(), myNickname);
                                }
                            }
                        });
                    }
                }).start();
            }
        });

        // 삭제 로직
        btnDelete.setOnClickListener(v -> {
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.RoundedCornersDialog)
                    .setTitle("삭제 확인")
                    .setMessage("'" + item.getTitle() + "' 게시글을 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        new Thread(() -> {
                            AppDatabase.getInstance(requireContext()).itemDao().delete(item);
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    if (getActivity() instanceof MainActivity) {
                                        ((MainActivity) getActivity()).refreshCurrentFragment();
                                    }
                                    dismiss();
                                });
                            }
                        }).start();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        btnEdit.setOnClickListener(v -> {
            Fragment editFragment;

            if ("FOUND".equalsIgnoreCase(item.getType())) {
                editFragment = AddFoundFragment.newInstance(item);
            } else {
                editFragment = AddLostFragment.newInstance(item);
            }

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, editFragment)
                        .addToBackStack(null)
                        .commit();

                dismiss();
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

        layoutFoundSpecific = v.findViewById(R.id.layoutFoundSpecific);
        tvHandledByContent = v.findViewById(R.id.tvHandledByContent);
        tvContactContent = v.findViewById(R.id.tvContactContent);

        ivItemImage = v.findViewById(R.id.ivItemImage);
        tvCommentHeader = v.findViewById(R.id.tvCommentHeader);
        layoutCommentsList = v.findViewById(R.id.layoutCommentsList);
        etCommentInput = v.findViewById(R.id.etCommentInput);
        btnSendComment = v.findViewById(R.id.btnSendComment);
        layoutOwnerButtons = v.findViewById(R.id.layoutOwnerButtons);
        btnEdit = v.findViewById(R.id.btnEdit);
        btnDelete = v.findViewById(R.id.btnDelete);
        v.findViewById(R.id.btnClose).setOnClickListener(view -> dismiss());
    }

    private void checkOwnership() {
        SharedPreferences pref = requireContext().getSharedPreferences("UserToken", Context.MODE_PRIVATE);
        String currentLoginUser = pref.getString("nickName", "");
        if (item.getAuthorNickname() != null && item.getAuthorNickname().equals(currentLoginUser)) {
            layoutOwnerButtons.setVisibility(View.VISIBLE);
        } else {
            layoutOwnerButtons.setVisibility(View.GONE);
        }
    }

    // DB에서 댓글 목록을 가져오는 메서드
    private void loadCommentsFromDB() {
        new Thread(() -> {
            List<Comment> comments = AppDatabase.getInstance(requireContext()).commentDao().getCommentsForItem(item.getId());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    layoutCommentsList.removeAllViews();
                    commentCount = 0;
                    for (Comment c : comments) {
                        addCommentUI(c.authorName, c.message, c.timestamp);
                    }
                    tvCommentHeader.setText("댓글 (" + commentCount + ")");
                });
            }
        }).start();
    }

    // UI에 댓글 뷰를 추가하는 메서드
    private void addCommentUI(String name, String message, String date) {
        View commentView = getLayoutInflater().inflate(R.layout.item_comment, layoutCommentsList, false);
        ((TextView) commentView.findViewById(R.id.tvCommentName)).setText(name);
        ((TextView) commentView.findViewById(R.id.tvCommentMessage)).setText(message);
        ((TextView) commentView.findViewById(R.id.tvCommentDate)).setText(date);
        layoutCommentsList.addView(commentView);
        commentCount++;
        tvCommentHeader.setText("댓글 (" + commentCount + ")");
    }

    private void displayItemData() {
        if ("LOST".equalsIgnoreCase(item.getType())) {
            badgeType.setText("분실물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_lost);
            if (layoutFoundSpecific != null) layoutFoundSpecific.setVisibility(View.GONE);

        } else {
            badgeType.setText("습득물");
            badgeType.setBackgroundResource(R.drawable.bg_badge_primary);

            if (layoutFoundSpecific != null) {
                layoutFoundSpecific.setVisibility(View.VISIBLE);
                tvHandledByContent.setText(item.getHandledBy());
                tvContactContent.setText(item.getContactName());
            }
        }

        badgeCategory.setText(item.getCategory());
        tvTitle.setText(item.getTitle());
        tvLocation.setText(item.getLocation());
        tvDate.setText(item.getDateOccurred());
        tvFeatureContent.setText(item.getNotes());

        Glide.with(this)
                .load(item.getImageUriString())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(ivItemImage);

        String status = item.getStatus();
        badgeStatus.setText(status != null ? status : "미발견");
    }
}