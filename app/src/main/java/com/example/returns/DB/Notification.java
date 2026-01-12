package com.example.returns.DB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notifications")
public class Notification {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String receiverNickname; // 알림을 받을 사람 (게시글 주인)
    public String title;           // 게시글 제목
    public String commenterName;   // 댓글 쓴 사람
    public String timestamp;       // 알림 시간
}