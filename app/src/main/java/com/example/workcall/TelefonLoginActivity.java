package com.example.workcall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

public class TelefonLoginActivity extends AppCompatActivity {

    private Button dogrulamaKodButton,dogrulaButton;
    private EditText telefonNumarasi,dogrulamaKodu;

    //telefon dogrulama
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks  callbacks;
    private String mDogrulamaId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullaniciYol;

    //yukleniyor penceresi
    private ProgressDialog yuklemeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telefon_login);

        dogrulamaKodButton=findViewById(R.id.dogrulama_kodu_Gonder);
        dogrulaButton=findViewById(R.id.dogrulama_butonu);

        telefonNumarasi=findViewById(R.id.telefon_numara);
        dogrulamaKodu=findViewById(R.id.dogrulama_kodu);

        //Firebase tanimlamasi
        mYetki=FirebaseAuth.getInstance();
        kullaniciYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        //Progress dialog taninlamasi
        yuklemeBar=new ProgressDialog(this);


        dogrulamaKodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String telefonNumara = telefonNumarasi.getText().toString();
                if (TextUtils.isEmpty(telefonNumara)){
                    Toast.makeText(TelefonLoginActivity.this, "Telefon numarasi bos olamaz", Toast.LENGTH_LONG).show();
                }
                else {

                    //yukleniyor pencerei
                    yuklemeBar.setTitle("Kodla Dogrulama");
                    yuklemeBar.setMessage("Lutfen Bekleyin...");
                    yuklemeBar.setCanceledOnTouchOutside(false);
                    yuklemeBar.show();

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(mYetki)
                                    .setPhoneNumber(telefonNumara)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(TelefonLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }
            }
        });

        dogrulaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Gorunurluk ayarlamasi
                dogrulamaKodButton.setVisibility(View.INVISIBLE);
                telefonNumarasi.setVisibility(View.INVISIBLE);

                String dogrulamaKod = dogrulamaKodu.getText().toString();

                if (TextUtils.isEmpty(dogrulamaKod)){
                    Toast.makeText(TelefonLoginActivity.this, "Dogrulama kod bos olamaz!", Toast.LENGTH_LONG).show();
                }
                else{

                    //yukleniyor pencerei
                    yuklemeBar.setTitle("Telefonla Dogrulama");
                    yuklemeBar.setMessage("Lutfen Bekleyin...");
                    yuklemeBar.setCanceledOnTouchOutside(false);
                    yuklemeBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mDogrulamaId, dogrulamaKod); //telefn yetki kimligi
                    TelefonGirisYap(credential);
                }
            }
        });

        callbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                TelefonGirisYap(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                //yukleme penceresi
                yuklemeBar.dismiss();

                Toast.makeText(TelefonLoginActivity.this, "Gecersiz telefon numarasi giidiniz.\nTekrar Deneyiniz!", Toast.LENGTH_LONG).show();

                //Gorunurluk ayarlamasi
                dogrulamaKodButton.setVisibility(View.VISIBLE);
                dogrulaButton.setVisibility(View.INVISIBLE);
                telefonNumarasi.setVisibility(View.VISIBLE);
                dogrulamaKodu.setVisibility(View.INVISIBLE);

            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mDogrulamaId = verificationId;
                mResendToken = token;

                //yukleme penceresi
                yuklemeBar.dismiss();

                Toast.makeText(TelefonLoginActivity.this, "Kod Gonderildi", Toast.LENGTH_LONG).show();

                //Gorunurluk ayarlamasi
                dogrulamaKodButton.setVisibility(View.INVISIBLE);
                dogrulaButton.setVisibility(View.VISIBLE);
                telefonNumarasi.setVisibility(View.INVISIBLE);
                dogrulamaKodu.setVisibility(View.VISIBLE);

            }

        };

    }

    private void TelefonGirisYap(PhoneAuthCredential credential) {
        mYetki.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String cihazToken = FirebaseInstanceId.getInstance().getToken();

                            String aktifKullaniciId=mYetki.getCurrentUser().getUid();

                            kullaniciYol.child(aktifKullaniciId).child("cihaz_token").setValue(cihazToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            yuklemeBar.dismiss();
                                            Toast.makeText(TelefonLoginActivity.this, "Oturum acma basariyla tamanlandi.", Toast.LENGTH_LONG).show();
                                            KullaniciAnaSayfaGonder();
                                        }
                                    });

                        }
                        else {

                            String hataMesaj= task.getException().toString();

                            Toast.makeText(TelefonLoginActivity.this, "Hata: "+hataMesaj, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void KullaniciAnaSayfaGonder() {

        Intent anasayfa= new Intent(TelefonLoginActivity.this,MainActivity.class);
        anasayfa.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(anasayfa);
        finish();
    }

}