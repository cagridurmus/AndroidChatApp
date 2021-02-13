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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class KayitActivity extends AppCompatActivity {

    private Button kayitButon;
    private EditText kullaniciMail,kullaniciSifre;
    private TextView hesapVar;

    //firebase
    private DatabaseReference databaseReference;
    private FirebaseAuth mYetki;

    private ProgressDialog yukleniyorDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayit);

        //Firabase
        mYetki =FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReference();

        //Kontrol tanimlamalari
        kayitButon=findViewById(R.id.kayit_butonu);

        kullaniciMail=findViewById(R.id.kayit_email);
        kullaniciSifre=findViewById(R.id.kayit_sifre);

        hesapVar=findViewById(R.id.zaten_hesap_Var);

        yukleniyorDialog=new ProgressDialog(this);

        hesapVar.setOnClickListener(new View.OnClickListener() {// hesap var textine bastinizda bu kisim gerceklesecek
            @Override
            public void onClick(View v) {

                Intent loginAktivityIntent = new Intent(KayitActivity.this,LoginActivity.class); // zaten hesabiniz var ise login sayfasini gondericek
                startActivity(loginAktivityIntent);
            }
        });

        kayitButon.setOnClickListener(new View.OnClickListener() { //kayit ol butonuna basildiginda yeni hesao olusturma metodu cagiriliyor
            @Override
            public void onClick(View v) {

                YeniHesapOlusturma();
            }
        });

    }

    private void YeniHesapOlusturma() { // yeni hesap olusturme metodu
        String eMail = kullaniciMail.getText().toString();
        String sifre = kullaniciSifre.getText().toString();

        if (TextUtils.isEmpty(eMail)){//kullani mailini girdigimiz yer bos ise uyari veriyor
            Toast.makeText(this,"E-mail Alani bos olamaz!!!",Toast.LENGTH_SHORT).show();// email alani bos olamaz uyarisi
        }
        if (TextUtils.isEmpty(sifre)){//kullani mailini girdigimiz yer bos ise uyari veriyor
            Toast.makeText(this,"Sifre Alani bos olamaz!!!",Toast.LENGTH_SHORT).show();// sifre alani bos olamaz uyarisi
        }
        else{

            yukleniyorDialog.setTitle("Yeni hesap olusturuluyor.");
            yukleniyorDialog.setMessage("Lutfen bekleyiniz...");
            yukleniyorDialog.setCanceledOnTouchOutside(true);//disari tiklanildiginda yeni hesap olusturma iptal ediliyor
            yukleniyorDialog.show();


            //Hesap olusturma kismi
            mYetki.createUserWithEmailAndPassword(eMail,sifre) //email ve sifre girildiginda alt kisim devreye giriyor
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){ //eger yukarida tanimladigimiz task basariliysa olusturuldu uyarisi vercek

                                //uniq bildirim id
                                String cihazToken= FirebaseInstanceId.getInstance().getToken();


                                String mevcutKullaniciId =mYetki.getCurrentUser().getUid();
                                databaseReference.child("Kullanicilar").child(mevcutKullaniciId).setValue("");

                                databaseReference.child("Kullanicilar").child(mevcutKullaniciId).child("cihaz_token").setValue(cihazToken);

                                Intent anasayfa=new Intent(KayitActivity.this, MainActivity.class);
                                anasayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);//geri tusuna basildiginda kayit sayfasina donmemesi icin
                                startActivity(anasayfa);
                                finish();

                                Toast.makeText(KayitActivity.this,"Yeni bir hesap olusturuldu",Toast.LENGTH_SHORT);
                                yukleniyorDialog.dismiss();
                            }
                            else{
                                String mesaj=task.getException().toString();
                                Toast.makeText(KayitActivity.this, "Hata: "+ mesaj+" Yanlis Bilgi Girdiniz.Kontrol Ediniz...", Toast.LENGTH_SHORT).show();
                                yukleniyorDialog.dismiss();
                            }
                        }
                    });

        }
    }
}