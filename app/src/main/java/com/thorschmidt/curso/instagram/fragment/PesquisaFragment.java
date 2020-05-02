package com.thorschmidt.curso.instagram.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.thorschmidt.curso.instagram.R;
import com.thorschmidt.curso.instagram.activity.PerfilAmigoActivity;
import com.thorschmidt.curso.instagram.adapter.AdapterPesquisa;
import com.thorschmidt.curso.instagram.model.Usuario;
import com.thorschmidt.curso.instagram.util.ConfiguracaoFirebase;
import com.thorschmidt.curso.instagram.util.RecyclerItemClickListener;
import com.thorschmidt.curso.instagram.util.UsuarioFirebase;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PesquisaFragment extends Fragment {

    //Widget
    private SearchView searchViewPesquisa;
    private RecyclerView recyclerPesquisa;

    private List<Usuario> listaUsuarios;
    private DatabaseReference usuariosRef;
    private AdapterPesquisa adapterPesquisa;
    private String idUsuarioLogado;

    public PesquisaFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pesquisa, container, false);

        searchViewPesquisa = view.findViewById(R.id.searchViewPesquisa);
        recyclerPesquisa   = view.findViewById(R.id.recyclerPesquisa);

        //Configurações iniciais
        listaUsuarios = new ArrayList<>();
        usuariosRef = ConfiguracaoFirebase.getFirebase()
                .child("usuarios");
        idUsuarioLogado = UsuarioFirebase.getIdentificadorUsuario();

        //Configura RecyclerView
        setRecyclerView();

        //Configura searchview
        setSearchView();

        return view;
    }

    private void setRecyclerView() {

        recyclerPesquisa.setHasFixedSize(true);
        recyclerPesquisa.setLayoutManager(new LinearLayoutManager(getActivity()));

        adapterPesquisa = new AdapterPesquisa(listaUsuarios, getActivity());

        recyclerPesquisa.setAdapter( adapterPesquisa );

        //Configurar evento de clique
        recyclerPesquisa.addOnItemTouchListener(new RecyclerItemClickListener(
                getActivity(),
                recyclerPesquisa,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        Usuario usuarioSelecionado = listaUsuarios.get(position);
                        Intent i = new Intent(getActivity(), PerfilAmigoActivity.class);
                        i.putExtra("usuarioSelecionado", usuarioSelecionado );
                        startActivity( i );

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));
    }

    private void setSearchView() {

        searchViewPesquisa.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String textoDigitado = newText.toUpperCase();
                pesquisarUsuarios( textoDigitado );
                return true;
            }
        });
    }

    private void pesquisarUsuarios(String texto){

        //limpar lista
        listaUsuarios.clear();

        //Pesquisa usuários caso tenha texto na pesquisa
        if( texto.length() >= 2 ){

            Query query = usuariosRef.orderByChild("nome")
                    .startAt(texto)
                    .endAt(texto + "\uf8ff" );

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //limpar lista
                    listaUsuarios.clear();

                    for( DataSnapshot ds : dataSnapshot.getChildren() ){

                        //verifica se é o usuário logado e o remove da lista
                        Usuario usuario = ds.getValue(Usuario.class);
                        if ( idUsuarioLogado.equals( usuario.getId() ) )
                            continue;

                        //adiciona usuário na lista
                        listaUsuarios.add( usuario );

                    }

                    adapterPesquisa.notifyDataSetChanged();

                    /*
                    int total = listaUsuarios.size();
                    Log.i("totalUsuarios", "total: " + total );
                    */
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

}

