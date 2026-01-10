package com.example.returns.home;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.returns.DB.AppDatabase;
import com.example.returns.DB.Item;
import com.example.returns.DB.ItemDao;
import com.example.returns.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private ItemAdapter adapter;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredList = new ArrayList<>();
    private ItemDao itemDao;

    private String currentSearch = "";
    private String currentType = "전체";
    private String currentCategory = "전체";
    private MaterialButton btnCategoryDropdown;

    public HomeFragment() { super(R.layout.fragment_home); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        itemDao = AppDatabase.getInstance(getContext()).itemDao();

        // 1. 리사이클러뷰 설정
        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(filteredList);
        rv.setAdapter(adapter);

        // 2. 검색창 로직
        EditText searchEdit = view.findViewById(R.id.searchEditText);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                currentSearch = (s != null) ? s.toString() : "";
                applyFilters();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // 3. 카테고리 드롭다운
        btnCategoryDropdown = view.findViewById(R.id.btnCategoryDropdown);
        btnCategoryDropdown.setOnClickListener(v -> showCategoryDropdown(v));

        // 4. 타입 (전체/습득/분실)
        ChipGroup typeGroup = view.findViewById(R.id.typeChipGroup);
        typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipTypeAll) currentType = "전체";
            else if (checkedId == R.id.chipTypeFound) currentType = "습득";
            else if (checkedId == R.id.chipTypeLost) currentType = "분실";
            applyFilters();
        });

        loadData();
    }

    private void showCategoryDropdown(View anchor) {
        String[] cats = {"전체","휴대폰","노트북", "지갑", "우산", "가방", "카드", "책", "기타"};
        ListPopupWindow popup = new ListPopupWindow(getContext());
        popup.setAnchorView(anchor);
        popup.setAdapter(new ArrayAdapter<>(getContext(), R.layout.item_dropdown, cats));
        popup.setWidth(anchor.getWidth() + 100);

        popup.setOnItemClickListener((parent, view, position, id) -> {
            currentCategory = cats[position];
            btnCategoryDropdown.setText("태그: " + currentCategory);
            applyFilters();
            popup.dismiss();
        });
        popup.show();
    }

    private void loadData() {
        allItems = itemDao.getAllItems();
        applyFilters();
    }

    private void applyFilters() {
        filteredList.clear();

        String searchKeyword = (currentSearch != null) ? currentSearch.toLowerCase() : "";

        for (Item item : allItems) {
            if (item == null) continue;

            // 1. 검색어 필터 (제목/위치가 null일 경우를 대비해 빈 문자열 처리)
            String title = (item.getTitle() != null) ? item.getTitle().toLowerCase() : "";
            String location = (item.getLocation() != null) ? item.getLocation().toLowerCase() : "";

            boolean matchesSearch = title.contains(searchKeyword) || location.contains(searchKeyword);

            // 2. 타입 필터 ("전체", "습득", "분실" 비교)
            boolean matchesType = "전체".equals(currentType) ||
                    ("습득".equals(currentType) && "FOUND".equals(item.getType())) ||
                    ("분실".equals(currentType) && "LOST".equals(item.getType()));

            // 3. 카테고리 필터 (아이템의 카테고리가 null인지 먼저 체크)
            boolean matchesCategory = "전체".equals(currentCategory) ||
                    (item.getCategory() != null && item.getCategory().equals(currentCategory));

            // 모든 조건 충족 시 추가
            if (matchesSearch && matchesType && matchesCategory) {
                filteredList.add(item);
            }
        }

        // 리스트 갱신
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}