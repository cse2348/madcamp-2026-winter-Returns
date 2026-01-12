package com.example.returns.DB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments")
public class Comment {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int itemId;        // 게시글 ID
    public String authorName;  // 작성자 닉네임
    public String message;     // 댓글 내용
    public String timestamp;   // 작성 시간
}