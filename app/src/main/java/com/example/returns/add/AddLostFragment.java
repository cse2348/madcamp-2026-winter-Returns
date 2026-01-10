package com.example.returns.add;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class AddLostFragment extends Fragment {
    public AddLostFragment() { super(R.layout.fragment_addlost); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etLocation = view.findViewById(R.id.et_location);
        EditText etFeatures = view.findViewById(R.id.et_features);
        EditText etContact = view.findViewById(R.id.et_contact_phone);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_category);
        Button btnSubmit = view.findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String location = etLocation.getText().toString().trim();

            int checkedChipId = chipGroup.getCheckedChipId();
            String category = (checkedChipId != View.NO_ID) ?
                    ((Chip)view.findViewById(checkedChipId)).getText().toString() : "기타";

            if (title.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "제목과 장소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            Item newItem = new Item();
            newItem.setTitle(title);
            newItem.setLocation(location);
            newItem.setCategory(category);
            newItem.setType("LOST");
            newItem.setStatus("미발견");
            newItem.setNotes(etFeatures.getText().toString());
            newItem.setContactPhone(etContact.getText().toString());

            new Thread(() -> {
                AppDatabase.getInstance(getContext()).itemDao().insert(newItem);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "분실물 등록 완료!", Toast.LENGTH_SHORT).show();
                        etTitle.setText(""); etLocation.setText(""); etFeatures.setText(""); etContact.setText("");
                    });
                }
            }).start();
        });
    }
}