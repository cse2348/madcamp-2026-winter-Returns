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
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String userNickname;
    private RelativeLayout rootLayout;

    private List<String> pendingNotification = new ArrayList<>(Arrays.asList());//아직 확인 안 한 알림 목록
    PopupWindow popupWindow_noti; //알림창

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

        // 1. 유저 정보 처리 및 데이터 저장 (수정됨)
        userNickname = getIntent().getStringExtra("userNickname");
        if (userNickname != null) {
            // 상세 페이지(Fragment)에서 꺼내 쓸 수 있도록 저장소에 저장합니다.
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

        popupWindow_noti=new PopupWindow(null, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,true);
        popupWindow_noti.setElevation(10f);

        ImageView btnUser = findViewById(R.id.btn_user);
        if (btnUser != null) {
            btnUser.setOnClickListener(v -> {
                if(popupWindow_noti.isShowing())popupWindow_noti.dismiss();
                else showUserModal(v);
            });
        }
    }

    // ★ 메인 화면 즉시 새로고침 메서드
    public void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).loadData();
        } else if (currentFragment instanceof GalleryFragment) {
            ((GalleryFragment) currentFragment).loadData();
        }
    }

    public void handleCommentAdded(int itemId, String itemTitle, String commenterName) {
        LayoutInflater inflater = getLayoutInflater();
        View notiView = inflater.inflate(R.layout.layout_notification_popup, rootLayout, false);
        TextView tvMessage = notiView.findViewById(R.id.tvNotiMessage);
        TextView tvTime = notiView.findViewById(R.id.tvNotiTime);
        TextView btnConfirm = notiView.findViewById(R.id.btnNotiConfirm);

        tvMessage.setText(String.format("%s님이 \"%s\" 게시물에 댓글을 남겼습니다.", commenterName, itemTitle));
        tvTime.setText(new SimpleDateFormat("yyyy. M. d. a h:mm:ss", Locale.KOREA).format(new Date()));

        pendingNotification.add(itemTitle);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = 50; params.leftMargin = 30; params.rightMargin = 30;
        notiView.setLayoutParams(params);
        notiView.setElevation(20f);
        rootLayout.addView(notiView);
        if(popupWindow_noti.isShowing())popupWindow_noti.dismiss();

        // 애니메이션 효과
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

    private void showUserModal(View anchorView) {
        View modalView = getLayoutInflater().inflate(R.layout.layout_user_modal, null);
        TextView tvUserId = modalView.findViewById(R.id.tv_modal_user_id);
        if (tvUserId != null) tvUserId.setText(userNickname);

        LinearLayout layoutNotification = modalView.findViewById(R.id.layout_user_modal);
        LinearLayout tvNoNotification = modalView.findViewById(R.id.layout_no_notification);

        if (pendingNotification == null || pendingNotification.isEmpty()) {
            tvNoNotification.setVisibility(View.VISIBLE);
        } else {
            tvNoNotification.setVisibility(View.GONE);
            for (String title : pendingNotification) {
                View itemView = getLayoutInflater().inflate(R.layout.layout_item_notification, null);
                TextView tvMessage = itemView.findViewById(R.id.tv_noti_message);
                String printing_title = title.length() < 20 ? title : title.substring(0, 17) + "...";
                tvMessage.setText("누군가가\n\"" + printing_title + "\" 게시물에 댓글을 남겼습니다.");
                layoutNotification.addView(itemView);

                View divider;
                if (pendingNotification.indexOf(title) != pendingNotification.size() - 1) {
                    divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                    divider.setBackgroundColor(Color.parseColor("#F1F5F9"));
                    layoutNotification.addView(divider);
                } else {
                    divider = null;
                }

                Runnable erase_noti_elem = () -> {
                    layoutNotification.removeView(itemView);
                    if (divider != null) layoutNotification.removeView(divider);
                    pendingNotification.remove(title);
                    if (pendingNotification.isEmpty()) {
                        tvNoNotification.setVisibility(View.VISIBLE);
                    }
                };

                itemView.findViewById(R.id.btn_noti_confirm).setOnClickListener(v -> {erase_noti_elem.run();});
                itemView.findViewById(R.id.layout_item_notification).setOnClickListener(v -> {
                    pendingNotification.remove(title);
                });

            }
        }

        popupWindow_noti.setContentView(modalView);
        popupWindow_noti.showAsDropDown(anchorView, 0, 10);
    }

}