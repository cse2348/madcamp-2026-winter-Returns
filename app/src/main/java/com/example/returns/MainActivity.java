package com.example.returns;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.gallery.GalleryFragment;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // 1. 시스템 바 패딩 설정 (EdgeToEdge 대응)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. 처음 실행 시 GalleryFragment를 화면에 띄우기
        // XML의 FrameLayout ID가 '@+id/container'이므로 이름을 맞췄습니다.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new GalleryFragment())
                    .commit();
        }

        /*
        // DB 테스트용 주석 (필요 시 해제)
        AppDatabase db = AppDatabase.getInstance(this);
        List<Item> itemList = db.itemDao().getAllItems();
        Log.d("ReturnsDB", "아이템 개수: " + itemList.size());
        */
    }
}