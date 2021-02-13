package com.example.workcall;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.workcall.Model.Kisiler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
 * Use the {@link KisilerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KisilerFragment extends Fragment {

    private View kisilerView;

    private RecyclerView kisilerListem;

    private DatabaseReference sohbetYol,kullaniciYol;
    private FirebaseAuth mYetki;

    private String aktifKullaniciId;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private String mParam1;
    private String mParam2;

    public KisilerFragment() {
        // Required empty public constructor
    }


    public static KisilerFragment newInstance(String param1, String param2) {
        KisilerFragment fragment = new KisilerFragment();
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
        kisilerView = inflater.inflate(R.layout.fragment_kisiler, container, false);

        kisilerListem=kisilerView.findViewById(R.id.kisiler_listesi);
        kisilerListem.setLayoutManager(new LinearLayoutManager(getContext()));

        mYetki =FirebaseAuth.getInstance();

        aktifKullaniciId=mYetki.getCurrentUser().getUid();

        sohbetYol= FirebaseDatabase.getInstance().getReference().child("Sohbetler").child(aktifKullaniciId);
        kullaniciYol=FirebaseDatabase.getInstance().getReference().child("Kullanicilar");


        return kisilerView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Kisiler> options = new FirebaseRecyclerOptions.Builder<Kisiler>()
                .setQuery(sohbetYol,Kisiler.class)
                .build();

        //adapter
        FirebaseRecyclerAdapter<Kisiler,KisilerViewHolder> adapter = new FirebaseRecyclerAdapter<Kisiler, KisilerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull KisilerViewHolder kisilerViewHolder, int i, @NonNull Kisiler kisiler) {

                String secilenKullaniciId =getRef(i).getKey();

                kullaniciYol.child(secilenKullaniciId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            if (snapshot.child("kullaniciDurum").hasChild("durum")){
                                String durum=snapshot.child("kullaniciDurum").child("durum").getValue().toString();
                                String tarih=snapshot.child("kullaniciDurum").child("tarih").getValue().toString();
                                String zaman=snapshot.child("kullaniciDurum").child("zaman").getValue().toString();

                                if (durum.equals("cevrimici")){
                                    kisilerViewHolder.cevrimiciSimge.setVisibility(View.VISIBLE);
                                }
                                else if (durum.equals("cevrimdisi")){
                                    kisilerViewHolder.cevrimiciSimge.setVisibility(View.INVISIBLE);
                                }
                            }
                            else {
                                kisilerViewHolder.cevrimiciSimge.setVisibility(View.INVISIBLE);
                            }


                            if (snapshot.hasChild("resim")){
                                //verileri firebaseden cekme
                                String profilResim=snapshot.child("resim").getValue().toString();
                                String kulllaniciAd=snapshot.child("isim").getValue().toString();
                                String kullaniciDurum=snapshot.child("durum").getValue().toString();

                                //kontrollere veri aktarimi
                                kisilerViewHolder.kullaniciAdi.setText(kulllaniciAd);
                                kisilerViewHolder.kullaniciDurumu.setText(kullaniciDurum);

                                if (profilResim.isEmpty()){
                                    kisilerViewHolder.profilResmi.setImageResource(R.drawable.profil_resmi);
                                }
                                else {
                                    Picasso.get().load(profilResim).into(kisilerViewHolder.profilResmi);
                                }

                                //Picasso.get().load(profilResim).into(kisilerViewHolder.profilResmi);
                            }

                            else {

                                //verileri firebaseden cekme
                                String kullaniciAd=snapshot.child("isim").getValue().toString();
                                String kullaniciDurum=snapshot.child("durum").getValue().toString();

                                //kontrollere veri aktarimi
                                kisilerViewHolder.kullaniciAdi.setText(kullaniciAd);
                                kisilerViewHolder.kullaniciDurumu.setText(kullaniciDurum);

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
            public KisilerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.kullanicilar_gosterme_layout,parent,false);
                KisilerViewHolder viewHolder = new KisilerViewHolder(view);

                return viewHolder;

            }
        };

        kisilerListem.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.startListening();

    }

    public static class  KisilerViewHolder extends RecyclerView.ViewHolder{
        //kontroller
        TextView kullaniciAdi,kullaniciDurumu;
        CircleImageView profilResmi;
        ImageView cevrimiciSimge;

        public KisilerViewHolder(@NonNull View itemView) {
            super(itemView);

            //tanimlamalar
            kullaniciAdi=itemView.findViewById(R.id.kullanici_profil_ad);
            kullaniciDurumu=itemView.findViewById(R.id.kullanici_durumu);
            profilResmi=itemView.findViewById(R.id.kullanici_profili);
            cevrimiciSimge=itemView.findViewById(R.id.kullanici_cevrimici);


        }
    }
}