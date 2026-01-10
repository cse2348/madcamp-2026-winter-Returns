package com.example.returns;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Arrays;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // 레이아웃 단계(Step) 정의
    private LinearLayout layoutInitial, layoutRegister, layoutChecking, layoutSuccess;
    private EditText etNickname;
    private TextView tvError, tvWelcomeName;

    // 중복 체크용 가짜 데이터 (v0와 동일)
    private final List<String> EXISTING_NICKNAMES = Arrays.asList("관리자", "테스트", "user1", "admin");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 시스템 바 영역 확보 (자동 생성된 코드 수정)
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

        // 2. 닉네임 전송(중복 확인) 버튼 클릭
        findViewById(R.id.btnCheckNickname).setOnClickListener(v -> checkNickname());

        // 3. 최종 시작하기 버튼 클릭
        findViewById(R.id.btnFinalStart).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            // 2. 입력한 닉네임을 다음 화면으로 전달 (선택사항)
            String nickname = etNickname.getText().toString();
            intent.putExtra("userNickname", nickname);
            
            // 3. 화면 전환 시작
            startActivity(intent);

            // 4. 로그인 화면을 스택에서 제거 (뒤로가기 눌렀을 때 다시 로그인창 안 뜨게)
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

    private void setStep(String step) {
        // 모든 레이아웃 리스트
        View[] layouts = {layoutInitial, layoutRegister, layoutChecking, layoutSuccess};

        // 현재 활성화할 레이아웃 찾기
        View targetView;
        switch (step) {
            case "register": targetView = layoutRegister; break;
            case "checking": targetView = layoutChecking; break;
            case "success": targetView = layoutSuccess; break;
            default: targetView = layoutInitial; break;
        }

        // 1. 기존에 보이고 있던 레이아웃들은 페이드 아웃시키며 숨기기
        for (View layout : layouts) {
            if (layout.getVisibility() == View.VISIBLE && layout != targetView) {
                layout.animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(() -> layout.setVisibility(View.GONE))
                        .start();
            }
        }

        // 2. 새로운 레이아웃(targetView) 애니메이션 설정
        targetView.setVisibility(View.VISIBLE);
        targetView.setAlpha(0f); // 일단 투명하게

        if (step.equals("register")) {
            // [모드 1] 옆에서 들어오기 (Slide in from Right)
            targetView.setTranslationX(500f); // 오른쪽으로 500만큼 밀어두기
            targetView.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(400)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        } else {
            // [모드 2] 뒤에서 앞으로 (Zoom In)
            targetView.setTranslationX(0f);
            targetView.setTranslationY(0f);
            targetView.setScaleX(0.5f); // 0.5배 크기에서 시작 (뒤에 있는 느낌)
            targetView.setScaleY(0.5f);

            targetView.animate()
                    .alpha(1f)
                    .scaleX(1f) // 원래 크기로 커짐 (앞으로 오는 느낌)
                    .scaleY(1f)
                    .setDuration(500)
                    .setInterpolator(new android.view.animation.OvershootInterpolator()) // 살짝 튕김
                    .start();
        }
    }

    private void checkNickname() {
        String nickname = etNickname.getText().toString().trim();

        // 유효성 검사
        if (nickname.isEmpty()) {
            showError("닉네임을 입력해주세요");
            return;
        }
        if (nickname.length() > 8) {
            showError("닉네임은 8자 이하로 입력해주세요");
            return;
        }

        // 중복 확인 단계로 전환 (ProgressBar 시작)
        setStep("checking");
        tvError.setVisibility(View.GONE);

        // 800ms 대기 (v0 setTimeout 재현)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (EXISTING_NICKNAMES.contains(nickname.toLowerCase())) {
                setStep("register");
                showError("이미 존재하는 닉네임입니다");
            } else {
                tvWelcomeName.setText(nickname + "님");
                setStep("success");
            }
        }, 800);
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
}