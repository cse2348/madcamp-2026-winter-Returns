package com.example.returns.add;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class AddFoundFragment extends Fragment {

    public AddFoundFragment() {
        super(R.layout.fragment_addfound);
    }

    private Uri selectedImageUri = null;
    private ImageView ivPhoto;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. XML 뷰 컴포넌트 연결
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etLocation = view.findViewById(R.id.et_location);
        EditText etTime = view.findViewById(R.id.et_time);
        EditText etFeatures = view.findViewById(R.id.et_features);
        EditText etStorage = view.findViewById(R.id.et_storage);
        EditText etHowToFind = view.findViewById(R.id.et_how_to_find);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_category);
        ivPhoto = view.findViewById(R.id.iv_photo);
        View btnImageAdd = view.findViewById(R.id.btn_image_add);
        Button btnSubmit = view.findViewById(R.id.btn_submit);

        // 2. 갤러리 실행기
        ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPhoto.setImageURI(uri);
                        ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    } else {
                        Toast.makeText(getContext(), "선택이 취소되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 사진 추가 버튼 클릭 시 갤러리 열기
        btnImageAdd.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // 3. 습득 시간 입력창 클릭 시 달력 띄우기
        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(getContext(), (view1, y, m, d) -> {
                etTime.setText(y + "-" + (m + 1) + "-" + d);
            }, year, month, day);
            datePicker.show();
        });

        // 4. 등록 버튼 클릭 시 DB 저장
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();

            if (title.isEmpty()) {
                Toast.makeText(getContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // DB 저장을 위한 Item 객체 생성
            Item newItem = new Item();
            newItem.setTitle(title);
            newItem.setLocation(etLocation.getText().toString());
            newItem.setDateOccurred(etTime.getText().toString());
            newItem.setHandledBy(etStorage.getText().toString());

            // 특징과 찾는 방법을 합쳐서 notes 필드에 저장
            String combinedNotes = "특징: " + etFeatures.getText().toString() + "\n" +
                    "찾는 법: " + etHowToFind.getText().toString();
            newItem.setNotes(combinedNotes);

            // 이미지 경로 저장
            if (selectedImageUri != null) {
                newItem.setImageUriString(selectedImageUri.toString());
            }

            newItem.setType("FOUND");
            newItem.setStatus("보관중");

            // 선택된 카테고리 텍스트 가져오기
            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID) {
                Chip chip = view.findViewById(checkedChipId);
                newItem.setCategory(chip.getText().toString());
            } else {
                newItem.setCategory("기타");
            }

            // 5. DB 저장 실행
            new Thread(() -> {
                try {
                    AppDatabase.getInstance(getContext()).itemDao().insert(newItem);

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "습득물이 등록되었습니다.", Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "저장 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();
        });
    }
}