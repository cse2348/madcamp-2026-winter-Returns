package com.example.returns;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.returns.DB.Item;
import com.example.returns.add.AddFoundFragment;
import com.example.returns.add.AddLostFragment;
import com.example.returns.gallery.GalleryFragment;
import com.example.returns.home.HomeFragment;
import com.example.returns.items.ItemDetailFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String userNickname;
    private RelativeLayout rootLayout;

    private List<String> pendingNotification = new ArrayList<>();
    PopupWindow popupWindow_noti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userNickname = getIntent().getStringExtra("userNickname");
        if (userNickname != null) {
            SharedPreferences pref = getSharedPreferences("UserToken", MODE_PRIVATE);
            pref.edit().putString("nickName", userNickname).apply();
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_gallery) {
                selectedFragment = new GalleryFragment();
            } else if (itemId == R.id.nav_found) {
                selectedFragment = new AddFoundFragment();
            } else if (itemId == R.id.nav_lost) {
                selectedFragment = new AddLostFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        popupWindow_noti = new PopupWindow(null, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow_noti.setElevation(10f);

        ImageView btnUser = findViewById(R.id.btn_user);
        if (btnUser != null) {
            btnUser.setOnClickListener(v -> {
                if (popupWindow_noti.isShowing()) popupWindow_noti.dismiss();
                else showUserModal(v);
            });
        }
    }

    public void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).loadData();
        } else if (currentFragment instanceof GalleryFragment) {
            ((GalleryFragment) currentFragment).loadData();
        }
    }

    // 알림 처리 메서드 수정
    public void handleCommentAdded(int itemId, String itemTitle, String commenterName) {
        SharedPreferences pref = getSharedPreferences("UserToken", MODE_PRIVATE);
        String myNickname = pref.getString("nickName", "익명");

        // 내가 내 게시물에 댓글을 달았을 때는 알림 팝업을 띄우지 않음
        if (commenterName.equals(myNickname)) {
            return;
        }

        LayoutInflater inflater = getLayoutInflater();
        View notiView = inflater.inflate(R.layout.layout_notification_popup, rootLayout, false);
        TextView tvMessage = notiView.findViewById(R.id.tvNotiMessage);
        TextView tvTime = notiView.findViewById(R.id.tvNotiTime);
        TextView btnConfirm = notiView.findViewById(R.id.btnNotiConfirm);

        tvMessage.setText(String.format("%s님이 \"%s\" 게시물에 댓글을 남겼습니다.", commenterName, itemTitle));
        tvTime.setText(new SimpleDateFormat("yyyy. M. d. a h:mm:ss", Locale.KOREA).format(new Date()));

        // 알림 목록에 추가
        pendingNotification.add(itemTitle);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = 50; params.leftMargin = 30; params.rightMargin = 30;
        notiView.setLayoutParams(params);
        notiView.setElevation(20f);
        rootLayout.addView(notiView);

        // 애니메이션
        notiView.setAlpha(0f);
        notiView.animate().alpha(1f).setDuration(500).start();

        btnConfirm.setOnClickListener(v -> {
            notiView.animate().alpha(0f).setDuration(400).withEndAction(() -> rootLayout.removeView(notiView)).start();
        });
    }

    public void showItemDetail(Item item) {
        ItemDetailFragment detailFragment = ItemDetailFragment.newInstance(item);
        detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
    }

    // 사용자 알림 모달 수정
    private void showUserModal(View anchorView) {
        View modalView = getLayoutInflater().inflate(R.layout.layout_user_modal, null);
        TextView tvUserId = modalView.findViewById(R.id.tv_modal_user_id);
        if (tvUserId != null) tvUserId.setText(userNickname);

        LinearLayout layoutNotificationContainer = modalView.findViewById(R.id.layout_user_modal);
        LinearLayout layoutNoNotification = modalView.findViewById(R.id.layout_no_notification);

        if (pendingNotification == null || pendingNotification.isEmpty()) {
            layoutNoNotification.setVisibility(View.VISIBLE);
        } else {
            layoutNoNotification.setVisibility(View.GONE);
            layoutNotificationContainer.removeAllViews();

            for (int i = 0; i < pendingNotification.size(); i++) {
                final String title = pendingNotification.get(i);
                View itemView = getLayoutInflater().inflate(R.layout.layout_item_notification, null);
                TextView tvMessage = itemView.findViewById(R.id.tv_noti_message);

                String printing_title = title.length() < 20 ? title : title.substring(0, 17) + "...";
                tvMessage.setText("누군가가\n\"" + printing_title + "\" 게시물에 댓글을 남겼습니다.");

                layoutNotificationContainer.addView(itemView);

                if (i < pendingNotification.size() - 1) {
                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                    divider.setBackgroundColor(Color.parseColor("#F1F5F9"));
                    layoutNotificationContainer.addView(divider);
                }

                itemView.findViewById(R.id.btn_noti_confirm).setOnClickListener(v -> {
                    pendingNotification.remove(title);
                    showUserModal(anchorView);
                });
            }
        }

        popupWindow_noti.setContentView(modalView);
        popupWindow_noti.showAsDropDown(anchorView, 0, 10);
    }
}