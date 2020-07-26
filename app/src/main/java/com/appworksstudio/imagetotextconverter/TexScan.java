package com.appworksstudio.imagetotextconverter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TexScan extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tex_scan);
        // inside your activity (if you did not enable transitions in your theme)

        FloatingActionButton floatingActionButton =
                (FloatingActionButton) findViewById(R.id.copy_btn);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TexScan.this, PdfCreater.class);

                startActivity(intent);

            }
        });

        FloatingActionButton texrec =
                findViewById(R.id.texrec);
        texrec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TexScan.this, MainActivity.class);

                startActivity(intent);

            }
        });
    }
}
