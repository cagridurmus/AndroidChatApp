package com.example.workcall;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.workcall.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArkadasBulActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView arkadasBulListesi;

    //Firebase
    private DatabaseReference kullaniciYol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arkadas_bul);

        arkadasBulListesi=findViewById(R.id.arkadas_bul_listesi);
        arkadasBulListesi.setLayoutManager(new LinearLayoutManager(this));

        toolbar=findViewById(R.id.arkadas_bulma);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Arkadas Bul");

        //Firebase tanimalama
        kullaniciYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Basladiginda

        //Sorgu-secenekler
        FirebaseRecyclerOptions<Kisiler> options =
                new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(kullaniciYol,Kisiler.class)
                .build();
        FirebaseRecyclerAdapter<Kisiler,ArkadasBulViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, ArkadasBulViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ArkadasBulViewHolder arkadasBulViewHolder, int i, @NonNull Kisiler kisiler) {

                if (kisiler.getResim()==null){
                    arkadasBulViewHolder.kullaniciAd.setText(kisiler.getIsim());
                    arkadasBulViewHolder.kullaniciDurum.setText(kisiler.getDurum());

                    /*if (kisiler.getResim().isEmpty()){
                        arkadasBulViewHolder.profilResim.setImageResource(R.drawable.profil_resmi);
                    }
                    else {
                        Picasso.get().load(kisiler.getResim()).into(arkadasBulViewHolder.profilResim);
                    }*/
                    Picasso.get().load(kisiler.getResim()).into(arkadasBulViewHolder.profilResim);
                    //Tiklandiginda
                    arkadasBulViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String secilenKullaniciId = getRef(i).getKey();

                            Intent profilAktivity = new Intent(ArkadasBulActivity.this,ProfilActivity.class);
                            profilAktivity.putExtra("secilenKullaniciId",secilenKullaniciId);
                            startActivity(profilAktivity);

                        }
                    });
                }
                else {
                    arkadasBulViewHolder.kullaniciAd.setText(kisiler.getIsim());
                    arkadasBulViewHolder.kullaniciDurum.setText(kisiler.getDurum());

                    if (kisiler.getResim().isEmpty()){
                        arkadasBulViewHolder.profilResim.setImageResource(R.drawable.profil_resmi);
                    }
                    else {
                        Picasso.get().load(kisiler.getResim()).into(arkadasBulViewHolder.profilResim);
                    }
                    //Picasso.get().load(kisiler.getResim()).into(arkadasBulViewHolder.profilResim);
                    //Tiklandiginda
                    arkadasBulViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            String secilenKullaniciId = getRef(i).getKey();

                            Intent profilAktivity = new Intent(ArkadasBulActivity.this,ProfilActivity.class);
                            profilAktivity.putExtra("secilenKullaniciId",secilenKullaniciId);
                            startActivity(profilAktivity);

                        }
                    });
                }


            }

            @NonNull
            @Override
            public ArkadasBulViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanicilar_gosterme_layout,parent,false);
                ArkadasBulViewHolder viewHolder = new ArkadasBulViewHolder(view);

                return viewHolder;

            }
        };

        arkadasBulListesi.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    public static class ArkadasBulViewHolder extends RecyclerView.ViewHolder{

        TextView kullaniciAd,kullaniciDurum;
        CircleImageView profilResim;


        public ArkadasBulViewHolder(@NonNull View itemView) {
            super(itemView);


            //Tanimlamalar
            kullaniciAd=itemView.findViewById(R.id.kullanici_profil_ad);
            kullaniciDurum=itemView.findViewById(R.id.kullanici_durumu);
            profilResim=itemView.findViewById(R.id.kullanici_profili);

        }
    }
}