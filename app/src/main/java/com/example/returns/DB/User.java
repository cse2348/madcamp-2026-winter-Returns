package com.example.returns.DB;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String nickname;

    // 결과를 보고받을 리스너 인터페이스
    public interface UserCallback {
        void onAvailable();  // 사용 가능할 때
        void onDuplicate();  // 중복일 때
        void onError(Exception e); // 서버 에러 시
    }

    // 1. 중복 체크 함수
    public static void checkNickname(String nickname, UserCallback callback) {
        AppDatabase.getDb().collection("users").document(nickname).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            callback.onDuplicate();
                        } else {
                            callback.onAvailable();
                        }
                    } else {
                        callback.onError(task.getException());
                    }
                });
    }

    // 2. 유저 추가 함수
    public static void register(String nickname, OnCompleteListener<Void> listener) {
        Map<String, Object> user = new HashMap<>();
        user.put("nickname", nickname);
        AppDatabase.getDb().collection("users").document(nickname)
                .set(user)
                .addOnCompleteListener(listener);
    }
}