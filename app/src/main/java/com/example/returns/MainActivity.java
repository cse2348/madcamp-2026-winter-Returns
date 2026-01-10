package com.example.returns;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;


import com.example.returns.add.AddFoundFragment;
import com.example.returns.add.AddLostFragment;
import com.example.returns.gallery.GalleryFragment;
import com.example.returns.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private String userNickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        userNickname=getIntent().getStringExtra("userNickname");
        ImageView btnUser = findViewById(R.id.btn_user);
        btnUser.setOnClickListener(v -> showUserModal(v));
    }
    private void showUserModal(View anchorView) {
        // 1. 모달 레이아웃 인플레이트
        View modalView = getLayoutInflater().inflate(R.layout.layout_user_modal, null);

        // 2. 데이터 설정 (예: SharedPreferences나 Intent에서 가져온 아이디)
        TextView tvUserId = modalView.findViewById(R.id.tv_modal_user_id);
        tvUserId.setText(userNickname); // 실제 사용자 ID로 교체하세요

        // 3. PopupWindow 생성 (너비, 높이는 콘텐츠에 맞게)
        PopupWindow popupWindow = new PopupWindow(modalView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true); // true는 바깥쪽 클릭 시 닫힘 설정

        // 4. 애니메이션 추가 (원하는 경우)
        popupWindow.setElevation(10f);

        // 5. 위치 설정 (btn_user 버튼 아래에 살짝 간격을 두고 표시)
        popupWindow.showAsDropDown(anchorView, 0, 10);
    }

    // onCreate 안에서 호출

}