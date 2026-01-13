package com.example.returns.DB;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class Notification {

    @Exclude
    public String Id;

    public DocumentReference Receiver; // 알림을 받을 사람 (게시글 주인)
    public String Author;
    public String Title;           // 게시글 제목
    public Timestamp Timestamp;       // 알림 시간

    public Notification() {
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
    public void uploadToFirebase(Callback callback) {
        AppDatabase.getDb().collection("Notifications")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    this.Id = documentReference.getId();
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
        DocumentReference myRef = User.getReferenceByName(nickname);
        AppDatabase.getDb().collection("Notifications")
                .whereEqualTo("Receiver", myRef)
                .orderBy("Timestamp", Query.Direction.DESCENDING) // 최신순
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Notification> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            n.Id = doc.getId(); // 수정할 때 필요함
                            list.add(n);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e->callback.onError(e));
    }

    public void deleteNotification(Callback callback) {
        AppDatabase.getDb().collection("Notifications").document(Id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e);
                });
    }
}