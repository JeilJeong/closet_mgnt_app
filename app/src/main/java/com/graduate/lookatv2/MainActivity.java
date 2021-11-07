package com.graduate.lookatv2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.graduate.lookatv2.keyfunction.BackButton;

public class MainActivity extends AppCompatActivity {
    private BackButton backButton;
    private ImageButton searchBySoundBtn;
    private ImageButton searchByTextBtn;

    @Override
    public void onBackPressed() {
        if (backButton == null) backButton = new BackButton(this);
        backButton.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_PopupOverlay);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBySoundBtn = findViewById(R.id.search_by_sound_btn);
        searchByTextBtn = findViewById(R.id.search_by_text_btn);

        searchBySoundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchBySoundActivity.class);
                startActivity(intent);
            }
        });

        searchByTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchByTextActivity.class);
                startActivity(intent);
            }
        });
    }
}