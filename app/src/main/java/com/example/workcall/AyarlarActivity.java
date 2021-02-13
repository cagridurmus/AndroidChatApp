package com.example.workcall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class AyarlarActivity extends AppCompatActivity {

    private Button hesapGuncel;
    private EditText kullaniciAd,kullaniciDurum;
    private CircleImageView kullaniciResim;

    private Toolbar toolbar;

    //Firebase
    private FirebaseAuth myetki;
    private DatabaseReference veriYoluuuu;
    private StorageReference kullaniciProfilYol;
    private StorageTask yuklemeGorevi;

    private String mevcutKullaniciId;

    //private static final int galeriSecim=1;

    private ProgressDialog yukleniyorBar;

    //uri
    Uri resimUri;
    String myUri="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ayarlar);

        toolbar=findViewById(R.id.ayarlar_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Hesap Ayarlari");
        //Firebase Tanimlamalari
        myetki=FirebaseAuth.getInstance();
        veriYoluuuu= FirebaseDatabase.getInstance().getReference();
        kullaniciProfilYol= FirebaseStorage.getInstance().getReference().child("Profil Resimleri");


        mevcutKullaniciId=myetki.getCurrentUser().getUid();



        //Kontrol Tanimlamalari
        hesapGuncel=findViewById(R.id.guncelleme_ayarlar);
        kullaniciAd=findViewById(R.id.kullanici_adi_ayarlar);
        kullaniciDurum=findViewById(R.id.profil_durum_ayarlar);
        kullaniciResim=findViewById(R.id.profil_resmi_ayarlar);

        //yukleniyor tanimlama
        yukleniyorBar=new ProgressDialog(this);

        hesapGuncel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        ayarlarGuncelle();

            }
        });

        //kullaniciAd.setVisibility(View.INVISIBLE);

        KullanıcıBilgisiAl();

        kullaniciResim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,galeriSecim);*/

                //Kirpma aktiivity acma

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(AyarlarActivity.this);

            }
        });
    }

    private String dosyaUzantisiAl(Uri uri) {

        ContentResolver contentResolver =getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();


        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    //resim secme kodu
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { //resim secmemiz icin imagecroper kutuphanesi eklememiz gerekiyor
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            //resim seciliyorsa
            CropImage.ActivityResult result=CropImage.getActivityResult(data);

            resimUri=result.getUri();
            kullaniciResim.setImageURI(resimUri);


        }
        else{
            Toast.makeText(this, "Resim secilemedi", Toast.LENGTH_LONG).show();
        }
    }

    private void KullanıcıBilgisiAl() {
        veriYoluuuu.child("Kullanicilar").child(mevcutKullaniciId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if ((snapshot.exists()) && (snapshot.hasChild("isim") && (snapshot.hasChild("resim")) )){
                            String kullaniciAdiAl = snapshot.child("isim").getValue().toString(); //veri tabaninidaki ismi alan olarak kullaniciadiala esitiliyor
                            String kullaniciDurumAl = snapshot.child("durum").getValue().toString(); //veri tabanindaki durum alan olarak kullanicidurumal esitliyor
                            String kullaniciResimAl = snapshot.child("resim").getValue().toString();

                            kullaniciAd.setText(kullaniciAdiAl);
                            kullaniciDurum.setText(kullaniciDurumAl);


                            if (kullaniciResimAl.isEmpty()){
                                kullaniciResim.setImageResource(R.drawable.profil_resmi);
                            }
                            else {
                                Picasso.get().load(kullaniciResimAl).into(kullaniciResim);
                            }



                        }
                        else if ((snapshot.exists()) && (snapshot.hasChild("isim"))){
                            String kullaniciAdiAl = snapshot.child("isim").getValue().toString(); //veri tabaninidaki ismi alan olarak kullaniciadiala esitiliyor
                            String kullaniciDurumAl = snapshot.child("durum").getValue().toString(); //veri tabanindaki durum alan olarak kullanicidurumal esitliyor

                            kullaniciAd.setText(kullaniciAdiAl);
                            kullaniciDurum.setText(kullaniciDurumAl);
                        }
                        else {
                            //kullaniciAd.setVisibility(View.VISIBLE);
                            Toast.makeText(AyarlarActivity.this, "Lutfen profil bilgilerinizi duzgun bir sekilde giriniz!!", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void ayarlarGuncelle() {
        String kullaniciAdi=kullaniciAd.getText().toString();
        String kullaniciDurumu=kullaniciDurum.getText().toString();

        if (TextUtils.isEmpty(kullaniciAdi)){
            Toast.makeText(this, "Lutfen isim giriniz", Toast.LENGTH_LONG).show();
        }
        if (TextUtils.isEmpty(kullaniciDurumu)){
            Toast.makeText(this, "Lutfen durum giriniz", Toast.LENGTH_LONG).show();
        }

        else{

            ResimYukle();
            
        }
    }

    private void ResimYukle() {

        yukleniyorBar.setTitle("Bilgi Aktarma");
        yukleniyorBar.setMessage("Lutfen bekleyiniz...");
        yukleniyorBar.setCanceledOnTouchOutside(false);
        yukleniyorBar.show();

        if (resimUri==null){

            DatabaseReference veriYolu =FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

            String gonderiId =veriYolu.push().getKey();

            String kullaniciAdi=kullaniciAd.getText().toString();
            String kullaniciDurumu=kullaniciDurum.getText().toString();

            HashMap<String,Object> profilHarita= new HashMap<>();
            profilHarita.put("uid",gonderiId);
            profilHarita.put("isim",kullaniciAdi);
            profilHarita.put("durum",kullaniciDurumu);


            veriYolu.child(mevcutKullaniciId).updateChildren(profilHarita);
            yukleniyorBar.dismiss();

        }
        else{

            final StorageReference resimYol = kullaniciProfilYol.child(mevcutKullaniciId+"."+dosyaUzantisiAl(resimUri));

            yuklemeGorevi =resimYol.putFile(resimUri);

            yuklemeGorevi.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if (!task.isSuccessful()){
                        throw task.getException();

                    }

                    return resimYol.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    //gorev tamamlandiginda
                    if (task.isSuccessful()){
                        //gorev basariliysa
                        Uri indirmeUri=task.getResult();
                        myUri=indirmeUri.toString();

                        DatabaseReference veriYol=FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

                        String gonderiId=veriYol.push().getKey();

                        String kullaniciAdiAl = kullaniciAd.getText().toString();
                        String kullaniciDurumAl = kullaniciDurum.getText().toString();

                        HashMap<String,Object> profilHarita= new HashMap<>();
                        profilHarita.put("uid",gonderiId);
                        profilHarita.put("isim",kullaniciAdiAl);
                        profilHarita.put("durum",kullaniciDurumAl);
                        profilHarita.put("resim",myUri);

                        veriYol.child(mevcutKullaniciId).updateChildren(profilHarita);
                        yukleniyorBar.dismiss();

                    }
                    else
                    {
                        //basarisizsa
                        String hataMesaj=task.getException().toString();
                        Toast.makeText(AyarlarActivity.this, "Hata: "+hataMesaj, Toast.LENGTH_SHORT).show();
                        yukleniyorBar.dismiss();

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(AyarlarActivity.this, "Hata: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    yukleniyorBar.dismiss();
                }
            });
        }



    }
}