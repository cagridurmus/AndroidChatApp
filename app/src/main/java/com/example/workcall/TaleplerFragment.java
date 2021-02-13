package com.example.workcall;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workcall.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaleplerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaleplerFragment extends Fragment {

    private View taleplerView;

    private RecyclerView taleplerListem;

    //firebase
    private DatabaseReference talepYol,kullanicilarYol,sohbetlerYol;
    private FirebaseAuth mYetki;

    private String aktifKullaniciId;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public TaleplerFragment() {
        // Required empty public constructor
    }


    public static TaleplerFragment newInstance(String param1, String param2) {
        TaleplerFragment fragment = new TaleplerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        taleplerView = inflater.inflate(R.layout.fragment_talepler, container, false);



        //firebase
        mYetki=FirebaseAuth.getInstance();
        aktifKullaniciId=mYetki.getCurrentUser().getUid();
        talepYol= FirebaseDatabase.getInstance().getReference().child("Sohbet Talebi");
        kullanicilarYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");
        sohbetlerYol= FirebaseDatabase.getInstance().getReference().child("Sohbetler");

        taleplerListem = taleplerView.findViewById(R.id.sohbet_listesi);
        taleplerListem.setLayoutManager(new LinearLayoutManager(getContext()));
        return taleplerView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Kisiler> options = new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(talepYol.child(aktifKullaniciId),Kisiler.class)
                .build();

        FirebaseRecyclerAdapter<Kisiler,TalepViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, TalepViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final TalepViewHolder talepViewHolder, int i, @NonNull Kisiler kisiler) {
                talepViewHolder.itemView.findViewById(R.id.talep_kabul_buton).setVisibility(View.VISIBLE);
                talepViewHolder.itemView.findViewById(R.id.talep_iptal_buton).setVisibility(View.VISIBLE);

                final String kullaniciIdListesi =getRef(i).getKey();

                DatabaseReference talepTuruAl =getRef(i).child("talep_turu").getRef();

                talepTuruAl.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String tur=snapshot.getValue().toString();

                            if (tur.equals("alindi")){
                                kullanicilarYol.child(kullaniciIdListesi).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("resim")){

                                            final String talepProfilResim =snapshot.child("resim").getValue().toString();

                                            /*if (talepProfilResim.isEmpty()){
                                                talepViewHolder.profilResmi.setImageResource(R.drawable.profil_resmi);
                                            }
                                            else {
                                                Picasso.get().load(talepProfilResim).into(talepViewHolder.profilResmi);
                                            }*/

                                            Picasso.get().load(talepProfilResim).into(talepViewHolder.profilResmi);
                                        }

                                            final String talepKullaniciAd =snapshot.child("isim").getValue().toString();
                                            final String talepKullaniciDurum =snapshot.child("durum").getValue().toString();

                                            talepViewHolder.kullaniciAd.setText(talepKullaniciAd);
                                            talepViewHolder.kullaniciDurum.setText("seninle iletisim kurmak istiyor");

                                            talepViewHolder.kabulButon.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    sohbetlerYol.child(aktifKullaniciId).child(kullaniciIdListesi).child("Sohbetler")
                                                            .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()){
                                                                sohbetlerYol.child(kullaniciIdListesi).child(aktifKullaniciId).child("Sohbetler")
                                                                        .setValue("Kaydedildi")
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                if (task.isSuccessful()){
                                                                                    talepYol.child(aktifKullaniciId).child(kullaniciIdListesi).removeValue()
                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    if (task.isSuccessful()){
                                                                                                        talepYol.child(kullaniciIdListesi).child(aktifKullaniciId).removeValue()
                                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                    @Override
                                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                                        Toast.makeText(getContext(), "Sohbet kaydedildi", Toast.LENGTH_LONG).show();

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
                                            });

                                            talepViewHolder.iptalButon.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    talepYol.child(aktifKullaniciId).child(kullaniciIdListesi)
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {

                                                            if (task.isSuccessful()){
                                                                talepYol.child(kullaniciIdListesi).child(aktifKullaniciId)
                                                                        .removeValue()
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                                Toast.makeText(getContext(), "Sohbet silindi", Toast.LENGTH_LONG).show();

                                                                            }
                                                                        });
                                                            }

                                                        }
                                                    });
                                                }
                                            });



                                        /*talepViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence sequence[] = new CharSequence[]
                                                        {
                                                              "Kabul Et",
                                                              "Iptal Et"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle(talepKullaniciAd+" Chat Talebi");

                                                builder.setItems(sequence, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which==0){
                                                            sohbetlerYol.child(aktifKullaniciId).child(kullaniciIdListesi).child("Sohbetler")
                                                                    .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        sohbetlerYol.child(kullaniciIdListesi).child(aktifKullaniciId).child("Sohbetler")
                                                                                .setValue("Kaydedildi")
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful()){
                                                                                            talepYol.child(aktifKullaniciId).child(kullaniciIdListesi).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                talepYol.child(kullaniciIdListesi).child(aktifKullaniciId).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                Toast.makeText(getContext(), "Sohbet kaydedildi", Toast.LENGTH_LONG).show();

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
                                                        if (which==1){
                                                            sohbetlerYol.child(aktifKullaniciId).child(kullaniciIdListesi).child("Sohbetler")
                                                                    .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        sohbetlerYol.child(kullaniciIdListesi).child(aktifKullaniciId).child("Sohbetler")
                                                                                .setValue("Kaydedildi")
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful()){
                                                                                            talepYol.child(aktifKullaniciId).child(kullaniciIdListesi).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                talepYol.child(kullaniciIdListesi).child(aktifKullaniciId).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                Toast.makeText(getContext(), "Sohbet silindi", Toast.LENGTH_LONG).show();

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

                                                    }
                                                });
                                                builder.show();


                                            }


                                        });*/
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                            else if (tur.equals("gonderildi")){
                                Button talepGondermeBtn = talepViewHolder.itemView.findViewById(R.id.talep_kabul_buton);

                                talepGondermeBtn.setText("Talep Iptal Et");

                                talepViewHolder.itemView.findViewById(R.id.talep_iptal_buton).setVisibility(View.INVISIBLE);

                                kullanicilarYol.child(kullaniciIdListesi).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.hasChild("resim")){

                                            final String talepProfilResim =snapshot.child("resim").getValue().toString();

                                            if (talepProfilResim.isEmpty()){
                                                talepViewHolder.profilResmi.setImageResource(R.drawable.profil_resmi);
                                            }
                                            else {
                                                Picasso.get().load(talepProfilResim).into(talepViewHolder.profilResmi);
                                            }

                                            //Picasso.get().load(talepProfilResim).into(talepViewHolder.profilResmi);
                                        }

                                        final String talepKullaniciAd =snapshot.child("isim").getValue().toString();
                                        final String talepKullaniciDurum =snapshot.child("durum").getValue().toString();

                                        talepViewHolder.kullaniciAd.setText(talepKullaniciAd);
                                        talepViewHolder.kullaniciDurum.setText(" kullanicisina talep gonderdin" );

                                        /*talepViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                CharSequence sequence[] = new CharSequence[]
                                                        {
                                                                "Chat Talebini Iptal Et"
                                                        };

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("Mevcut Chat Talebi");

                                                builder.setItems(sequence, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        if (which==0){
                                                            sohbetlerYol.child(aktifKullaniciId).child(kullaniciIdListesi).child("Sohbetler")
                                                                    .setValue("Kaydedildi").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                    if (task.isSuccessful()){
                                                                        sohbetlerYol.child(kullaniciIdListesi).child(aktifKullaniciId).child("Sohbetler")
                                                                                .setValue("Kaydedildi")
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                                        if (task.isSuccessful()){
                                                                                            talepYol.child(aktifKullaniciId).child(kullaniciIdListesi).removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()){
                                                                                                                talepYol.child(kullaniciIdListesi).child(aktifKullaniciId).removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                Toast.makeText(getContext(), "Chat Talebiniz silindi", Toast.LENGTH_LONG).show();

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

                                                    }
                                                });
                                                builder.show();


                                            }


                                        });*/
                                        talepViewHolder.kabulButon.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                talepYol.child(aktifKullaniciId).child(kullaniciIdListesi)
                                                        .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if (task.isSuccessful()){
                                                            talepYol.child(kullaniciIdListesi).child(aktifKullaniciId)
                                                                    .removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            Toast.makeText(getContext(), "Chat Talebiniz Silindi", Toast.LENGTH_LONG).show();

                                                                        }
                                                                    });
                                                        }

                                                    }
                                                });

                                            }
                                        });

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public TalepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanicilar_gosterme_layout,parent,false);

                TalepViewHolder holder = new TalepViewHolder(view);

                return holder;

            }
        };

        taleplerListem.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();
    }

    public static class TalepViewHolder extends RecyclerView.ViewHolder{
        TextView kullaniciAd,kullaniciDurum;
        CircleImageView profilResmi;
        Button kabulButon,iptalButon;

        public TalepViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAd=itemView.findViewById(R.id.kullanici_profil_ad);
            kullaniciDurum=itemView.findViewById(R.id.kullanici_durumu);
            profilResmi=itemView.findViewById(R.id.kullanici_profili);
            kabulButon=itemView.findViewById(R.id.talep_kabul_buton);
            iptalButon=itemView.findViewById(R.id.talep_iptal_buton);
        }
    }
}