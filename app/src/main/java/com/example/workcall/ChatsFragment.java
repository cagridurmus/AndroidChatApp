package com.example.workcall;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.workcall.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    private View ozelSohbetView;
    private RecyclerView sohbetlerListe;

    //firebase
    private FirebaseAuth mYetki;
    private DatabaseReference sohbetYol,kullaniciYol;

    private String aktifKullaniciId;


    public ChatsFragment() {
        // Required empty public constructor
    }


    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment();
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
        ozelSohbetView= inflater.inflate(R.layout.fragment_chats, container, false);


        //firebase
        mYetki=FirebaseAuth.getInstance();
        aktifKullaniciId=mYetki.getCurrentUser().getUid();
        sohbetYol= FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        kullaniciYol= FirebaseDatabase.getInstance().getReference().child("Kullanicilar");

        sohbetlerListe=ozelSohbetView.findViewById(R.id.sohbetler_liste);
        sohbetlerListe.setLayoutManager(new LinearLayoutManager(getContext()));

        return ozelSohbetView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Kisiler> options=new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(sohbetYol,Kisiler.class)
                .build();

        FirebaseRecyclerAdapter<Kisiler,SohbetlerViewHolder> adapter =new FirebaseRecyclerAdapter<Kisiler, SohbetlerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final SohbetlerViewHolder sohbetlerViewHolder, int i, @NonNull Kisiler kisiler) {
                final String kullaniciId=getRef(i).getKey();
                final String[] profilResim = {"Varsayilan Resim"};

                kullaniciYol.child(kullaniciId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            if (snapshot.hasChild("resim")){
                                profilResim[0] =snapshot.child("resim").getValue().toString();

                                if (profilResim[0].isEmpty()){
                                    sohbetlerViewHolder.profilResmi.setImageResource(R.drawable.profil_resmi);
                                }
                                else {
                                    Picasso.get().load(profilResim[0]).into(sohbetlerViewHolder.profilResmi);
                                }
                                //Picasso.get().load(profilResim[0]).into(sohbetlerViewHolder.profilResmi);
                            }
                            final String kullaniciAdiAl=snapshot.child("isim").getValue().toString();
                            final String kullaniciDurumAl=snapshot.child("durum").getValue().toString();

                            sohbetlerViewHolder.kullaniciAdi.setText(kullaniciAdiAl);

                            //veri tabaninda kullanici durumuna yonelik verileri cekme
                            if (snapshot.child("kullaniciDurum").hasChild("durum")){
                                String durum=snapshot.child("kullaniciDurum").child("durum").getValue().toString();
                                String tarih=snapshot.child("kullaniciDurum").child("tarih").getValue().toString();
                                String zaman=snapshot.child("kullaniciDurum").child("zaman").getValue().toString();

                                if (durum.equals("cevrimici")){
                                    sohbetlerViewHolder.kullaniciDurum.setText("cevrimici");
                                }
                                else if (durum.equals("cevrimdisi")){

                                    sohbetlerViewHolder.kullaniciDurum.setText("Son Gorulme: "+tarih+" "+zaman);

                                }
                            }
                            else {
                                sohbetlerViewHolder.kullaniciDurum.setText("cevrimdisi");
                            }




                            sohbetlerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatActivity = new Intent(getContext(),ChatActivity.class);
                                    chatActivity.putExtra("kullanici_id_ziyaret",kullaniciId);
                                    chatActivity.putExtra("kullanici_ad_ziyaret",kullaniciAdiAl);
                                    chatActivity.putExtra("resim_ziyaret", profilResim[0]);
                                    startActivity(chatActivity);

                                }
                            });

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @NonNull
            @Override
            public SohbetlerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanicilar_gosterme_layout,parent,false);

                return  new SohbetlerViewHolder(view);
            }
        };

        sohbetlerListe.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    public static class SohbetlerViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profilResmi;
        TextView kullaniciAdi,kullaniciDurum;

        public SohbetlerViewHolder(@NonNull View itemView) {
            super(itemView);

            kullaniciAdi=itemView.findViewById(R.id.kullanici_profil_ad);
            kullaniciDurum=itemView.findViewById(R.id.kullanici_durumu);

            profilResmi=itemView.findViewById(R.id.kullanici_profili);
        }
    }
}