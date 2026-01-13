package com.example.returns.gallery;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Toast;

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

        // 2. 데이터 초기 로드
        loadData();

        // 3. 검색창 로직
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

        // 4. 카테고리 버튼
        btnCategoryDropdown = view.findViewById(R.id.btnCategoryDropdown);
        btnCategoryDropdown.setOnClickListener(v -> showCategoryDropdown(v));

        // 5. 타입 칩 그룹
        ChipGroup typeGroup = view.findViewById(R.id.typeChipGroup);
        typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipTypeAll) currentType = "전체";
            else if (checkedId == R.id.chipTypeFound) currentType = "습득";
            else if (checkedId == R.id.chipTypeLost) currentType = "분실";
            applyFilters();
        });
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showCategoryDropdown(View anchor) {
        String[] cats = {"전체","휴대폰","노트북", "지갑", "우산", "가방", "카드", "책", "기타"};
        categoryPopupMenu = new ListPopupWindow(getContext());
        categoryPopupMenu.setAnchorView(anchor);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.item_dropdown, cats);
        categoryPopupMenu.setAdapter(adapter);
        categoryPopupMenu.setWidth(anchor.getWidth() + 100);
        categoryPopupMenu.setModal(true);

        categoryPopupMenu.setOnItemClickListener((parent, view, position, id) -> {
            currentCategory = cats[position];
            btnCategoryDropdown.setText("태그: " + currentCategory);
            applyFilters();
            categoryPopupMenu.dismiss();
        });
        categoryPopupMenu.show();
    }

    // 데이터 로드 (MainActivity에서 호출)
    public void loadData() {
        Item.getAllItems(new Item.ListItemCallback(){
            @Override
            public void onSuccess(List<Item> list) {
                if (isAdded()&&getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (adapter != null) {
                            adapter.updateData(list);
                            applyFilters();
                        }
                    });
                }
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()&&getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(),"서버 연결에 실패했습니다.",Toast.LENGTH_SHORT).show();
                        Log.e("GalleryFragment", "데이터 로드 실패",e);
                    });
                }

            }
        });
    }

    private void applyFilters() {
        if (adapter != null) {
            // GalleryAdapter 내부의 filter 기능을 사용
            adapter.filter(getContext(),currentSearch, currentType, currentCategory);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}