package com.example.returns;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.DB.Notification;
import com.example.returns.DB.User;
import com.example.returns.add.AddFoundFragment;
import com.example.returns.add.AddLostFragment;
import com.example.returns.gallery.GalleryFragment;
import com.example.returns.home.HomeFragment;
import com.example.returns.items.ItemDetailFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.List;

public class MainActivity extends AppCompatActivity {

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


    //리얼타임팝업 안할꺼니깐...
    /*
    private void ReadyNotification()
    {
        FirebaseFirestore db = AppDatabase.getDb();

        SharedPreferences pref = getSharedPreferences("UserToken", Context.MODE_PRIVATE);
        String myNickname = pref.getString("nickName", "익명");
        DocumentReference myRef = User.getReferenceByName(myNickname);

        db.collection("Notifications")
                .whereEqualTo("Receiver", myRef)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> { // 실시간 업데이트를 위해 SnapshotListener 사용
                    if (error != null) {
                        Log.e("Firestore", "알림 로드 실패", error);
                        //여기에서 서버 연결이 끊겼다고 해도 되긴합니다
                    }

                    if (value != null && !isFinishing() && !isDestroyed() ) {
                        //List<Notification> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            Notification n = doc.toObject(Notification.class);
                            if (n != null) {
                                //list.add(n);
                                n.Id=doc.getId();
                                String formattedTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(n.timestamp.toDate());
                                runOnUiThread(() -> showRealtimePopup(n.Title, n.Author, formattedTime));
                            }
                        }

                    }
                });
    }*/

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
        SharedPreferences pref = getSharedPreferences("UserToken", Context.MODE_PRIVATE);
        String myNickname = pref.getString("nickName", "익명");
        if (tvUserId != null) tvUserId.setText(myNickname);

        loadNotificationList(modalView);

        popupWindow_noti.setContentView(modalView);
        popupWindow_noti.showAsDropDown(anchorView, 0, 10);
    }

    private void loadNotificationList(View modalView) {
        LinearLayout container = modalView.findViewById(R.id.noti_container);
        View layoutNoNoti = modalView.findViewById(R.id.layout_no_notification);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy. M. d. a h:mm:ss", Locale.KOREA);

        if (container == null || layoutNoNoti == null) return;

        SharedPreferences pref = getSharedPreferences("UserToken", MODE_PRIVATE);
        String myNickname = pref.getString("nickName", "익명");

        Notification.getUnreadNotifications(myNickname, new Notification.ListNotificationCallback() {
            @Override
            public void onSuccess(List<Notification> list) {
                Log.d("Notification","도착한 noti :" + list.size());
                if(isFinishing()||isDestroyed()) return;
                container.removeAllViews();
                container.addView(layoutNoNoti);
                if(list==null||list.isEmpty()) {
                    layoutNoNoti.setVisibility(View.VISIBLE);
                    return;
                }
                else {
                    layoutNoNoti.setVisibility(View.GONE);

                    for (int i = 0; i < list.size(); i++) {
                        final Notification noti = list.get(i);
                        View itemView = getLayoutInflater().inflate(R.layout.layout_item_notification, container, false);

                        ((TextView) itemView.findViewById(R.id.tv_noti_message)).setText(noti.Title + " 게시물에 댓글이 달렸습니다.");

                        String formattedTime = sdf.format(noti.Timestamp.toDate());
                        ((TextView) itemView.findViewById(R.id.tv_noti_time)).setText(formattedTime);

                        final View divider = new View(MainActivity.this);


                        itemView.findViewById(R.id.btn_noti_confirm).setOnClickListener(v -> {
                            container.removeView(itemView);//낙관적으로 일단 삭제, 응답 개선
                            container.removeView(divider);
                            if(container.getChildCount()<=1){
                                layoutNoNoti.setVisibility(View.VISIBLE);
                            }
                            noti.deleteNotification(new Notification.Callback() {
                                @Override
                                public void onSuccess() {}

                                @Override
                                public void onError(Exception e) {
                                    if(!isFinishing() && !isDestroyed()){
                                        Toast.makeText(MainActivity.this,"서버 연결에 실패했습니다.",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        });
                        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                        divider.setBackgroundColor(Color.parseColor("#F1F5F9"));
                        container.addView(divider);
                        container.addView(itemView);
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                if(!isFinishing() && !isDestroyed()){
                    Toast.makeText(MainActivity.this,"알림 수신에 실패했습니다.",Toast.LENGTH_SHORT).show();
                    Log.e("MainActivity","loadNotificationlist",e);
                }
            }
        });
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