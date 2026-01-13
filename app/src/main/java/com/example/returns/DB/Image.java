package com.example.returns.DB;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class Image {
    // 결과를 보고받을 리스너 인터페이스
    public interface StringCallback {
        void onSuccess(String url);  // 사용 가능할 때
        void onError(Exception e); // 서버 에러 시
    }

    // 1. 중복 체크 함수
    public static void uploadImage(Uri fileUri, StringCallback callback) {
        String fileName = "images/" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = AppDatabase.getStorage().getReference().child(fileName);

        storageRef.putFile(fileUri).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String remoteUrl = uri.toString(); // 우리가 원하던 그 "http..." 주소!
                    callback.onSuccess(remoteUrl);
                }).addOnFailureListener(  e-> {
                    storageRef.delete();
                    callback.onError(e);
                });
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public interface Callback {
        void onSuccess();  // 사용 가능할 때
        void onError(Exception e); // 서버 에러 시
    }
    public static void eraseImage(String ImageUrl) {
        StorageReference storageRef = AppDatabase.getStorage().getReferenceFromUrl(ImageUrl);
        storageRef.delete().addOnSuccessListener(aVoid -> {

        }).addOnFailureListener(e -> {
            Log.e("FirebaseImage", "이미지 삭제 실패 (무시하고 진행): " + e.getMessage(), e);
        });
    }
}