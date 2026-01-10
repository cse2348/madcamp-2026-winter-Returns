package com.example.returns;

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

public class MainActivity extends AppCompatActivity {

    private String userNickname;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.main);

        // 1. 시스템 바 패딩 설정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. 하단 내비게이션 뷰 초기화
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // 3. 탭 클릭 시 화면 전환 리스너 설정
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

        // 4. 처음 실행 시 홈 탭이 디폴트
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        // 유저 정보 처리
        userNickname = getIntent().getStringExtra("userNickname");
        ImageView btnUser = findViewById(R.id.btn_user);
        if (btnUser != null) {
            btnUser.setOnClickListener(v -> showUserModal(v));
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

        // RelativeLayout에 맞춘 레이아웃 설정
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.topMargin = 50;
        params.leftMargin = 30;
        params.rightMargin = 30;
        notiView.setLayoutParams(params);

        // 팝업이 헤더보다 위에 보이도록 설정
        notiView.setElevation(20f);

        rootLayout.addView(notiView);

        // 애니메이션 효과
        notiView.setAlpha(0f);
        notiView.animate().alpha(1f).setDuration(500).start();

        btnConfirm.setOnClickListener(v -> {
            notiView.animate().alpha(0f).setDuration(400).withEndAction(() -> {
                rootLayout.removeView(notiView);
            }).start();
        });
    }

    public void showItemDetail(Item item) {
        ItemDetailFragment detailFragment = ItemDetailFragment.newInstance(item);
        detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
    }

    private List<String> dummyNotification = Arrays.asList(
            "투명 비닐우산 분실",
            "파란색 지갑 발견",
            "아이폰 15 프로 분실"
    );

    private void showUserModal(View anchorView) {
        View modalView = getLayoutInflater().inflate(R.layout.layout_user_modal, null);

        TextView tvUserId = modalView.findViewById(R.id.tv_modal_user_id);
        if (tvUserId != null) {
            tvUserId.setText(userNickname);
        }

        // 알림을 담을 부모 레이아웃과 빈 상태 텍스트뷰 가져오기
        LinearLayout layoutNotification = modalView.findViewById(R.id.layout_user_modal);
        TextView tvNoNotification = modalView.findViewById(R.id.item_no_notification);

        // 2. 알림 리스트 처리 메커니즘
        if (dummyNotification == null || dummyNotification.isEmpty()) {
            tvNoNotification.setVisibility(View.VISIBLE);
        } else {
            tvNoNotification.setVisibility(View.GONE); // "알림 없음" 숨기기

            // 리스트를 돌면서 동적으로 항목 추가
            for (String title : dummyNotification) {
                View itemView = getLayoutInflater().inflate(R.id.layout_item_notification, null);

                TextView tvMessage = itemView.findViewById(R.id.tv_noti_message);
                String printingtitle;
                if(title.length()<20)printingtitle=title;
                else printingtitle=title.substring(0,17)+"...";
                // 이미지와 동일한 문구 구성
                tvMessage.setText("누군가가\n\"" + printingtitle + "\" 게시물에 댓글을 남겼습니다.");

                // 확인 버튼 클릭 이벤트
                itemView.findViewById(R.id.btn_noti_confirm).setOnClickListener(v -> {
                    //Toast.makeText(this, title + " 확인 완료", Toast.LENGTH_SHORT).show();
                });

                layoutNotification.addView(itemView); // 실제 레이아웃에 추가

                // 항목 간 구분선 추가 (마지막 항목 제외)
                if (dummyNotification.indexOf(title) != dummyNotification.size() - 1) {
                    View divider = new View(this);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                    divider.setBackgroundColor(Color.parseColor("#F1F5F9"));
                    layoutNotification.addView(divider);
                }
            }
        }

        // PopupWindow 생성 및 표시
        PopupWindow popupWindow = new PopupWindow(modalView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true);
        popupWindow.setElevation(10f);
        popupWindow.showAsDropDown(anchorView, 0, 10);
    }
}