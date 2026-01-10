package com.example.returns;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.returns.DB.Item;
import com.example.returns.items.ItemDetailFragment;
import com.example.returns.add.AddFoundFragment;
import com.example.returns.add.AddLostFragment;
import com.example.returns.gallery.GalleryFragment;
import com.example.returns.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

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
    }

    public void showItemDetail(Item item) {
        ItemDetailFragment detailFragment = ItemDetailFragment.newInstance(item);
        detailFragment.show(getSupportFragmentManager(), detailFragment.getTag());
    }
}