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
import com.example.returns.home.HomeFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class AddLostFragment extends Fragment {

    public AddLostFragment() {
        super(R.layout.fragment_addlost);
    }

    private Uri selectedImageUri = null;
    private ImageView ivPhoto;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. XML 뷰 연결
        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etLocation = view.findViewById(R.id.et_location);
        EditText etTime = view.findViewById(R.id.et_time);
        EditText etFeatures = view.findViewById(R.id.et_features);
        EditText etContact = view.findViewById(R.id.et_contact_phone);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_category);
        ivPhoto = view.findViewById(R.id.iv_photo);
        View btnImageAdd = view.findViewById(R.id.btn_image_add);
        Button btnSubmit = view.findViewById(R.id.btn_submit);

        // 2. 갤러리 열기 기능
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

        btnImageAdd.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // 3. 분실 날짜 선택 (클릭 시 달력 팝업)
        if (etTime != null) {
            etTime.setOnClickListener(v -> {
                Calendar c = Calendar.getInstance();
                new DatePickerDialog(getContext(), (view1, y, m, d) -> {
                    etTime.setText(y + "-" + (m + 1) + "-" + d);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
            });
        }

        // 4. 분실물 등록 버튼 클릭
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            if (title.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "제목과 장소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // DB 저장을 위한 객체 생성
            Item newItem = new Item();
            newItem.setTitle(title);
            newItem.setLocation(location);
            newItem.setDateOccurred(etTime.getText().toString());
            newItem.setNotes(etFeatures.getText().toString());
            newItem.setContactPhone(etContact.getText().toString());

            newItem.setType("LOST");
            newItem.setStatus("미발견");

            // 이미지 경로 저장
            if (selectedImageUri != null) {
                newItem.setImageUriString(selectedImageUri.toString());
            }

            // 카테고리 설정
            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID) {
                Chip chip = view.findViewById(checkedChipId);
                newItem.setCategory(chip.getText().toString());
            } else {
                newItem.setCategory("기타");
            }

            // 5. DB 저장
            new Thread(() -> {
                try {
                    AppDatabase.getInstance(getContext()).itemDao().insert(newItem);

                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "분실물 등록 완료!", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.container,new HomeFragment())
                                    .commit();
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