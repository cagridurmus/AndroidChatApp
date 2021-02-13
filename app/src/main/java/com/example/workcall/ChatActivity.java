package com.example.workcall;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workcall.Adapter.MesajAdapter;
import com.example.workcall.Model.Mesajlar;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView kullaniciAd,kullaniciSonGorulme;
    private CircleImageView kullaniciResmi;
    private ImageView sohbeteOk;
    private EditText girilenMesajMetin;

    private ImageButton mesajGonderBtn,dosyaGonderBtn;

    private String idMesajAl,adMesajAl,resimMesajAl,idMesajGonderen;

    private Toolbar toolbar;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference mesajYol,kullaniciYol;

    private final List<Mesajlar> mesajlarList = new ArrayList<>();

    private LinearLayoutManager layoutManager;
    private MesajAdapter mesajAdapter;
    private RecyclerView kullaniciMesajListe;

    private String kaktifZaman,kaktifTarih;
    private String kontrol="",myUrl="";
    private StorageTask yuklemeGorev;
    private Uri dosyaUri;

    //progress
    private ProgressDialog yuklemeBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //Firebase
        mYetki=FirebaseAuth.getInstance();
        idMesajGonderen=mYetki.getCurrentUser().getUid();
        mesajYol= FirebaseDatabase.getInstance().getReference();

        //chat fragment gelen intent al
        idMesajAl=getIntent().getExtras().get("kullanici_id_ziyaret").toString();
        adMesajAl=getIntent().getExtras().get("kullanici_ad_ziyaret").toString();
        resimMesajAl=getIntent().getExtras().get("resim_ziyaret").toString();

        kullaniciAd=findViewById(R.id.kullanici_Adi_gosterme);
        kullaniciSonGorulme=findViewById(R.id.kullanici_durumu_gosterme);
        kullaniciResmi=findViewById(R.id.kullanici_resmi_gosterme);
        sohbeteOk=findViewById(R.id.sohbete_gonderme);
        mesajGonderBtn=findViewById(R.id.mesaj_gonderme_btn);
        girilenMesajMetin=findViewById(R.id.girilen_mesaj);
        dosyaGonderBtn=findViewById(R.id.dosya_gonderme_btn);

        mesajAdapter= new MesajAdapter(mesajlarList);
        kullaniciMesajListe=findViewById(R.id.kullanici_mesajlari);
        layoutManager=new LinearLayoutManager(this);
        kullaniciMesajListe.setLayoutManager(layoutManager);
        kullaniciMesajListe.setAdapter(mesajAdapter);

        kullaniciAd.setText(adMesajAl);

        if (resimMesajAl.isEmpty()){
            kullaniciResmi.setImageResource(R.drawable.profil_resmi);
        }
        else {
            Picasso.get().load(resimMesajAl).into(kullaniciResmi);
        }

        mesajGonderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MesajGonderme();

            }
        });



        yuklemeBar=new ProgressDialog(this);

        //takvim
        Calendar calendar = Calendar.getInstance();

        //tarih format
        SimpleDateFormat aktifTarih = new SimpleDateFormat("MMM dd,yyy");
        kaktifTarih=aktifTarih.format(calendar.getTime());
        //saat format
        SimpleDateFormat aktifZaman = new SimpleDateFormat("hh:mm:ss a");
        kaktifZaman=aktifZaman.format(calendar.getTime());


        kullaniciYol= FirebaseDatabase.getInstance().getReference();

        sohbeteOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });





        dosyaGonderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {"Resimler",
                         "PDF",
                         "Word"};

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Dosya Sec");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (which==0){
                            kontrol="resim";
                            //Galeri acma
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Resim Seciniz"),438);

                        }
                        if (which==1){
                            kontrol="pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Pdf Dosyasi Seciniz"),438);
                        }
                        if (which==2){
                            kontrol="docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Word Dosyasi Seciniz"),438);

                        }
                    }
                });

                builder.show();
            }
        });

        SonGorulmeGoster();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data != null && data.getData() != null){

            //progress
            yuklemeBar.setTitle("Dosya gonderiliyor");
            yuklemeBar.setMessage("Lutfen bekleyiniz...");
            yuklemeBar.setCanceledOnTouchOutside(false);
            yuklemeBar.show();

            dosyaUri=data.getData();

            if (!kontrol.equals("resim")){
                StorageReference depolamaYol= FirebaseStorage.getInstance().getReference().child("Dokuman Dosyalari");

                String mesajGonderenYol = "Mesajlar/" +idMesajGonderen +"/" +idMesajAl;
                String mesajAlanYol = "Mesajlar/" +idMesajAl +"/" +idMesajGonderen;

                DatabaseReference mesajAnahtarYol = mesajYol.child("Mesajlar").child(idMesajGonderen).child(idMesajAl).push();

                final String mesajEkleId = mesajAnahtarYol.getKey();

                final StorageReference dosyaYol= depolamaYol.child(mesajEkleId +"."+kontrol);

                yuklemeGorev=dosyaYol.putFile(dosyaUri);
                yuklemeGorev.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return dosyaYol.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri indirmeUrl = task.getResult();
                            myUrl=indirmeUrl.toString();

                            HashMap mesajMetni = new HashMap();
                            mesajMetni.put("mesaj",myUrl);
                            mesajMetni.put("isim",dosyaUri.getLastPathSegment());
                            mesajMetni.put("tur",kontrol);
                            mesajMetni.put("kimden",idMesajGonderen);
                            mesajMetni.put("kime",idMesajAl);
                            mesajMetni.put("mesajId",mesajEkleId);
                            mesajMetni.put("zaman",kaktifZaman);
                            mesajMetni.put("tarih",kaktifTarih);

                            HashMap mesajGovdeDetay = new HashMap();
                            mesajGovdeDetay.put(mesajGonderenYol + "/" + mesajEkleId , mesajMetni);
                            mesajGovdeDetay.put(mesajAlanYol + "/" + mesajEkleId , mesajMetni);

                            mesajYol.updateChildren(mesajGovdeDetay).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj gonderildi", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj gonderme hatali", Toast.LENGTH_SHORT).show();
                                    }
                                    girilenMesajMetin.setText("");

                                }
                            });
                        }

                    }
                });

            }
            else if (kontrol.equals("resim")){
                StorageReference depolamaYol= FirebaseStorage.getInstance().getReference().child("Resim Dosyalari");

                String mesajGonderenYol = "Mesajlar/" +idMesajGonderen +"/" +idMesajAl;
                String mesajAlanYol = "Mesajlar/" +idMesajAl +"/" +idMesajGonderen;

                DatabaseReference mesajAnahtarYol = mesajYol.child("Mesajlar").child(idMesajGonderen).child(idMesajAl).push();

                final String mesajEkleId = mesajAnahtarYol.getKey();

                final StorageReference dosyaYol= depolamaYol.child(mesajEkleId +"."+"jpg");

                yuklemeGorev=dosyaYol.putFile(dosyaUri);
                yuklemeGorev.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        return dosyaYol.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri indirmeUrl = task.getResult();
                            myUrl=indirmeUrl.toString();

                            HashMap mesajMetni = new HashMap();
                            mesajMetni.put("mesaj",myUrl);
                            mesajMetni.put("isim",dosyaUri.getLastPathSegment());
                            mesajMetni.put("tur",kontrol);
                            mesajMetni.put("kimden",idMesajGonderen);
                            mesajMetni.put("kime",idMesajAl);
                            mesajMetni.put("mesajId",mesajEkleId);
                            mesajMetni.put("zaman",kaktifZaman);
                            mesajMetni.put("tarih",kaktifTarih);

                            HashMap mesajGovdeDetay = new HashMap();
                            mesajGovdeDetay.put(mesajGonderenYol + "/" + mesajEkleId , mesajMetni);
                            mesajGovdeDetay.put(mesajAlanYol + "/" + mesajEkleId , mesajMetni);

                            mesajYol.updateChildren(mesajGovdeDetay).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj gonderildi", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        yuklemeBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Mesaj gonderme hatali", Toast.LENGTH_SHORT).show();
                                    }
                                    girilenMesajMetin.setText("");

                                }
                            });
                        }

                    }
                });


            }
            else {
                yuklemeBar.dismiss();
                String hataMesaj="Oge secilemedi";
                Toast.makeText(this, "Hata: "+hataMesaj, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void SonGorulmeGoster(){
       kullaniciYol.child("Kullanicilar").child(idMesajAl).addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {

               if (snapshot.child("kullaniciDurum").hasChild("durum")){
                   String durum=snapshot.child("kullaniciDurum").child("durum").getValue().toString();
                   String tarih=snapshot.child("kullaniciDurum").child("tarih").getValue().toString();
                   String zaman=snapshot.child("kullaniciDurum").child("zaman").getValue().toString();

                   if (durum.equals("cevrimici")){
                       kullaniciSonGorulme.setText("cevrimici");
                   }
                   else if (durum.equals("cevrimdisi")){
                       kullaniciSonGorulme.setText("Son Gorulme: "+tarih+" "+zaman);
                   }
               }
               else {
                   kullaniciSonGorulme.setText("cevrimdisi");
               }

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

    }

    @Override
    protected void onStart() {
        super.onStart();

        mesajYol.child("Mesajlar").child(idMesajGonderen).child(idMesajAl)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Mesajlar mesajlar= snapshot.getValue(Mesajlar.class);

                        mesajlarList.add(mesajlar);
                        mesajAdapter.notifyDataSetChanged();

                        kullaniciMesajListe.smoothScrollToPosition(kullaniciMesajListe.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

    private void MesajGonderme() {
        String mesajMetin = girilenMesajMetin.getText().toString();

        if (TextUtils.isEmpty(mesajMetin)){
            Toast.makeText(this, "Bos mesaj gonderiyorsunuz!", Toast.LENGTH_SHORT).show();
        }
        else {
            String mesajGonderenYol = "Mesajlar/" +idMesajGonderen +"/" +idMesajAl;
            String mesajAlanYol = "Mesajlar/" +idMesajAl +"/" +idMesajGonderen;

            DatabaseReference mesajAnahtarYol = mesajYol.child("Mesajlar").child(idMesajGonderen).child(idMesajAl).push();

            String mesajEkleId = mesajAnahtarYol.getKey();

            HashMap mesajMetni = new HashMap();
            mesajMetni.put("mesaj",mesajMetin);
            mesajMetni.put("tur","metin");
            mesajMetni.put("kimden",idMesajGonderen);
            mesajMetni.put("kime",idMesajAl);
            mesajMetni.put("mesajId",mesajEkleId);
            mesajMetni.put("zaman",kaktifZaman);
            mesajMetni.put("tarih",kaktifTarih);

            HashMap mesajGovdeDetay = new HashMap();
            mesajGovdeDetay.put(mesajGonderenYol + "/" + mesajEkleId , mesajMetni);
            mesajGovdeDetay.put(mesajAlanYol + "/" + mesajEkleId , mesajMetni);

            mesajYol.updateChildren(mesajGovdeDetay).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        yuklemeBar.dismiss();

                        Toast.makeText(ChatActivity.this, "Mesaj gonderildi", Toast.LENGTH_LONG).show();
                    }
                    else {
                        yuklemeBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Mesaj gonderme hatali", Toast.LENGTH_SHORT).show();
                    }
                    girilenMesajMetin.setText("");

                }
            });

        }

    }
}