package com.example.returns.home;

import static java.security.AccessController.getContext;

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
        Item.getAllItems(new Item.ListItemCallback(){
            @Override
            public void onSuccess(List<Item> list) {
                allItems=list;
                if (isAdded()&&getActivity() != null) {
                    getActivity().runOnUiThread(()->applyFilters());
                }
            }
            @Override
            public void onError(Exception e) {
                if (isAdded()&&getActivity() != null) {
                    Toast.makeText(getContext(),"서버 연결에 실패했습니다.",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void applyFilters() {

        String searchKeyword = (currentSearch != null) ? currentSearch.toLowerCase() : "";

        Item.queryItems(searchKeyword,currentType,currentCategory,new Item.ListItemCallback(){
            @Override
            public void onSuccess(List<Item> list) {
                filteredList.clear();
                filteredList.addAll(list);
                if(adapter!=null)adapter.notifyDataSetChanged();
                Log.e("HomeFragment", "Filtered list size: " + (list != null ? list.size() : 0));
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(getContext(),"서버 연결에 실패했습니다.",Toast.LENGTH_SHORT).show();
                Log.e("HomeFragment", "서버와 연결 실패", e);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}