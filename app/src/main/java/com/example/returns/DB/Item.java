package com.example.returns.DB;

import android.net.Uri;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Item{

    private int id; // 데이터 고유 번호
    private String type;           // "LOST" 또는 "FOUND"
    private String category;
    private String title;          // 제목
    private String location;       // 발견/분실 장소
    private String dateOccurred;   // 날짜
    private String status;         // "보관중", "찾아감", "미발견"
    private String authorNickname; // 작성자 닉네임 (본인 확인용)
    private String contactName;    // 회수 방법
    private String notes;          // 특징/추가 설명
    private String handledBy;      // 보관 장소
    private String imageUriString; // 이미지 경로 (String 형태)

    // 기본 생성자
    public Item() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDateOccurred() { return dateOccurred; }
    public void setDateOccurred(String dateOccurred) { this.dateOccurred = dateOccurred; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAuthorNickname() { return authorNickname; }
    public void setAuthorNickname(String authorNickname) { this.authorNickname = authorNickname; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }

    public String getImageUriString() { return imageUriString; }
    public void setImageUriString(String imageUriString) { this.imageUriString = imageUriString; }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public void uploadItemWithImage(Callback callback) {
        String imageUri = getImageUriString();

        if (imageUri != null && !imageUri.isEmpty() && !imageUri.startsWith("http")) {
            Image.uploadImage(Uri.parse(imageUri), new Image.Callback() {
                @Override
                public void onSuccess(String url) {
                    setImageUriString(url);
                    uploadToFirebase(callback);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } else {
            uploadToFirebase(callback);
        }
    }

    private void uploadToFirebase(Callback callback) {
        AppDatabase.getDb().collection("items")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }
}