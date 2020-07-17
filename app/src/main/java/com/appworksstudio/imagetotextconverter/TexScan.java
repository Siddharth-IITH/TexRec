package com.appworksstudio.imagetotextconverter;

import androidx.appcompat.app.AppCompatActivity;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Explode;
import android.view.View;
import android.view.Window;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TexScan extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tex_scan);
        // inside your activity (if you did not enable transitions in your theme)

        FloatingActionButton floatingActionButton =
                (FloatingActionButton) findViewById(R.id.fab_scan);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(TexScan.this,PdfCreater.class);

                startActivity(intent);

            }
        });
    }
}
