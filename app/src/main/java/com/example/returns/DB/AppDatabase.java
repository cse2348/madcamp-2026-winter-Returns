package com.example.returns.DB;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.List;

@Database(entities = {Item.class, Comment.class, Notification.class}, version = 4)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ItemDao itemDao();
    public abstract CommentDao commentDao();
    public abstract NotificationDao notificationDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "returns_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public void insertMockData(Context context) {
        ItemDao dao = getInstance(context).itemDao();

        // 1. 습득물 Mock 데이터
        Item item1 = new Item();
        item1.setTitle("에어팟 프로");
        item1.setType("FOUND");
        item1.setCategory("기타");
        item1.setLocation("카페테리아");
        item1.setDateOccurred("2026-01-06");
        item1.setStatus("보관중");
        item1.setAuthorNickname("관리자");
        item1.setNotes("뒤에 이름 각인되어 있어요");
        item1.setHandledBy("점원분께 맡겼습니다");
        item1.setContactName("점원분께 문의바랍니다");
        // 샘플 이미지 URL (Unsplash)
        item1.setImageUriString("file:///android_asset/airpods.png");
        dao.insert(item1);

        Item item2 = new Item();
        item2.setTitle("검정 백팩");
        item2.setType("FOUND");
        item2.setCategory("가방");
        item2.setLocation("중앙도서관 3층");
        item2.setDateOccurred("2026-01-05");
        item2.setStatus("찾아감");
        item2.setAuthorNickname("test");
        item2.setNotes("가방 안 태그에 이름에 적혀있어요");
        item2.setHandledBy("사서분꼐 맡겼습니다");
        item2.setContactName("사서분께 문의바랍니다");
        // 샘플 이미지 URL (Unsplash)
        item2.setImageUriString("file:///android_asset/backpack.png");
        dao.insert(item2);

        Item item3 = new Item();
        item3.setTitle("파랑 우산");
        item3.setType("LOST");
        item3.setCategory("우산");
        item3.setLocation("도서관 1층 입구");
        item3.setDateOccurred("2026-01-06");
        item3.setStatus("미발견");
        item3.setAuthorNickname("관리자");
        item3.setNotes("우산 가운데가 살짝 찢어져 있어요");
        // 샘플 이미지 URL (Unsplash)
        item3.setImageUriString("file:///android_asset/blue_umbrella.png");
        dao.insert(item3);

        Item item4 = new Item();
        item4.setTitle("학생증");
        item4.setType("LOST");
        item4.setCategory("카드");
        item4.setLocation("태울관");
        item4.setDateOccurred("2026-01-12");
        item4.setStatus("미발견");
        item4.setAuthorNickname("관리자");
        item4.setNotes("전산학부 3학년이에요");
        // 샘플 이미지 URL (Unsplash)
        item4.setImageUriString("file:///android_asset/id-card.png");
        dao.insert(item4);

        Item item5 = new Item();
        item5.setTitle("아이폰");
        item5.setType("LOST");
        item5.setCategory("휴대폰");
        item5.setLocation("E3-5");
        item5.setDateOccurred("2026-01-11");
        item5.setStatus("발견");
        item5.setAuthorNickname("관리자");
        item5.setNotes("증명사진 뒤에 있어요");
        item5.setHandledBy("전산학부 사무실에 맡겼습니다.");
        item5.setContactName("전산학부 사무실에 문의바랍니다.");
        // 샘플 이미지 URL (Unsplash)
        item5.setImageUriString("file:///android_asset/iphone.png");
        dao.insert(item5);

        Item item6 = new Item();
        item6.setTitle("검정 가죽 지갑");
        item6.setType("FOUND");
        item6.setCategory("지갑");
        item6.setLocation("학생회관 2층");
        item6.setDateOccurred("2026-01-05");
        item6.setStatus("찾아감");
        item6.setAuthorNickname("관리자");
        item6.setNotes("지갑에 민증 있어요 (04년생 입니다)");
        item6.setHandledBy("잃어버린 곳에 두고 갔어요");
        item6.setContactName(" 주변찾아보셔야 할것 같아요");
        // 샘플 이미지 URL (Unsplash)
        item6.setImageUriString("file:///android_asset/leather_wallet.png");
        dao.insert(item6);

        Item item7 = new Item();
        item7.setTitle("자동차 열쇠");
        item7.setType("LOST");
        item7.setCategory("기타");
        item7.setLocation("기계동 앞 주차장");
        item7.setDateOccurred("2026-01-04");
        item7.setStatus("보관중");
        item7.setAuthorNickname("관리자");
        item7.setNotes("현대 아반떼에요");
        // 샘플 이미지 URL (Unsplash)
        item7.setImageUriString("file:///android_asset/smart_key.png");
        dao.insert(item7);
    }


}