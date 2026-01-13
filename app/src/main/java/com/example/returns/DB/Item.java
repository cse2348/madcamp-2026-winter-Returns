package com.example.returns.DB;

import android.net.Uri;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Exclude;
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
    @Exclude
    private String Id; // 데이터 고유 번호

    private String Type;           // "LOST" 또는 "FOUND"
    private String Category;
    private String Title;          // 제목
    private String Location;       // 발견/분실 장소
    private String DateOccurred;   // 날짜
    private String Status;         // "보관중", "찾아감", "미발견"
    private String Author; // 작성자 닉네임 (본인 확인용)
    private String ContactName;    // 회수 방법
    private String Notes;          // 특징/추가 설명
    private String HandledBy;      // 보관 장소
    private String ImageUriString; // 이미지 경로 (String 형태)

    private List<String> SearchKeywords; //검색 키워드들

    // 기본 생성자
    public Item() {
    }

    @Exclude
    public String getId() { return Id; }

    public String getType() { return Type; }
    public void setType(String type) { this.Type = type; }

    public String getCategory() { return Category; }
    public void setCategory(String category) { this.Category = category; }

    public String getTitle() { return Title; }
    public void setTitle(String title) { this.Title = title; }

    public String getLocation() { return Location; }
    public void setLocation(String location) { this.Location = location; }

    public String getDateOccurred() { return DateOccurred; }
    public void setDateOccurred(String dateOccurred) { this.DateOccurred = dateOccurred; }

    public String getAuthor(){return Author;}
    public void setAuthor(String author) {this.Author=author;}

    public String getStatus() { return Status; }
    public void setStatus(String status) { this.Status = status; }

    public String getContactName() { return ContactName; }
    public void setContactName(String contactName) { this.ContactName = contactName; }

    public String getNotes() { return Notes; }
    public void setNotes(String notes) { this.Notes = notes; }

    public String getHandledBy() { return HandledBy; }
    public void setHandledBy(String handledBy) { this.HandledBy = handledBy; }

    public String getImageUriString() { return ImageUriString; }
    public void setImageUriString(String imageUriString) { this.ImageUriString = imageUriString; }

    @Exclude
    public DocumentReference getRef(){
        return AppDatabase.getDb()
            .collection("Items")
            .document(Id);
    }

    public void createSearchKeyword()
    {
        Set<String> keywordSet = new HashSet<>();
        keywordSet.addAll(generateSearchKeywords(this.Title));
        keywordSet.addAll(generateSearchKeywords(this.Category));
        keywordSet.addAll(generateSearchKeywords(this.Location));
        keywordSet.addAll(generateSearchKeywords(this.DateOccurred));
        keywordSet.addAll(generateSearchKeywords(this.Status));
        keywordSet.addAll(generateSearchKeywords(this.Notes));
        keywordSet.addAll(generateSearchKeywords(this.HandledBy));
        this.SearchKeywords = new ArrayList<>(keywordSet);
    }
    private List<String> generateSearchKeywords(String tar) {
        if (tar == null || tar.isEmpty()) return new ArrayList<String>();

        List<String> keywordSet = new ArrayList<String>();
        String[] words = tar.toLowerCase().split("\\s+");
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
        AppDatabase.getDb().collection("Items")
                .add(this)
                .addOnSuccessListener(documentReference -> {
                    Id=documentReference.getId();
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    callback.onError(e);
                });
    }

    public void updateItemWithImage(String Old_image,Callback callback) {
        String imageUri = getImageUriString();

        if (imageUri != null && !imageUri.isEmpty() && !imageUri.startsWith("http")) {
            Image.uploadImage(Uri.parse(imageUri), new Image.StringCallback() {
                @Override
                public void onSuccess(String url) {
                    Image.eraseImage(Old_image);
                    setImageUriString(url);
                    updateToFirebase(callback);
                }

                @Override
                public void onError(Exception e) {
                    callback.onError(e);
                }
            });
        } else {
            updateToFirebase(callback);
        }
    }
    private void updateToFirebase(Callback callback) {
        createSearchKeyword();
        AppDatabase.getDb().collection("Items").document(Id)
                .set(this)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public interface ListItemCallback {
        void onSuccess(List<Item> list);
        void onError(Exception e);
    }

    @Exclude
    public static void getAllItems(ListItemCallback callback) {
        AppDatabase.getDb().collection("Items")
                .orderBy("DateOccurred", Query.Direction.DESCENDING) // 최신순
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Item> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Item n = doc.toObject(Item.class);
                        if (n != null) {
                            n.Id = doc.getId(); // 수정할 때 필요함
                            list.add(n);
                        }
                    }
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e->callback.onError(e));
    }

    public static void queryItems(String query,String type, String category, ListItemCallback callback) {
        String lowerQuery = query.toLowerCase().trim();
        Query baseQuery = AppDatabase.getDb().collection("Items");
        if (!type.equals("전체")) {
            String typeValue = type.equals("습득") ? "FOUND" : "LOST";
            baseQuery = baseQuery.whereEqualTo("Type", typeValue);
        }
        if (!category.equals("전체")) {
            baseQuery = baseQuery.whereEqualTo("Category", category);
        }
        if (!lowerQuery.isEmpty()) {
            baseQuery = baseQuery.whereArrayContains("SearchKeywords", lowerQuery);
        }

        // 5. 서버에서 가져오기
        baseQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Item> r_val=new ArrayList<Item>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Item item = doc.toObject(Item.class);
                if (item != null) {
                    item.Id =doc.getId();
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
                .document(Id);


        db.collection("Comments")
            .whereEqualTo("ItemId", itemRef) // 여기서 핵심은 String이 아닌 Reference 객체를 넣는 것!
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