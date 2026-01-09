package com.example.returns.DB;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ItemDao {

    // 1. 새로운 물품 등록하기 (탭 3, 4에서 사용)
    @Insert
    void insert(Item item);

    // 2. 모든 목록 가져오기 (최신순으로 정렬 - 탭 1에서 사용)
    @Query("SELECT * FROM items ORDER BY id DESC")
    List<Item> getAllItems();

    // 3. 검색하기 (제목이나 장소에 키워드가 포함된 경우 - 탭 1 검색창)
    @Query("SELECT * FROM items WHERE title LIKE '%' || :search || '%' OR location LIKE '%' || :search || '%' ORDER BY id DESC")
    List<Item> searchItems(String search);

    // 4. 필터링하기 (분실물만 보기, 습득물만 보기 - 탭 1 필터 칩)
    @Query("SELECT * FROM items WHERE type = :type ORDER BY id DESC")
    List<Item> getItemsByType(String type);

    // 5. 상태 변경하기 (보관중 -> 찾아감 업데이트 - 상세 페이지에서 사용)
    @Update
    void update(Item item);

    // 6. 데이터 삭제하기 (본인 글 삭제 - 상세 페이지에서 사용)
    @Delete
    void delete(Item item);
}