package com.example.workcall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ResimGosterActivity extends AppCompatActivity {
    private ImageView imageView;
    private String resimUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resim_goster);

        imageView.findViewById(R.id.resim_goruntule);
        resimUrl=getIntent().getStringExtra("url");

        Picasso.get().load(resimUrl).into(imageView);

        /*if (resimUrl.isEmpty()){
            imageView.setImageResource(R.drawable.ic_baseline_insert_drive_file_24);
        }
        else {
            Picasso.get().load(resimUrl).into(imageView);
        }*/
    }
}