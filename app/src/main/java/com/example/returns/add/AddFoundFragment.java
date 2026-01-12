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
import android.widget.LinearLayout;
import android.widget.RadioGroup;
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
import com.example.returns.MainActivity;
import com.example.returns.R;
import com.example.returns.home.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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

        LinearLayout layoutStatusEdit = view.findViewById(R.id.layout_status_edit);
        RadioGroup rgStatus = view.findViewById(R.id.rg_status);

        if (editItem != null) {
            if (tvHeader != null) tvHeader.setText("게시글 수정");
            etTitle.setText(editItem.getTitle());
            etLocation.setText(editItem.getLocation());
            etTime.setText(editItem.getDateOccurred());
            etStorage.setText(editItem.getHandledBy());

            etFeatures.setText(editItem.getNotes());
            etHowToFind.setText(editItem.getContactName());

            btnSubmit.setText("수정 완료");

            if (layoutStatusEdit != null) {
                layoutStatusEdit.setVisibility(View.VISIBLE);
                if ("찾아감".equals(editItem.getStatus())) {
                    rgStatus.check(R.id.rb_status_resolved);
                } else {
                    rgStatus.check(R.id.rb_status_unresolved);
                }
            }

            if (editItem.getImageUriString() != null) {
                selectedImageUri = Uri.parse(editItem.getImageUriString());
                Glide.with(this).load(selectedImageUri).into(ivPhoto);
            }
        }

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

            item.setNotes(etFeatures.getText().toString());
            item.setContactName(etHowToFind.getText().toString());

            item.setAuthorNickname(currentNickname);
            item.setType("FOUND");

            if (editItem != null) {
                int checkedId = rgStatus.getCheckedRadioButtonId();
                item.setStatus(checkedId == R.id.rb_status_resolved ? "찾아감" : "보관중");
            } else {
                item.setStatus("보관중");
            }

            if (selectedImageUri != null) {
                item.setImageUriString(selectedImageUri.toString());
            }

            int checkedChipId = chipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID) {
                Chip chip = view.findViewById(checkedChipId);
                item.setCategory(chip.getText().toString());
            }

            new Thread(() -> {
                try {
                    if (editItem != null) {
                        AppDatabase.getInstance(getContext()).itemDao().update(item);
                    } else {
                        AppDatabase.getInstance(getContext()).itemDao().insert(item);
                    }

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), editItem != null ? "수정되었습니다." : "등록되었습니다.", Toast.LENGTH_SHORT).show();

                        if (getActivity() instanceof MainActivity) {
                            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_nav);
                            if (bottomNav != null) {
                                bottomNav.setSelectedItemId(R.id.nav_home);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
}