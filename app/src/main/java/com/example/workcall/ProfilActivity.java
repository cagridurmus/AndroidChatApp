package com.example.workcall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilActivity extends AppCompatActivity {

    private String alinanKullaniciId,aktifDurum,aktifKullaniciId;

    private CircleImageView kullaniciProfilResmi;
    private TextView kullaniciProfil,kullaniciDurum;
    private Button mesajGonder,mesajIptal;

    //Firebase
    private DatabaseReference kullaniciYol,sohbetTalepYol,sohbetYol,bildirimYol;
    private FirebaseAuth mYetki;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        alinanKullaniciId=getIntent().getExtras().get("secilenKullaniciId").toString();

        //Toast.makeText(this, "Merhaba "+alinanKullaniciId, Toast.LENGTH_LONG).show();

        //tanimlamalar
        kullaniciProfilResmi=findViewById(R.id.profil_ziyaret);
        kullaniciProfil=findViewById(R.id.kullanici_ziyaret);
        kullaniciDurum=findViewById(R.id.profil_durum_ziyaret);
        mesajGonder=findViewById(R.id.mesaj_talebi);
        mesajIptal=findViewById(R.id.mesaj_talebi_iptal);

        aktifDurum="yeni";

        //Firebase
        kullaniciYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        sohbetTalepYol=FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        sohbetYol=FirebaseDatabase.getInstance().getReference().child("Sohbetler");
        bildirimYol=FirebaseDatabase.getInstance().getReference().child("Bildirimler");
        mYetki=FirebaseAuth.getInstance();

        aktifKullaniciId=mYetki.getCurrentUser().getUid();

        KullaniciBilgisiAl();

    }

    private void KullaniciBilgisiAl() {

        kullaniciYol.child(alinanKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if ((snapshot.exists())  && (snapshot.hasChild("resim"))){
                    //veri tabanundan verileri cekme
                    String kullaniciAdiAl = snapshot.child("isim").getValue().toString(); //veri tabaninidaki ismi alan olarak kullaniciadiala esitiliyor
                    String kullaniciDurumAl = snapshot.child("durum").getValue().toString(); //veri tabanindaki durum alan olarak kullanicidurumal esitliyor
                    String kullaniciResmiAl = snapshot.child("resim").getValue().toString();

                    //verileri kontrollere atama
                    if (kullaniciResmiAl.isEmpty()){
                        kullaniciProfilResmi.setImageResource(R.drawable.profil_resmi);
                    }
                    else {
                        Picasso.get().load(kullaniciResmiAl).into(kullaniciProfilResmi);
                    }
                    kullaniciProfil.setText(kullaniciAdiAl);
                    kullaniciDurum.setText(kullaniciDurumAl);

                    //Chat talebi gonderme metodu
                    ChatTalebleriYonet();
                }
                else {

                    //veri tabanundan verileri cekme
                    String kullaniciAdiAl = snapshot.child("isim").getValue().toString(); //veri tabaninidaki ismi alan olarak kullaniciadiala esitiliyor
                    String kullaniciDurumAl = snapshot.child("durum").getValue().toString(); //veri tabanindaki durum alan olarak kullanicidurumal esitliyor

                    //verileri kontrollere atama
                    kullaniciProfil.setText(kullaniciAdiAl);
                    kullaniciDurum.setText(kullaniciDurumAl);
                    ChatTalebleriYonet();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void ChatTalebleriYonet() {
        //talep varsa buton iptali
        sohbetTalepYol.child(aktifKullaniciId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChild(alinanKullaniciId)){

                    String talep_turu=snapshot.child(alinanKullaniciId).child("talep_turu").getValue().toString();

                    if (talep_turu.equals("gonderildi")){

                        aktifDurum="talep_gonderildi";
                        mesajGonder.setText("Mesaj Talebini Iptal Et");

                    }
                    else{
                        aktifDurum="talep_alindi";
                        mesajGonder.setText("Mesaj Talebi Kabul Et");
                        mesajIptal.setVisibility(View.VISIBLE);
                        mesajIptal.setEnabled(true);

                        mesajIptal.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                MesajTalebiIptal();
                            }
                        });
                    }
                }

                else {
                    sohbetYol.child(aktifKullaniciId)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(alinanKullaniciId)){
                                        aktifDurum="arkadaslar";
                                        mesajGonder.setText("Bu sohbeti sil");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (aktifKullaniciId.equals(alinanKullaniciId)){

            //Butonu sakla
            mesajGonder.setVisibility(View.INVISIBLE);

        }
        else {

            //mesaj talebi gitsin
            mesajGonder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mesajGonder.setEnabled(false);
                    if (aktifDurum.equals("yeni")){

                        SohbetTalebiGonder();

                    }
                    if (aktifDurum.equals("talep_gonderildi")){

                        MesajTalebiIptal();
                    }
                    if (aktifDurum.equals("talep_alindi")){

                        MesajTalebiKabul();
                    }
                    if (aktifDurum.equals("arkadaslar")){

                        OzelSohbetSil();
                    }
                }
            });
        }
    }

    private void OzelSohbetSil() {
        sohbetYol.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    //Talebi alandan sil
                    sohbetYol.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                mesajGonder.setEnabled(true);
                                aktifDurum="yeni";
                                mesajGonder.setText("Mesaj Talebi Gonder");
                                mesajGonder.setVisibility(View.INVISIBLE);
                                mesajGonder.setEnabled(false);

                            }

                        }
                    });
                }

            }
        });


    }

    private void MesajTalebiKabul() {

        sohbetYol.child(aktifKullaniciId).child(alinanKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){
                            sohbetYol.child(alinanKullaniciId).child(aktifKullaniciId).child("Sohbetler").setValue("Kaydedildi")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                sohbetTalepYol.child(aktifKullaniciId).child(alinanKullaniciId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){
                                                                    sohbetTalepYol.child(alinanKullaniciId).child(aktifKullaniciId)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    mesajGonder.setEnabled(true);
                                                                                    aktifDurum="arkadaslar";
                                                                                    mesajGonder.setText("Bu sohbeti sil");
                                                                                    mesajGonder.setVisibility(View.INVISIBLE);
                                                                                    mesajGonder.setEnabled(false);
                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void MesajTalebiIptal() {
        //Talebi gonderenden sil
        sohbetTalepYol.child(aktifKullaniciId).child(alinanKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){

                    //Talebi alandan sil
                    sohbetTalepYol.child(alinanKullaniciId).child(aktifKullaniciId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                mesajGonder.setEnabled(true);
                                aktifDurum="yeni";
                                mesajGonder.setText("Mesaj Talebi Gonder");
                                mesajGonder.setVisibility(View.INVISIBLE);
                                mesajGonder.setEnabled(false);

                            }

                        }
                    });
                }

            }
        });

    }

    private void SohbetTalebiGonder() {

        //veritabanina veri gonderme
        sohbetTalepYol.child(aktifKullaniciId).child(alinanKullaniciId).child("talep_turu").setValue("gonderildi")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            sohbetTalepYol.child(alinanKullaniciId).child(aktifKullaniciId).child("talep_turu")
                                    .setValue("alindi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()){

                                        //bildirim icin
                                        HashMap<String,String> hashMap = new HashMap<>();
                                        hashMap.put("kimden",aktifKullaniciId);
                                        hashMap.put("tur","talep");

                                        //bildirim veritabani yolu veri godnerme
                                        bildirimYol.child(alinanKullaniciId).push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    mesajGonder.setEnabled(true);
                                                    aktifDurum="talep_gonderildi";
                                                    mesajGonder.setText("Mesaj Talebi Iptal");
                                                }

                                            }
                                        });
                                    }
                                }
                            });
                        }

                    }
                });
    }
}