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

import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.DB.Notification;
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

public class MainActivity extends AppCompatActivity {

    private String userNickname;
    private RelativeLayout rootLayout;
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
            if (itemId == R.id.nav_home) selectedFragment = new HomeFragment();
            else if (itemId == R.id.nav_gallery) selectedFragment = new GalleryFragment();
            else if (itemId == R.id.nav_found) selectedFragment = new AddFoundFragment();
            else if (itemId == R.id.nav_lost) selectedFragment = new AddLostFragment();

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFragment).commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) bottomNav.setSelectedItemId(R.id.nav_home);

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

    public void handleCommentAdded(int itemId, String itemTitle, String commenterName, String authorName) {

        if (authorName != null && authorName.equals(commenterName)) {
            return;
        }

        SharedPreferences pref = getSharedPreferences("UserToken", MODE_PRIVATE);
        String myNickname = pref.getString("nickName", "익명");
        String currentTime = new SimpleDateFormat("yyyy. M. d. a h:mm:ss", Locale.KOREA).format(new Date());

        new Thread(() -> {
            Notification newNoti = new Notification();
            newNoti.receiverNickname = authorName;
            newNoti.title = itemTitle;
            newNoti.commenterName = commenterName;
            newNoti.timestamp = currentTime;

            AppDatabase.getInstance(this).notificationDao().insert(newNoti);

            if (authorName != null && authorName.equals(myNickname) && !commenterName.equals(myNickname)) {
                runOnUiThread(() -> showRealtimePopup(itemTitle, commenterName, currentTime));
            }
        }).start();
    }

    private void showRealtimePopup(String title, String commenter, String time) {
        LayoutInflater inflater = getLayoutInflater();
        View notiView = inflater.inflate(R.layout.layout_notification_popup, rootLayout, false);

        ((TextView) notiView.findViewById(R.id.tvNotiMessage)).setText(String.format("\"%s\"에 댓글이 달렸습니다.", title));
        ((TextView) notiView.findViewById(R.id.tvNotiTime)).setText(time);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = 50; params.leftMargin = 30; params.rightMargin = 30;
        notiView.setLayoutParams(params);
        notiView.setElevation(20f);
        rootLayout.addView(notiView);

        notiView.setAlpha(0f);
        notiView.animate().alpha(1f).setDuration(500).start();

        notiView.findViewById(R.id.btnNotiConfirm).setOnClickListener(v -> {
            notiView.animate().alpha(0f).setDuration(400).withEndAction(() -> rootLayout.removeView(notiView)).start();
        });
    }

    private void showUserModal(View anchorView) {
        View modalView = getLayoutInflater().inflate(R.layout.layout_user_modal, null);
        TextView tvUserId = modalView.findViewById(R.id.tv_modal_user_id);
        if (tvUserId != null) tvUserId.setText(userNickname);

        loadNotificationList(modalView);

        popupWindow_noti.setContentView(modalView);
        popupWindow_noti.showAsDropDown(anchorView, 0, 10);
    }

    private void loadNotificationList(View modalView) {
        LinearLayout container = modalView.findViewById(R.id.noti_container);
        View layoutNoNoti = modalView.findViewById(R.id.layout_no_notification);

        if (container == null || layoutNoNoti == null) return;

        SharedPreferences pref = getSharedPreferences("UserToken", MODE_PRIVATE);
        String myNickname = pref.getString("nickName", "익명");

        new Thread(() -> {
            List<Notification> notiList = AppDatabase.getInstance(this).notificationDao().getNotificationsForUser(myNickname);

            runOnUiThread(() -> {
                container.removeAllViews();
                container.addView(layoutNoNoti);

                if (notiList == null || notiList.isEmpty()) {
                    layoutNoNoti.setVisibility(View.VISIBLE);
                } else {
                    layoutNoNoti.setVisibility(View.GONE);

                    for (int i = 0; i < notiList.size(); i++) {
                        final Notification noti = notiList.get(i);
                        View itemView = getLayoutInflater().inflate(R.layout.layout_item_notification, container, false);

                        ((TextView) itemView.findViewById(R.id.tv_noti_message)).setText(noti.title + " 게시물에 댓글이 달렸습니다.");
                        ((TextView) itemView.findViewById(R.id.tv_noti_time)).setText(noti.timestamp);

                        itemView.findViewById(R.id.btn_noti_confirm).setOnClickListener(v -> {
                            new Thread(() -> {
                                AppDatabase.getInstance(this).notificationDao().delete(noti);
                                runOnUiThread(() -> loadNotificationList(modalView));
                            }).start();
                        });

                        container.addView(itemView);

                        if (i < notiList.size() - 1) {
                            View divider = new View(this);
                            divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                            divider.setBackgroundColor(Color.parseColor("#F1F5F9"));
                            container.addView(divider);
                        }
                    }
                }
            });
        }).start();
    }

    public void showItemDetail(Item item) {
        ItemDetailFragment detailFragment = ItemDetailFragment.newInstance(item);
        detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
    }

    public void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (currentFragment instanceof HomeFragment) ((HomeFragment) currentFragment).loadData();
        else if (currentFragment instanceof GalleryFragment) ((GalleryFragment) currentFragment).loadData();
    }
}