package com.example.returns.DB;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.storage.FirebaseStorage;

public class AppDatabase {
    private static FirebaseFirestore dbInstance;
    private static FirebaseStorage storageInstance;

    public static synchronized FirebaseFirestore getDb() {
        if (dbInstance == null) {
            dbInstance = FirebaseFirestore.getInstance();

            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .build();
            dbInstance.setFirestoreSettings(settings);
        }
        return dbInstance;
    }

    // Storage 인스턴스 가져오기 (이미지 업로드용)
    public static synchronized FirebaseStorage getStorage() {
        if (storageInstance == null) {
            storageInstance = FirebaseStorage.getInstance();
        }
        return storageInstance;
    }
}