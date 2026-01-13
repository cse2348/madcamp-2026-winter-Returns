package com.example.returns.DB;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class Comments {

    @Exclude
    public String Id;

    public DocumentReference ItemId;        // 게시글 ID
    public String Message;     // 댓글 내용
    public Timestamp Timestamp;   // 작성 시간
    public String Author;

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
    public void uploadToFirebase(Callback callback) {
        FirebaseFirestore db = AppDatabase.getDb();
        ItemId.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("Comments")
                                .add(this)
                                .addOnSuccessListener(ref -> callback.onSuccess())
                                .addOnFailureListener(e -> callback.onError(e));
                    } else {
                        callback.onError(new Exception("원글이 존재하지 않아 댓글을 달 수 없습니다."));
                    }
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
                .orderBy("Timestamp", Query.Direction.ASCENDING) // 시간순 정렬
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