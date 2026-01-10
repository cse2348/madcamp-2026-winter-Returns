package com.example.returns.DB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDao {

    // 1. 새로운 물품 등록하기
    @Insert
    void insert(Item item);

    // 2. 모든 목록 가져오기 (최신순)
    @Query("SELECT * FROM items ORDER BY id DESC")
    List<Item> getAllItems();

    // 3. 검색하기 (제목이나 장소)
    @Query("SELECT * FROM items WHERE title LIKE '%' || :search || '%' OR location LIKE '%' || :search || '%' ORDER BY id DESC")
    List<Item> searchItems(String search);

    // 4. 필터링하기 (LOST/FOUND)
    @Query("SELECT * FROM items WHERE type = :type ORDER BY id DESC")
    List<Item> getItemsByType(String type);

    // 5. 내가 쓴 글만 가져오기 (본인 게시물 관리용)
    @Query("SELECT * FROM items WHERE authorNickname = :nickname ORDER BY id DESC")
    List<Item> getItemsByAuthor(String nickname);

    // 6. 상태 업데이트 (보관중/찾아감 등)
    @Update
    void update(Item item);

    // 7. 데이터 삭제 (본인 확인 후 호출)
    @Delete
    void delete(Item item);
}