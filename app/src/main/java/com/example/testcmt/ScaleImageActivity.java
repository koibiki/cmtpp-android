package com.example.testcmt;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.example.view.ScaleViewGroup;

public class ScaleImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_image);
        ImageView si = findViewById(R.id.si);
        ScaleViewGroup svg = findViewById(R.id.svg);
        svg.setScalableView(si);
    }
}
