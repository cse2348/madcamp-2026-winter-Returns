package com.example.returns;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private LinearLayout layoutInitial, layoutRegister, layoutChecking, layoutSuccess;
    private EditText etNickname;
    private TextView tvError, tvWelcomeName;

    // 중복 체크용 가짜 데이터
    private final List<String> EXISTING_NICKNAMES = Arrays.asList("관리자", "테스트", "user1", "admin");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 시스템 바 영역 확보
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 뷰 초기화
        initViews();

        // 초기 화면 설정
        setStep("initial");

        // 1. 시작하기 버튼 클릭
        findViewById(R.id.btnStart).setOnClickListener(v -> setStep("register"));

        // 2. 닉네임 전송(중복 확인) 버튼 클릭 시 검사 시작
        findViewById(R.id.btnCheckNickname).setOnClickListener(v -> checkNickname());

        // 3. 최종 시작하기 버튼 클릭
        findViewById(R.id.btnFinalStart).setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();

            // 여기서도 안전을 위해 8자 체크
            if (nickname.length() > 8) {
                setStep("register");
                showError("닉네임은 8자 이하만 가능합니다.");
                return;
            }

            SharedPreferences pref = getSharedPreferences("UserToken", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("nickName", nickname);
            editor.apply();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userNickname", nickname);
            startActivity(intent);
            finish();
        });
    }

    private void initViews() {
        layoutInitial = findViewById(R.id.layoutInitial);
        layoutRegister = findViewById(R.id.layoutRegister);
        layoutChecking = findViewById(R.id.layoutChecking);
        layoutSuccess = findViewById(R.id.layoutSuccess);
        etNickname = findViewById(R.id.etNickname);
        tvError = findViewById(R.id.tvError);
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
    }


    private void checkNickname() {
        String nickname = etNickname.getText().toString().trim();

        // 1. 비어있는지 체크
        if (nickname.isEmpty()) {
            showError("닉네임을 입력해주세요.");
            shakeView(etNickname);
            return;
        }

        // 2. 글자 수 체크 (8자 넘었는지 확인)
        if (nickname.length() > 8) {
            showError("닉네임은 8자 이하로 입력해주세요. (현재 " + nickname.length() + "자)");
            shakeView(etNickname);
            return;
        }

        // 3. 글자 수가 통과되면 로딩 화면으로 전환
        setStep("checking");
        tvError.setVisibility(View.GONE);

        // 실제 중복 체크 시뮬레이션
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (EXISTING_NICKNAMES.contains(nickname.toLowerCase())) {
                setStep("register");
                showError("이미 존재하는 닉네임입니다.");
                shakeView(etNickname);
            } else {
                // 통과 시 성공 화면으로
                tvWelcomeName.setText(nickname + "님");
                setStep("success");
            }
        }, 800);
    }

    private void setStep(String step) {
        View[] layouts = {layoutInitial, layoutRegister, layoutChecking, layoutSuccess};
        View targetView;

        switch (step) {
            case "register": targetView = layoutRegister; break;
            case "checking": targetView = layoutChecking; break;
            case "success": targetView = layoutSuccess; break;
            default: targetView = layoutInitial; break;
        }

        for (View layout : layouts) {
            if (layout.getVisibility() == View.VISIBLE && layout != targetView) {
                layout.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layout.setVisibility(View.GONE))
                        .start();
            }
        }

        targetView.setVisibility(View.VISIBLE);
        targetView.setAlpha(0f);

        if (step.equals("register")) {
            targetView.setTranslationX(500f);
            targetView.animate().alpha(1f).translationX(0f).setDuration(400).start();
        } else {
            targetView.setScaleX(0.5f);
            targetView.setScaleY(0.5f);
            targetView.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).start();
        }
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setTextColor(Color.RED);
        tvError.setVisibility(View.VISIBLE);
    }

    private void shakeView(View view) {
        view.animate()
                .translationX(20f).setDuration(50)
                .withEndAction(() -> view.animate().translationX(-20f).setDuration(50)
                        .withEndAction(() -> view.animate().translationX(0f).setDuration(50).start())
                        .start())
                .start();
    }
}