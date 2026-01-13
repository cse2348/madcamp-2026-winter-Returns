package com.example.returns.DB;

public class Comments {

    public String id;

    public com.google.firebase.firestore.DocumentReference ItemId;        // 게시글 ID
    public String message;     // 댓글 내용
    public String timestamp;   // 작성 시간


    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }
    private void uploadToFirebase(Callback callback) {
        AppDatabase.getDb().collection("Notifications")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }
}