package com.example.workcall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private SekmeErisimAdapter mySekmeErisimAdapter;

    //Firabase

    private FirebaseAuth mYetki;
    private DatabaseReference kullanicilarReference;

    private String aktifKullaniciId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar=findViewById(R.id.ana_sayfa_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WorkCall");

        myViewPager=findViewById(R.id.ana_sekmeler_pager);
        mySekmeErisimAdapter=new SekmeErisimAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(mySekmeErisimAdapter);

        myTabLayout=findViewById(R.id.ana_sekmeler);
        myTabLayout.setupWithViewPager(myViewPager);

        //Firebase
        mYetki=FirebaseAuth.getInstance();

        kullanicilarReference= FirebaseDatabase.getInstance().getReference();


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser mevcutKullanici=mYetki.getCurrentUser();

        if (mevcutKullanici==null){
            mYetki.signOut();
            KullaniciyiLoginGonder();
        }
        else{
            SonGorulmeGuncelle("cevrimici");
            KullaniciDogrula();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser mevcutKullanici=mYetki.getCurrentUser();

        if (mevcutKullanici !=null){
            SonGorulmeGuncelle("cevrimdisi");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser mevcutKullanici=mYetki.getCurrentUser();

        if (mevcutKullanici !=null){
            SonGorulmeGuncelle("cevrimdisi");
        }
    }

    private void KullaniciDogrula() {
        String mevcutKullanici =mYetki.getCurrentUser().getUid();

        kullanicilarReference.child("Kullanicilar").child(mevcutKullanici).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("isim").exists())){
                    Toast.makeText(MainActivity.this, "Hosgeldiniz...", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent ayarlar=new Intent(MainActivity.this,AyarlarActivity.class);
                    ayarlar.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(ayarlar);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void KullaniciyiLoginGonder() {
        Intent loginIntent =new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //sag taraftaki uc nokta gozuken sekmeyi olusturmak icin


        getMenuInflater().inflate(R.menu.secenekler_menu,menu); //secenekler menusune bagladik

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) { // sag taraftaki secenekler secildiginde yapilmasi gereken


        if (item.getItemId()==R.id.arkadas_bulma){
            Intent arkadasBul = new Intent(MainActivity.this,ArkadasBulActivity.class);
            startActivity(arkadasBul);
        }
        if (item.getItemId()==R.id.ayarlar){
            Intent ayarlar=new Intent(MainActivity.this,AyarlarActivity.class);
            startActivity(ayarlar);
        }
        if (item.getItemId()==R.id.cikis_yap){
            SonGorulmeGuncelle("cevrimdisi");
            mYetki.signOut();//cikis yapmak
            Intent giris=new Intent(MainActivity.this, LoginActivity.class);
            startActivity(giris);

        }
        if (item.getItemId()==R.id.grup_olustur){
            yeniGrupTalebi();
        }

        return super.onOptionsItemSelected(item);
    }

    private void yeniGrupTalebi() { // grup olusturma kismi
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this , R.style.AlertDialog);
        dialog.setTitle("Grup Adini Giriniz: ");

        final EditText grupAdi=new EditText(MainActivity.this);
        grupAdi.setHint("Ornek: Karantina Grubu");
        dialog.setView(grupAdi);

        dialog.setPositiveButton("Olustur", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String grupAdiAl=grupAdi.getText().toString();
                
                if (grupAdiAl.isEmpty()){
                    Toast.makeText(MainActivity.this, "Lutfen grup adi giriniz!!!", Toast.LENGTH_LONG).show();
                }
                else{
                    YeniGrupOlustur(grupAdiAl);
                }
            }
        });
        
        dialog.setNegativeButton("Iptal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = dialog.create();
        alertDialog.show();



    }

    private void YeniGrupOlustur(String grupAdi) {

        kullanicilarReference.child("Gruplar").child(grupAdi).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, grupAdi+" adli grup olusturuldu.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void SonGorulmeGuncelle(String durum){

        String kaktifZaman,kaktifTarih;

        Calendar calendar = Calendar.getInstance();

        //tarih format
        SimpleDateFormat aktifTarih = new SimpleDateFormat("MMM dd,yyy");
        kaktifTarih=aktifTarih.format(calendar.getTime());

        SimpleDateFormat aktifZaman = new SimpleDateFormat("hh:mm:ss a");
        kaktifZaman=aktifZaman.format(calendar.getTime());

        HashMap<String,Object> cevrimiciDurumMap = new HashMap<>();
        cevrimiciDurumMap.put("zaman",kaktifZaman);
        cevrimiciDurumMap.put("tarih",kaktifTarih);
        cevrimiciDurumMap.put("durum",durum);

        aktifKullaniciId=mYetki.getCurrentUser().getUid();


        kullanicilarReference.child("Kullanicilar").child(aktifKullaniciId).child("kullaniciDurum").updateChildren(cevrimiciDurumMap);

    }

}