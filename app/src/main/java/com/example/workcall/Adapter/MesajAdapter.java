package com.example.workcall.Adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workcall.ChatActivity;
import com.example.workcall.MainActivity;
import com.example.workcall.Model.Mesajlar;
import com.example.workcall.R;
import com.example.workcall.ResimGosterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MesajAdapter extends RecyclerView.Adapter<MesajAdapter.MesajViewHolder> {

    private List<Mesajlar> kullaniciMesajListesi;

    //Firebase
    private FirebaseAuth mYetki;
    private DatabaseReference kullanicilarYol;

    public MesajAdapter(List<Mesajlar> kullaniciMesajListesi){
        this.kullaniciMesajListesi=kullaniciMesajListesi;
    }

    public class MesajViewHolder extends RecyclerView.ViewHolder{

        public TextView gonderenMesajMetin,aliciMesajMetin;
        public CircleImageView aliciProfilResmi;
        public ImageView gonderenMesajResim,alanMesajResim;

        public MesajViewHolder(@NonNull View itemView) {
            super(itemView);

            gonderenMesajMetin=itemView.findViewById(R.id.gonderen_mesaj_metni);
            aliciMesajMetin=itemView.findViewById(R.id.alici_mesaj_metni);
            aliciProfilResmi=itemView.findViewById(R.id.mesaj_profil_resmi);
            gonderenMesajResim=itemView.findViewById(R.id.gonderen_tarih);
            alanMesajResim=itemView.findViewById(R.id.alan_tarih);
        }
    }

    @NonNull
    @Override
    public MesajViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ozel_mesaj_layout,parent,false);

        mYetki=FirebaseAuth.getInstance();

        return new MesajViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MesajViewHolder holder, final int position) {
        String mesajGonderenId=mYetki.getCurrentUser().getUid();

        Mesajlar mesajlar = kullaniciMesajListesi.get(position);

        String kKullaniciId = mesajlar.getKimden();
        String kMesajTur = mesajlar.getTur();

        kullanicilarYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar").child(kKullaniciId);

        kullanicilarYol.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("resim")){
                    String profilResmiAl=snapshot.child("resim").getValue().toString();

                    if (profilResmiAl.isEmpty()){
                        holder.aliciProfilResmi.setImageResource(R.drawable.profil_resmi);
                    }
                    else {
                        Picasso.get().load(profilResmiAl).into(holder.aliciProfilResmi);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.aliciMesajMetin.setVisibility(View.GONE);
        holder.aliciProfilResmi.setVisibility(View.GONE);
        holder.gonderenMesajMetin.setVisibility(View.GONE);
        holder.gonderenMesajResim.setVisibility(View.GONE);
        holder.alanMesajResim.setVisibility(View.GONE);

        if (kMesajTur.equals("metin")){
            if (kKullaniciId.equals(mesajGonderenId)){
                holder.gonderenMesajMetin.setVisibility(View.VISIBLE);

                holder.gonderenMesajMetin.setBackgroundResource(R.drawable.gonderen_mesaj_layout);
                holder.gonderenMesajMetin.setTextColor(Color.BLACK);
                holder.gonderenMesajMetin.setText(mesajlar.getMesaj()+"\n\n" +mesajlar.getZaman()+"-" + mesajlar.getTarih());
            }
            else {


                holder.aliciProfilResmi.setVisibility(View.VISIBLE);
                holder.aliciMesajMetin.setVisibility(View.VISIBLE);

                holder.aliciMesajMetin.setBackgroundResource(R.drawable.alici_mesaj_layout);
                holder.aliciMesajMetin.setTextColor(Color.BLACK);
                holder.aliciMesajMetin.setText(mesajlar.getMesaj()+"\n\n" +mesajlar.getZaman()+"-" + mesajlar.getTarih());
            }
        }

        else if(kMesajTur.equals("resim")){
            if (kKullaniciId.equals(mesajGonderenId)){
                holder.gonderenMesajResim.setVisibility(View.VISIBLE);

                if (mesajlar.getMesaj().isEmpty()){
                    holder.gonderenMesajResim.setImageResource(R.drawable.profil_resmi);
                }
                else {
                    Picasso.get().load(mesajlar.getMesaj()).into(holder.gonderenMesajResim);
                }
            }
            else{
                holder.aliciProfilResmi.setVisibility(View.VISIBLE);
                holder.alanMesajResim.setVisibility(View.VISIBLE);

                if (mesajlar.getMesaj().isEmpty()){
                    holder.alanMesajResim.setImageResource(R.drawable.profil_resmi);
                }
                else {
                    Picasso.get().load(mesajlar.getMesaj()).into(holder.alanMesajResim);
                }
            }
        }
        else if ((kMesajTur.equals("pdf")) || (kMesajTur.equals("docx")) ){
            if (kKullaniciId.equals(mesajGonderenId)){
                holder.gonderenMesajResim.setVisibility(View.VISIBLE);

                //holder.gonderenMesajResim.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);

                Picasso.get().load(R.drawable.ic_baseline_insert_drive_file_24).into(holder.gonderenMesajResim);


            }
            else {
                holder.aliciProfilResmi.setVisibility(View.VISIBLE);
                holder.alanMesajResim.setVisibility(View.VISIBLE);

                //holder.alanMesajResim.setBackgroundResource(R.drawable.ic_baseline_insert_drive_file_24);

                Picasso.get().load(R.drawable.ic_baseline_insert_drive_file_24).into(holder.alanMesajResim);

            }

        }

        if (kKullaniciId.equals(mesajGonderenId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (kullaniciMesajListesi.get(position).getTur().equals("pdf") || kullaniciMesajListesi.get(position).getTur().equals("docx")){

                        CharSequence options [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu belgeyi indir ve goruntule",
                                        "Iptal et",
                                        "Herkesten sil"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    //benden sil
                                    GonderilenMesajSil(position,holder);


                                }
                                else if (which==1){
                                    //indir goruntule
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kullaniciMesajListesi.get(position).getMesaj()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which==2){
                                    //iptal
                                    Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which==3){
                                    //herkesten sil
                                    MesajHerkestenSil(position,holder);

                                }

                            }
                        });
                        builder.show();

                    }
                    else if (kullaniciMesajListesi.get(position).getTur().equals("metin")){

                        CharSequence options [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Iptal",
                                        "Herkesten sil"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    //benden sil
                                    GonderilenMesajSil(position,holder);

                                }
                                else if (which==1){
                                    //iptal
                                    Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which==2){
                                    //herkesten sil
                                    MesajHerkestenSil(position,holder);


                                }

                            }
                        });
                        builder.show();
                    }
                    else if (kullaniciMesajListesi.get(position).getTur().equals("resim")){

                        CharSequence options [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu resmi goruntule",
                                        "Iptal",
                                        "Herkesten sil"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    //benden sil
                                    GonderilenMesajSil(position,holder);

                                }
                                else if (which==1){
                                    //goruntule
                                    Intent intent = new Intent(holder.itemView.getContext(), ResimGosterActivity.class);
                                    intent.putExtra("url",kullaniciMesajListesi.get(position).getMesaj());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which==2){
                                    //iptal
                                    Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which==3){
                                    //herkesten sil
                                    MesajHerkestenSil(position,holder);
                                }
                            }
                        });
                        builder.show();
                    }

                }
            });
        }
        else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (kullaniciMesajListesi.get(position).getTur().equals("pdf") || kullaniciMesajListesi.get(position).getTur().equals("docx")){

                        CharSequence options [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu belgeyi indir ve goruntule",
                                        "Iptal et"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    //benden sil
                                    GonderilenMesajSil(position,holder);
                                }
                                else if (which==1){
                                    //indir goruntule
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kullaniciMesajListesi.get(position).getMesaj()));
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which==2){
                                    //iptal
                                    Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();

                    }
                    else if (kullaniciMesajListesi.get(position).getTur().equals("metin")){

                        CharSequence options [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Iptal"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    //benden sil
                                    GonderilenMesajSil(position,holder);

                                }
                                else if (which==1){
                                    //iptal
                                    Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }
                    else if (kullaniciMesajListesi.get(position).getTur().equals("resim")){

                        CharSequence options [] = new CharSequence[]
                                {
                                        "Benden Sil",
                                        "Bu resmi goruntule",
                                        "Iptal"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Mesaj Silinsin Mi?");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which==0){
                                    //benden sil
                                    GonderilenMesajSil(position,holder);


                                }
                                else if (which==1){
                                    //iptal
                                    Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                                    holder.itemView.getContext().startActivity(intent);

                                }
                                else if (which==2){
                                    //goruntule
                                    Intent intent = new Intent(holder.itemView.getContext(), ResimGosterActivity.class);
                                    intent.putExtra("url",kullaniciMesajListesi.get(position).getMesaj());
                                    holder.itemView.getContext().startActivity(intent);

                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }



    @Override
    public int getItemCount() {
        return kullaniciMesajListesi.size();
    }

    private void GonderilenMesajSil(final int pozisyon,final MesajViewHolder holder){

        DatabaseReference mesajYol = FirebaseDatabase.getInstance().getReference();
        mesajYol.child("Mesajlar").child(kullaniciMesajListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajListesi.get(pozisyon).getKime())
                .child(kullaniciMesajListesi.get(pozisyon).getMesajId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Silme islemi basariyla gerceklestirildi", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Silme islemi gerceklestirilemedi.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void AlinanMesajSil(final int pozisyon,final MesajViewHolder holder){

        DatabaseReference mesajYol = FirebaseDatabase.getInstance().getReference();
        mesajYol.child("Mesajlar").child(kullaniciMesajListesi.get(pozisyon).getKime())
                .child(kullaniciMesajListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajListesi.get(pozisyon).getMesajId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Silme islemi basariyla gerceklestirildi", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Silme islemi gerceklestirilemedi.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void MesajHerkestenSil(final int pozisyon,final MesajViewHolder holder){

        final DatabaseReference mesajYol = FirebaseDatabase.getInstance().getReference();
        mesajYol.child("Mesajlar").child(kullaniciMesajListesi.get(pozisyon).getKime())
                .child(kullaniciMesajListesi.get(pozisyon).getKimden())
                .child(kullaniciMesajListesi.get(pozisyon).getMesajId())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()){
                    mesajYol.child("Mesajlar").child(kullaniciMesajListesi.get(pozisyon).getKimden())
                            .child(kullaniciMesajListesi.get(pozisyon).getKime())
                            .child(kullaniciMesajListesi.get(pozisyon).getMesajId())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(holder.itemView.getContext(), "Silme islemi basariyla gerceklestirildi", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else {
                    Toast.makeText(holder.itemView.getContext(), "Silme islemi gerceklestirilemedi.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
