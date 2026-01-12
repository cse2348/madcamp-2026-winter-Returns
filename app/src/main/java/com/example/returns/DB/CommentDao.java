package com.example.returns.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface CommentDao {
    @Insert
    void insert(Comment comment);

    @Query("SELECT * FROM comments WHERE itemId = :itemId ORDER BY id ASC")
    List<Comment> getCommentsForItem(int itemId);
}