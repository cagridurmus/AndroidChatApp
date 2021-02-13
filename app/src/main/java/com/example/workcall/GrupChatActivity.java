package com.example.workcall;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class GrupChatActivity extends AppCompatActivity {

    private  Toolbar toolbar;
    private ImageButton imageButton;
    private EditText editText;
    private ScrollView scrollView;
    private TextView textView;

    //intent degiskeni
    private String mevcutGrupAdi,aktifKullaniciId,aktifKullaniciAd,aktifTarih,aktifZaman;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullaniciYol,grupYol,grupAnahtariYol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grup_chat);

        //intenti alma tanimlamasi
        mevcutGrupAdi=getIntent().getExtras().get("grupAdi").toString();
        Toast.makeText(this, mevcutGrupAdi, Toast.LENGTH_LONG).show();

        //Firebase
        mYetki=FirebaseAuth.getInstance();
        aktifKullaniciId=mYetki.getCurrentUser().getUid();
        kullaniciYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        grupYol=FirebaseDatabase.getInstance().getReference().child("Gruplar").child(mevcutGrupAdi);

        //tanimlamalar
        toolbar=findViewById(R.id.grup_chat_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(mevcutGrupAdi);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        imageButton=findViewById(R.id.mesaj_gondurme_buton);
        editText=findViewById(R.id.grup_mesaj);
        scrollView=findViewById(R.id.my_Scrollview);
        textView=findViewById(R.id.grup_chat_metni);

        //Kullanici bilgisi alma
        KullaniciBilgisiAl();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MesajVeriTabaninaKaydet();

                editText.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        grupYol.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){
                    MesajlariGoster(snapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.exists()){
                    MesajlariGoster(snapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void MesajlariGoster(DataSnapshot snapshot) {

        Iterator iterator= snapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String sohbetTarih=(String)((DataSnapshot)iterator.next()).getValue();
            String sohbetMesaj=(String)((DataSnapshot)iterator.next()).getValue();
            String sohbetAd=(String)((DataSnapshot)iterator.next()).getValue();
            String sohbetZaman=(String)((DataSnapshot)iterator.next()).getValue();

            textView.append(sohbetAd+" :\n"+sohbetMesaj + "\n"+sohbetZaman+"     "+sohbetTarih +"\n\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

    private void MesajVeriTabaninaKaydet() {
        String mesaj=editText.getText().toString();
        String mesajAnahtari=grupYol.push().getKey();


        if (TextUtils.isEmpty(mesaj)){
            Toast.makeText(this, "Mesaj alani bos olamaz", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar tarih = Calendar.getInstance();
            SimpleDateFormat tarihFormat = new SimpleDateFormat("MMM dd, yyyy");
            aktifTarih=tarihFormat.format(tarih.getTime());

            Calendar zaman= Calendar.getInstance();
            SimpleDateFormat zamanFormat = new SimpleDateFormat("hh:mm:ss a");
            aktifZaman=zamanFormat.format(zaman.getTime());

            HashMap<String,Object> hashMap = new HashMap<>();
            grupYol.updateChildren(hashMap);

            grupAnahtariYol=grupYol.child(mesajAnahtari);

            HashMap<String,Object> mesajBilgiMap =new HashMap<>();
            mesajBilgiMap.put("isim",aktifKullaniciAd);
            mesajBilgiMap.put("mesaj",mesaj);
            mesajBilgiMap.put("tarih",aktifTarih);
            mesajBilgiMap.put("zaman",aktifZaman);

            grupAnahtariYol.updateChildren(mesajBilgiMap);

        }
    }

    private void KullaniciBilgisiAl() {

        kullaniciYol.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    aktifKullaniciAd=snapshot.child("isim").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}