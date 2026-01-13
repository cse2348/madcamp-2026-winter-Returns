package com.example.returns.DB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Notification {

    public String id;

    public com.google.firebase.firestore.DocumentReference Receiver; // 알림을 받을 사람 (게시글 주인)
    public String Title;           // 게시글 제목
    public String timestamp;       // 알림 시간

    public boolean isRead;         // 읽음 여부

    public Notification() {
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
    private void uploadToFirebase(Callback callback) {
        AppDatabase.getDb().collection("Notifications")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    this.id = documentReference.getId();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public interface ListNotificationCallback {
        void onSuccess(List<Notification> list);
        void onError(Exception e);
    }

    public static void getUnreadNotifications(String nickname, ListNotificationCallback callback) {
        AppDatabase.getDb().collection("Notifications")
                .whereEqualTo("receiverNickname", nickname)
                .whereEqualTo("isRead", false) // 안 읽은 것만 필터링
                .orderBy("timestamp", Query.Direction.DESCENDING) // 최신순
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            n.id = doc.getId(); // 수정할 때 필요함
                            list.add(n);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e->callback.onError(e));
    }

    public static void markAsRead(String docId, Callback callback) {
        // 굳이 찾기(get)를 할 필요 없이, 주소(docId)로 바로 가서 업데이트합니다.
        AppDatabase.getDb().collection("Notifications").document(docId)
                .update("isRead", true)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }
}