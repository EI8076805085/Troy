package com.eminence.sitasrm.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eminence.sitasrm.MainActivity;
import com.eminence.sitasrm.R;

public class IntroSliderActivity extends AppCompatActivity {
    LinearLayout ll2,ll1;
    TextView txt_skip;
    RelativeLayout txt_nextLayout,txt_continueLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_slider);

        ll2 = findViewById(R.id.ll2);
        ll1 = findViewById(R.id.ll1);
        txt_nextLayout = findViewById(R.id.txt_nextLayout);
        txt_skip = findViewById(R.id.txt_skip);
        txt_continueLayout = findViewById(R.id.txt_continueLayout);

        txt_nextLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ll1.setVisibility(View.GONE);
                ll2.setVisibility(View.VISIBLE);
            }
        });

        txt_skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroSliderActivity.this, MainActivity.class);
                intent.putExtra("goto", "home");
                startActivity(intent);
                finish();
            }
        });

        txt_continueLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroSliderActivity.this, MainActivity.class);
                intent.putExtra("goto", "home");
                startActivity(intent);
                finish();
            }
        });
    }
}