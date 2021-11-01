package com.graduate.lookatv2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.graduate.lookatv2.keyfunction.BackButton;

public class MainActivity extends AppCompatActivity {
    private ImageButton searchBtn;
    private ImageButton addBtn;

    private ImageButton archiveBtn;
    private ImageButton homeBtn;
    private ImageButton profileBtn;

    private com.graduate.lookatv2.HomeFragment homeFragment;
    private ArchiveFragment archiveFragment;
    private com.graduate.lookatv2.ProfileFragment profileFragment;
    private FragmentManager manager;

    private BackButton backButton;

    static {

    }
    @Override
    public void onBackPressed() {
        if (backButton == null) backButton = new BackButton(this);
        backButton.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBtn = findViewById(R.id.searchButton);
        addBtn = findViewById(R.id.addButton);

        archiveBtn = findViewById(R.id.naviArchiveButton);
        homeBtn = findViewById(R.id.naviHomeButton);
        profileBtn = findViewById(R.id.naviProfileButton);

        archiveFragment = new ArchiveFragment();
        homeFragment = new com.graduate.lookatv2.HomeFragment();
        profileFragment = new com.graduate.lookatv2.ProfileFragment();

//      First loaded fragment
        manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container_view, homeFragment);
        transaction.commit();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), com.graduate.lookatv2.SearchActivity.class);
                startActivity(intent);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), com.graduate.lookatv2.camview.EdgeDetectionActivity.class);
//                startActivity(intent);
            }
        });

        archiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navIconSelected(0);
                onFragmentChanged(0);
            }
        });
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navIconSelected(1);
                onFragmentChanged(1);
            }
        });
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navIconSelected(2);
                onFragmentChanged(2);
            }
        });
    }

    public void navIconSelected(int index) {
        if (index == 0) {
            archiveBtn.setImageResource(R.drawable.baseline_inventory_24);
            homeBtn.setImageResource(R.drawable.outline_home_24);
            profileBtn.setImageResource(R.drawable.outline_person_outline_24);
        }
        else if (index == 1) {
            archiveBtn.setImageResource(R.drawable.outline_inventory_2_20);
            homeBtn.setImageResource(R.drawable.baseline_home_20);
            profileBtn.setImageResource(R.drawable.outline_person_outline_24);
        }
        else if (index == 2) {
            archiveBtn.setImageResource(R.drawable.outline_inventory_2_20);
            homeBtn.setImageResource(R.drawable.outline_home_24);
            profileBtn.setImageResource(R.drawable.ic_baseline_person_24);
        }
    }

    public void onFragmentChanged(int index) {
        FragmentTransaction transaction = manager.beginTransaction();
        if (index == 0) {
            transaction.replace(R.id.fragment_container_view, archiveFragment);
        }
        else if (index == 1) {
            transaction.replace(R.id.fragment_container_view, homeFragment);
        }
        else if (index == 2) {
            transaction.replace(R.id.fragment_container_view, profileFragment);
        }
        transaction.commit();
    }
}