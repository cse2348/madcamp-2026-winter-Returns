package com.example.returns.DB;

import android.net.Uri;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Item implements Serializable{

    private String id; // 데이터 고유 번호
    private String type;           // "LOST" 또는 "FOUND"
    private String category;
    private String title;          // 제목
    private String location;       // 발견/분실 장소
    private String dateOccurred;   // 날짜
    private String status;         // "보관중", "찾아감", "미발견"
    private DocumentReference author; // 작성자 닉네임 (본인 확인용)
    private String contactName;    // 회수 방법
    private String notes;          // 특징/추가 설명
    private String handledBy;      // 보관 장소
    private String imageUriString; // 이미지 경로 (String 형태)

    private List<String> searchKeywords; //검색 키워드들

    // 기본 생성자
    public Item() {
    }

    public String getId() { return id; }

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

    public com.google.firebase.firestore.DocumentReference getAuthor(){return author;}
    public void setAuthor(com.google.firebase.firestore.DocumentReference author) {this.author=author;}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }

    public String getImageUriString() { return imageUriString; }
    public void setImageUriString(String imageUriString) { this.imageUriString = imageUriString; }

    public DocumentReference getRef(){
        return AppDatabase.getDb()
            .collection("Items")
            .document(id);
    }

    public void createSearchKeyword()
    {
        Set<String> keywordSet = new HashSet<>();
        keywordSet.addAll(generateSearchKeywords(this.title));
        keywordSet.addAll(generateSearchKeywords(this.category));
        keywordSet.addAll(generateSearchKeywords(this.location));
        keywordSet.addAll(generateSearchKeywords(this.dateOccurred));
        keywordSet.addAll(generateSearchKeywords(this.status));
        keywordSet.addAll(generateSearchKeywords(this.notes));
        keywordSet.addAll(generateSearchKeywords(this.handledBy));
        this.searchKeywords = new ArrayList<>(keywordSet);
    }
    private List<String> generateSearchKeywords(String tar) {
        if (tar == null || tar.isEmpty()) return new ArrayList<String>();

        List<String> keywordSet = new ArrayList<String>();
        String[] words = this.title.toLowerCase().split("\\s+");
        for (String word : words) {
            if (!word.isEmpty()) {
                for (int i = 1; i <= word.length(); i++) {
                    keywordSet.add(word.substring(0, i));
                }
            }
        }
        return keywordSet;
    }

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public void uploadItemWithImage(Callback callback) {
        String imageUri = getImageUriString();

        if (imageUri != null && !imageUri.isEmpty() && !imageUri.startsWith("http")) {
            Image.uploadImage(Uri.parse(imageUri), new Image.StringCallback() {
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
        createSearchKeyword();
        AppDatabase.getDb().collection("items")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    id=documentReference.getId();
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public void updateItemWithImage(String docId,Callback callback) {
        String imageUri = getImageUriString();

        if (imageUri != null && !imageUri.isEmpty() && !imageUri.startsWith("http")) {
            String old_image=getImageUriString();
            Image.uploadImage(Uri.parse(imageUri), new Image.StringCallback() {
                @Override
                public void onSuccess(String url) {
                    Image.eraseImage(old_image);
                    setImageUriString(url);
                    updateToFirebase(docId,callback);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } else {
            updateToFirebase(docId,callback);
        }
    }
    private void updateToFirebase(String docId, Callback callback) {
        createSearchKeyword();
        AppDatabase.getDb().collection("items").document(docId)
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public interface ListItemCallback {
        void onSuccess(List<Item> list);
        void onError(Exception e);
    }
    public static void getAllItems(ListItemCallback callback) {
        AppDatabase.getDb().collection("Items")
                .orderBy("dateOccured", Query.Direction.DESCENDING) // 최신순
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Item n = doc.toObject(Item.class);
                        if (n != null) {
                            n.id = doc.getId(); // 수정할 때 필요함
                            list.add(n);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e->callback.onError(e));
    }

    public static void queryItems(String query,String type, String category, ListItemCallback callback) {
        String lowerQuery = query.toLowerCase().trim();
        Query baseQuery = AppDatabase.getDb().collection("items");
        if (!type.equals("전체")) {
            String typeValue = type.equals("습득") ? "FOUND" : "LOST";
            baseQuery = baseQuery.whereEqualTo("type", typeValue);
        }
        if (!category.equals("전체")) {
            baseQuery = baseQuery.whereEqualTo("category", category);
        }
        if (!lowerQuery.isEmpty()) {
            baseQuery = baseQuery.whereArrayContains("searchKeywords", lowerQuery);
        }

        // 5. 서버에서 가져오기
        baseQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Item> r_val=new ArrayList<Item>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Item item = doc.toObject(Item.class);
                if (item != null) {
                    item.id =doc.getId();
                    r_val.add(item);
                }
            }
            callback.onSuccess(r_val);
        }).addOnFailureListener(e -> {
            callback.onError(e);
        });
    }

    public void deleteItem(Callback callback) {
        FirebaseFirestore db = AppDatabase.getDb();
        DocumentReference itemRef = AppDatabase.getDb()
                .collection("Items")
                .document(id);


        db.collection("Comments")
            .whereEqualTo("ItemID", itemRef) // 여기서 핵심은 String이 아닌 Reference 객체를 넣는 것!
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                WriteBatch batch = db.batch();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    batch.delete(doc.getReference());
                }
                batch.delete(itemRef);
                batch.commit().addOnSuccessListener(aVoid -> {
                    if (getImageUriString() != null) Image.eraseImage(getImageUriString());
                    callback.onSuccess();
                }).addOnFailureListener(callback::onError);
            })
            .addOnFailureListener(e -> callback.onError(e));
    }
}