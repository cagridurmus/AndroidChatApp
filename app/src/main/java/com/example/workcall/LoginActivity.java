package com.example.workcall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button girisButon,telefonButon;
    private EditText kullaniciMail,kullaniciSifre;
    private TextView yeniHesap,sifreUnutmaBaglantisi;


    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullaniciYol;

    //Progress Diyalog
    private ProgressDialog girisDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Kontrol Tanimlamalari
        girisButon=findViewById(R.id.giris_butonu);
        telefonButon=findViewById(R.id.telefonla_giris_butonu);

        kullaniciMail=findViewById(R.id.giris_email);
        kullaniciSifre=findViewById(R.id.giris_sifre);

        yeniHesap=findViewById(R.id.yeni_hesap_alma);
        sifreUnutmaBaglantisi=findViewById(R.id.sifre_unutma_baglantisi);

        //Firebase tanimlama
        mYetki=FirebaseAuth.getInstance();
        kullaniciYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");


        //Progress Dialog tanimlama
        girisDialog=new ProgressDialog(this);

        yeniHesap.setOnClickListener(new View.OnClickListener() { //yeni hesap alma kismina basinca bu kisimdaki fonksiyonlar yapilcak
            @Override
            public void onClick(View v) {

                Intent kayitAktivityIntent = new Intent(LoginActivity.this,KayitActivity.class); //kayit olma ekranina gidilmesini sagliyor
                startActivity(kayitAktivityIntent);
            }
        });

        girisButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    KullaniciGiris();
            }
        });

        telefonButon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent telefonOturum = new Intent(LoginActivity.this,TelefonLoginActivity.class);
                startActivity(telefonOturum);

            }
        });

    }

    private void KullaniciGiris() {
        String eMail=kullaniciMail.getText().toString();
        String sifre=kullaniciSifre.getText().toString();

        if (TextUtils.isEmpty(eMail)){
            Toast.makeText(this,"E-mail Alani bos olamaz!!1",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(sifre)){
            Toast.makeText(this,"Sifre Alani bos olamaz!!!",Toast.LENGTH_SHORT).show();
        }
        else {
            //Progess Dialog
            girisDialog.setTitle("Giris yapiliyor.");
            girisDialog.setMessage("Lutfen Bekleyiniz...");
            girisDialog.setCanceledOnTouchOutside(true);
            girisDialog.show();

            //Giris yapma
            mYetki.signInWithEmailAndPassword(eMail,sifre)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){

                                String aktifKullaniciId=mYetki.getCurrentUser().getUid();

                                //bildirim icin uniq id
                                String cihazToken = FirebaseInstanceId.getInstance().getToken();

                                //token numarasina kullanici bilgilerine ekleme
                                kullaniciYol.child(aktifKullaniciId).child("cihaz_token").setValue(cihazToken)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){

                                                    Intent anaSayfa =new Intent(LoginActivity.this,MainActivity.class);
                                                    anaSayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(anaSayfa);
                                                    finish();

                                                    Toast.makeText(LoginActivity.this, "Giris Basarili", Toast.LENGTH_SHORT).show();
                                                    girisDialog.dismiss();
                                                }
                                            }
                                        });
                            }
                            else{
                                String mesaj=task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Hata: "+ mesaj+" Hatali bir giris yaptiniz.Tekrar deneyiniz!!!", Toast.LENGTH_SHORT).show();
                                girisDialog.dismiss();
                            }

                        }
                    });
        }
    }




    private void KullaniciAnaAktivityGonder() {
        Intent anaAktiviteIntent =new Intent(LoginActivity.this,MainActivity.class);
        startActivity(anaAktiviteIntent);
    }
}