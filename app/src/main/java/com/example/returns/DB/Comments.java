package com.example.returns.DB;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Comments {

    public String id;

    public com.google.firebase.firestore.DocumentReference ItemId;        // 게시글 ID
    public String message;     // 댓글 내용
    public String timestamp;   // 작성 시간
    public String authorName;

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
    public void uploadToFirebase(Callback callback) {
        AppDatabase.getDb().collection("Notifications")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public interface ListCommentCallback {
        void onSuccess(List<Comments> list);
        void onError(Exception e);
    }

    public static void getCommentsForItem(Item item,ListCommentCallback callback) {
        FirebaseFirestore db = AppDatabase.getDb();
        DocumentReference itemRef = db.collection("Items").document(item.getId());

        db.collection("Comments")
                .whereEqualTo("ItemId", itemRef)
                .orderBy("timestamp", Query.Direction.ASCENDING) // 시간순 정렬
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comments> commentsList = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Comments comment = doc.toObject(Comments.class);
                        if (comment != null) {
                            commentsList.add(comment);
                        }
                    }
                    callback.onSuccess(commentsList);
                })
                .addOnFailureListener(callback::onError);
    }
}