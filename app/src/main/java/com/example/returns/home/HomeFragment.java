package com.example.returns.home;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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
import com.example.returns.MainActivity;
import com.example.returns.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private ItemAdapter adapter;
    private List<Item> allItems = new ArrayList<>();
    private List<Item> filteredList = new ArrayList<>();

    private String currentSearch = "";
    private String currentType = "전체";
    private String currentCategory = "전체";
    private MaterialButton btnCategoryDropdown;

    public HomeFragment() { super(R.layout.fragment_home); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. RecyclerView 설정
        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(filteredList, item -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showItemDetail(item);
            }
        });
        rv.setAdapter(adapter);

        // 2. 검색창 로직 (엔터 시 필터 적용)
        EditText searchEdit = view.findViewById(R.id.searchEditText);
        searchEdit.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                currentSearch = searchEdit.getText().toString().trim();
                applyFilters();
                hideKeyboard(searchEdit);
                return true;
            }
            return false;
        });

        // 3. 카테고리 드롭다운 설정
        btnCategoryDropdown = view.findViewById(R.id.btnCategoryDropdown);
        btnCategoryDropdown.setOnClickListener(v -> showCategoryDropdown(v));

        // 4. 타입 필터
        ChipGroup typeGroup = view.findViewById(R.id.typeChipGroup);
        typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipTypeAll) currentType = "전체";
            else if (checkedId == R.id.chipTypeFound) currentType = "습득";
            else if (checkedId == R.id.chipTypeLost) currentType = "분실";
            applyFilters();
        });

        loadData();
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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

    // 데이터 로드 
    public void loadData() {
        new Thread(() -> {
            if (itemDao != null) {
                allItems = itemDao.getAllItems();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(this::applyFilters);
                }
            }
        }).start();
    }

    private void applyFilters() {
        filteredList.clear();
        String searchKeyword = (currentSearch != null) ? currentSearch.toLowerCase() : "";

        for (Item item : allItems) {
            if (item == null) continue;

            // 1. 검색어 필터
            String title = (item.getTitle() != null) ? item.getTitle().toLowerCase() : "";
            String location = (item.getLocation() != null) ? item.getLocation().toLowerCase() : "";
            boolean matchesSearch = title.contains(searchKeyword) || location.contains(searchKeyword);

            // 2. 타입 필터
            boolean matchesType = "전체".equals(currentType) ||
                    ("습득".equals(currentType) && "FOUND".equalsIgnoreCase(item.getType())) ||
                    ("분실".equals(currentType) && "LOST".equalsIgnoreCase(item.getType()));

            // 3. 카테고리 필터
            boolean matchesCategory = "전체".equals(currentCategory) ||
                    (item.getCategory() != null && item.getCategory().equals(currentCategory));

            if (matchesSearch && matchesType && matchesCategory) {
                filteredList.add(item);
            }
        }

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