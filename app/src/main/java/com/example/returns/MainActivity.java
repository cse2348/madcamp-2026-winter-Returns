package com.example.returns;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        /*
        // 1. DB 통 가져오기
        AppDatabase db = AppDatabase.getInstance(this);

        // 2. 저장할 물건(Item) 만들기
        Item testItem = new Item();
        testItem.setTitle("테스트 우산");
        testItem.setLocation("강의동 2층");
        testItem.setType("FOUND");

       // 3. 리모컨으로 DB에 넣기
        db.itemDao().insert(testItem);

       // 4. 잘 들어갔나 확인 (로그캣에 출력됨)
        List<Item> itemList = db.itemDao().getAllItems();
        Log.d("ReturnsDB", "아이템 개수: " + itemList.size());
        */
    }
}