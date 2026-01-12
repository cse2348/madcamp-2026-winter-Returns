package com.example.returns.DB;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    void insert(Notification notification);

    @Query("SELECT * FROM notifications WHERE receiverNickname = :nickname ORDER BY id DESC")
    List<Notification> getNotificationsForUser(String nickname);

    @Delete
    void delete(Notification notification);
}