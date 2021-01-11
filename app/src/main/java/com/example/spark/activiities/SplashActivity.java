package com.example.spark.activiities;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.example.spark.R;

public class SplashActivity extends AppCompatActivity {

    private final int ANIMATION_DURATION = 3000;

    private TextView splash_TEXTVIEW_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        findView();
        showView(splash_TEXTVIEW_logo);
    }

    private void findView() {
        splash_TEXTVIEW_logo = findViewById(R.id.splash_TEXTVIEW_logo);
    }

    private void startApp() {
        Intent inent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(inent);
        finish();
    }

    public void showView(final View view) {
        view.setAlpha(0.0f);
        view.animate()
                .alpha(1.0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new LinearInterpolator())
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        view.setVisibility(View.VISIBLE);
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startApp();
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) { }
                    @Override
                    public void onAnimationRepeat(Animator animation) { }
                });
    }
}