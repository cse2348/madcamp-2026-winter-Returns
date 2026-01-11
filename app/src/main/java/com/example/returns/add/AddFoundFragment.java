package com.example.returns.add;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.R;
import com.example.returns.home.HomeFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

public class AddFoundFragment extends Fragment {

    private Item editItem = null;
    private Uri selectedImageUri = null;
    private ImageView ivPhoto;

    public AddFoundFragment() {
        super(R.layout.fragment_addfound);
    }

    public static AddFoundFragment newInstance(Item item) {
        AddFoundFragment fragment = new AddFoundFragment();
        Bundle args = new Bundle();
        args.putSerializable("edit_item", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. 수정 모드인지 확인
        if (getArguments() != null) {
            editItem = (Item) getArguments().getSerializable("edit_item");
        }

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etLocation = view.findViewById(R.id.et_location);
        EditText etTime = view.findViewById(R.id.et_time);
        EditText etFeatures = view.findViewById(R.id.et_features);
        EditText etStorage = view.findViewById(R.id.et_storage);
        EditText etHowToFind = view.findViewById(R.id.et_how_to_find);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_category);
        ivPhoto = view.findViewById(R.id.iv_photo);
        Button btnSubmit = view.findViewById(R.id.btn_submit);
        TextView tvHeader = view.findViewById(R.id.tv_add_title);

        // 2. 수정 모드일 경우 기존 데이터 채우기
        if (editItem != null) {
            if (tvHeader != null) tvHeader.setText("게시글 수정");
            etTitle.setText(editItem.getTitle());
            etLocation.setText(editItem.getLocation());
            etTime.setText(editItem.getDateOccurred());
            etStorage.setText(editItem.getHandledBy());
            etFeatures.setText(editItem.getNotes());
            btnSubmit.setText("수정 완료");

            if (editItem.getImageUriString() != null) {
                selectedImageUri = Uri.parse(editItem.getImageUriString());
                Glide.with(this).load(selectedImageUri).into(ivPhoto);
            }
        }

        // 갤러리 실행기
        ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        ivPhoto.setImageURI(uri);
                        ivPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                }
        );

        view.findViewById(R.id.btn_image_add).setOnClickListener(v -> galleryLauncher.launch("image/*"));

        etTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (v1, y, m, d) -> {
                etTime.setText(y + "-" + (m + 1) + "-" + d);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // 3. 등록/수정 버튼 클릭
        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences pref = requireContext().getSharedPreferences("UserToken", Context.MODE_PRIVATE);
            String currentNickname = pref.getString("nickName", "익명");

            Item item = (editItem != null) ? editItem : new Item();
            item.setTitle(title);
            item.setLocation(etLocation.getText().toString());
            item.setDateOccurred(etTime.getText().toString());
            item.setHandledBy(etStorage.getText().toString());
            item.setAuthorNickname(currentNickname);
            item.setNotes("특징: " + etFeatures.getText().toString() + "\n방법: " + etHowToFind.getText().toString());
            item.setType("FOUND");
            item.setStatus("보관중");

            if (selectedImageUri != null) {
                item.setImageUriString(selectedImageUri.toString());
            }

            // 카테고리 설정
            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID) {
                Chip chip = view.findViewById(checkedChipId);
                item.setCategory(chip.getText().toString());
            }

            new Thread(() -> {
                try {
                    if (editItem != null) {
                        AppDatabase.getInstance(getContext()).itemDao().update(item); // 수정
                    } else {
                        AppDatabase.getInstance(getContext()).itemDao().insert(item); // 신규 등록
                    }

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), editItem != null ? "수정되었습니다." : "등록되었습니다.", Toast.LENGTH_SHORT).show();
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.container, new HomeFragment())
                                .commit();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}