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

public class AddFoundFragment extends Fragment {
    public AddFoundFragment() { super(R.layout.fragment_addfound); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText etTitle = view.findViewById(R.id.et_title);
        EditText etLocation = view.findViewById(R.id.et_location);
        EditText etStorage = view.findViewById(R.id.et_storage);
        EditText etHowToFind = view.findViewById(R.id.et_how_to_find);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_category);
        Button btnSubmit = view.findViewById(R.id.btn_submit);

        btnSubmit.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) return;

            Item newItem = new Item();
            newItem.setTitle(title);
            newItem.setLocation(etLocation.getText().toString());
            newItem.setHandledBy(etStorage.getText().toString());
            newItem.setNotes("수령방법: " + etHowToFind.getText().toString());
            newItem.setType("FOUND");
            newItem.setStatus("보관중");

            int checkedChipId = chipGroup.getCheckedChipId();
            newItem.setCategory((checkedChipId != View.NO_ID) ?
                    ((Chip)view.findViewById(checkedChipId)).getText().toString() : "기타");

            new Thread(() -> {
                AppDatabase.getInstance(getContext()).itemDao().insert(newItem);
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "습득물 등록 완료!", Toast.LENGTH_SHORT).show();
                        etTitle.setText(""); etLocation.setText(""); etStorage.setText(""); etHowToFind.setText("");
                    });
                }
            }).start();
        });
    }
}