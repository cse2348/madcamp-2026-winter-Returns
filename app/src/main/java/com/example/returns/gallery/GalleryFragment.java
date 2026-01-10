package com.example.returns.gallery;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.MainActivity;
import com.example.returns.R;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class GalleryFragment extends Fragment {
    private GalleryAdapter adapter;
    private String currentSearch = "";
    private String currentType = "전체";
    private String currentCategory = "전체";
    private ListPopupWindow categoryPopupMenu;
    private MaterialButton btnCategoryDropdown;

    public GalleryFragment() { super(R.layout.fragment_gallery); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. RecyclerView 설정
        RecyclerView rv = view.findViewById(R.id.galleryRecyclerView);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new GalleryAdapter();

        adapter.setOnItemClickListener(item -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showItemDetail(item);
            }
        });

        rv.setAdapter(adapter);

        // 2. DB 데이터 로드
        loadDataFromDB();

        // 3. 검색창 로직
        EditText searchEdit = view.findViewById(R.id.searchEditText);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                currentSearch = s.toString();
                applyFilters();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 4. 카테고리 드롭다운 버튼 설정
        btnCategoryDropdown = view.findViewById(R.id.btnCategoryDropdown);
        btnCategoryDropdown.setOnClickListener(v -> showCategoryDropdown(v));

        // 5. 타입 칩 이벤트
        ChipGroup typeGroup = view.findViewById(R.id.typeChipGroup);
        typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipTypeAll) currentType = "전체";
            else if (checkedId == R.id.chipTypeFound) currentType = "습득";
            else if (checkedId == R.id.chipTypeLost) currentType = "분실";
            applyFilters();
        });
    }

    private void showCategoryDropdown(View anchor) {
        String[] cats = {"전체","휴대폰","노트북", "지갑", "우산",  "가방", "카드", "책",  "기타"};

        categoryPopupMenu = new ListPopupWindow(getContext());
        categoryPopupMenu.setAnchorView(anchor);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_dropdown, cats);
        categoryPopupMenu.setAdapter(adapter);

        categoryPopupMenu.setWidth(anchor.getWidth() + 100);
        categoryPopupMenu.setHeight(ListPopupWindow.WRAP_CONTENT);
        categoryPopupMenu.setModal(true);

        categoryPopupMenu.setOnItemClickListener((parent, view, position, id) -> {
            currentCategory = cats[position];
            btnCategoryDropdown.setText("태그: " + currentCategory);
            applyFilters();
            categoryPopupMenu.dismiss();
        });
        categoryPopupMenu.show();
    }

    private void loadDataFromDB() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        List<Item> items = db.itemDao().getAllItems();
        adapter.updateData(items);
    }
    private void applyFilters() {
        if (adapter != null) {
            adapter.filter(currentSearch, currentType, currentCategory);
        }
    }
}