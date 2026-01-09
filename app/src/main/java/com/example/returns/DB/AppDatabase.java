package com.example.returns.DB;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. 어떤 entities를 쓸지 결정
@Database(entities = {Item.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    // 2. DAO을 가져올 수 있게 연결
    public abstract ItemDao itemDao();

    // 3. DB는 메모리를 많이 차지하니깐 하나만 만들기
    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "returns_db") // DB 이름
                    .fallbackToDestructiveMigration() // 버전 바뀔 때 기존 데이터 초기화 허용
                    .allowMainThreadQueries() // 메인 스레드에서 접근 허용
                    .build();
        }
        return instance;
    }
}
